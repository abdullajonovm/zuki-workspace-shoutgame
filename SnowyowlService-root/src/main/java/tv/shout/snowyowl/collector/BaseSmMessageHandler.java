package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;

import tv.shout.collector.BaseMessageHandler;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.collector.SubscriberUtil;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.FastMap;

public abstract class BaseSmMessageHandler
extends BaseMessageHandler
implements GamePublisher
{
    private static Logger _logger = Logger.getLogger(BaseSmMessageHandler.class);

    //ROLES
    public static final String SUPERUSER = "SUPERUSER";
    public static final String ADMIN = "ADMIN";
    public static final String GAME_ADMIN = "GAME_ADMIN";
    public static final String SHOUTCASTER = "SHOUTCASTER";
    public static final String PAYOUT_MODEL = "PAYOUT_MODEL";
    public static final String AFFILIATE = "AFFILIATE";
    public static final String SPONSOR = "SPONSOR";
    public static final String ROLE_SETTER = "ROLE_SETTER";

    //includes roles that can be set. SUPERUSER and ROLE_SETTER can't be set except manually - by design
    protected List<String> ROLES = Arrays.asList(
        ADMIN, GAME_ADMIN, SHOUTCASTER, PAYOUT_MODEL, AFFILIATE, SPONSOR
    );

    @Autowired
    protected IDaoMapper _dao;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected SubscriberUtil _subscriberUtil;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    protected abstract List<String> getValidFormVars();

    @Override
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        logCreateMessage();

        CollectorMessage m = new CollectorMessage();
        m.setMessageType(getHandlerMessageType());
        Map<String, String> props = createProps(requestPath, requestHeaders, requestParameters);

        // Add all allowed params to the collector message
        requestParameters.entrySet().stream()
            .filter(map -> getValidFormVars().contains(map.getKey()))
            .forEach(map -> props.put(map.getKey(), map.getValue()));

        m.setProperties(props);
        CollectorMessageResult result = new CollectorMessageResult(m);
        return result;
    }

    @Override
    public void handleMessage(CollectorMessage message)
    throws PublishResponseError, BadRequestException
    {
        Map<String, String> props = message.getProperties();
        String requestPath = props.get("__requestPath");
        _logger.info("processing " + getHandlerMessageType() + " message: " + requestPath);

        CollectorEndpointHandler collectorEndpointHandler = Optional
                .ofNullable(_collectorEndpointHandlerByPath.get(requestPath))
                .orElseThrow(BadRequestException::new);

        String logMessageTag = getLogMessageTag(requestPath);

        Map<String,Object> extraResponseParms = collectorEndpointHandler.getMessageHandlerFunction().apply(message, logMessageTag);

        publishResponseWdsDoc(props.get(PARM_TO_WDS), message.getMessageId(), logMessageTag, true, null, null, extraResponseParms);
    }

    @Override
    public HttpResponse handleSyncRequestCallback(HttpRequest request)
    throws BadRequestException
    {
        _logger.info("processing sync request: " + request.getPath());

        String logMessageTag = getLogMessageTag(request.getPath());

        CollectorEndpointHandler collectorEndpointHandler = Optional
                .ofNullable(_collectorEndpointHandlerByPath.get(request.getPath()))
                .orElseThrow(BadRequestException::new);

        return collectorEndpointHandler.getSyncRequestHandlerFunction().apply(request, logMessageTag);
    }

    protected Subscriber getSubscriber(Map<String, String> props, String messageId, String logTag)
    throws PublishResponseError
    {
        SubscriberUtil.SubscriberResponse response = _subscriberUtil.getSubscriberFromSession(props);
        if (response.subscriber == null) {
            throw new PublishResponseError(
                props.get(PARM_TO_WDS),
                messageId,
                logTag,
                false,
                "subscriberNotFound",
                response.noSubscriberReason.toString()
            );
        } else {
            return response.subscriber;
        }
    }

    protected Subscriber getSubscriber(HttpRequest props, String logTag)
    throws PublishResponseError
    {
        SubscriberUtil.SubscriberResponse response = _subscriberUtil.getSubscriberFromSession(props);
        if (response.subscriber == null) {
            throw new PublishResponseError(
                null,
                null,
                logTag,
                false,
                "subscriberNotFound",
                response.noSubscriberReason.toString()
            );
        } else {
            return response.subscriber;
        }
    }

    protected int getAppId()
    throws PublishResponseError
    {
        return _shoutContestService.getContextId(new FastMap<>("appId", ISnowyowlService.APP_ID));
    }

    public static <T> Map<T, T> tupleListToMap(List<Tuple<T>> list)
    {
        if (list == null) return null;

        Map<T, T> map = new HashMap<>(list.size());
        list.forEach(tuple -> {
            map.put(tuple.getKey(), tuple.getVal());
        });

        return map;
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        return wrapInTransaction(_transactionManager, transactionalMethod, params);
    }

    public static Object wrapInTransaction(PlatformTransactionManager transactionManager, Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            result = transactionalMethod.apply(params);

            transactionManager.commit(txStatus);
            txStatus = null;

        } catch (RuntimeException e) {
            _logger.error("uncaught runtime exception", e);
            throw e;

        } finally {
            if (txStatus != null) {
                transactionManager.rollback(txStatus);
                txStatus = null;
                throw new IllegalStateException("transaction failed");
            }
        }

        return result;
    }

    //make sure the caller has this wrapped in a transaction
    protected void publishGameWithExtrasToWds(String gameId)
    {
        publishGameWithExtrasToWds(gameId, null, null, null);
    }

    //make sure the caller has this wrapped in a transaction
    protected void publishGameWithExtrasToWds(String gameId, PayoutModel pm, GamePayout gp, GameStats gameStats)
    {
        publishGameWithExtrasToWds(gameId, pm, gp, gameStats, _dao, _shoutContestService);
    }

}
