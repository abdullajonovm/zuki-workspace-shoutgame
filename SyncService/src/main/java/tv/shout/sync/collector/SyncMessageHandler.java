package tv.shout.sync.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import tv.shout.sync.service.ISyncService;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;

@EnableScheduling
@Service("SyncMessageHandler")
public class SyncMessageHandler
extends BaseMessageHandler
{
    public static final String COLLECTOR_MESSAGE_TYPE_SYNC_MESSAGE_HANDLER = "SYNC_MESSAGE_HANDLER";
    public static final String COLLECTOR_SYNC_MESSAGE_PATH = "/sync/privateGenerateSyncDoc";

    private Logger _logger = Logger.getLogger(SyncMessageHandler.class);

    // Valid form post param names
    private static final List<String> validFormVars = Arrays.asList(
            "fromDate", "subscriberId", "securityKey", "contextualId"
    );

    @Value("${sync.collector.security.key}")
    private String _securityKey;

//    @Value("${dm.context.id}")
//    private int defaultContextId;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private SubscriberUtil _subscriberUtil;

//    private ExecutorService executor = Executors.newWorkStealingPool();// .newSingleThreadScheduledExecutor();// .newSingleThreadExecutor();

    //@Override
    @Override
    public String getHandlerMessageType()
    {
        return COLLECTOR_MESSAGE_TYPE_SYNC_MESSAGE_HANDLER;
    }

    //@Override
    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        return new CollectorEndpoint[] {
            new CollectorEndpoint("/sync/generateSyncDoc", ConnectionType.ANY),
            new CollectorEndpoint(COLLECTOR_SYNC_MESSAGE_PATH, ConnectionType.ANY)
        };
    }

    @Override
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders,Map<String, String> requestParameters)
    throws BadRequestException
    {
        logCreateMessage();

//        //init the app helper
//        DistributedMap<String, App> appByNameMap = DistributedMap.getMap("appByName");
//        _appHelper.setAppMap(appByNameMap);
        if (requestPath == null) {
            requestPath = requestParameters.get("__requestPath");
        }

        CollectorMessage m = new CollectorMessage();
        m.setMessageType(getHandlerMessageType());
        Map<String, String> props = createProps(requestPath, requestHeaders, requestParameters);

        // Add params to collector message
        for(String formVar : requestParameters.keySet())
            if (validFormVars.contains(formVar)) {
                props.put(formVar, requestParameters.get(formVar));
            }

        m.setProperties(props);
        CollectorMessageResult result = new CollectorMessageResult(m);
        return result;
    }

    @Override
    public void handleMessage(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        this._logger.info("processing " + getHandlerMessageType() + " message...");

        String toWds = props.get(PARM_TO_WDS);
        String requestPath = props.get("__requestPath");
        Map<String,Object> extraResponseParms = null;
        String logMessageTag = requestPath;
        if (logMessageTag != null && logMessageTag.contains("/"))
            logMessageTag = logMessageTag.replace("/", "");
        else
            logMessageTag = "INVALID_PATH";

        if ("/sync/generateSyncDoc".equals(requestPath)) {
            extraResponseParms = generateSyncDoc(props, message);

        } else if (COLLECTOR_SYNC_MESSAGE_PATH.equals(requestPath)) {
            extraResponseParms = privateGenerateSyncDoc(props, message);

        } else {
            _logger.error("invalid requestPath: " + requestPath);
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), "handleMessage", false, "unknownRequest", requestPath);
        }

        publishResponseWdsDoc(toWds, message.getMessageId(), logMessageTag, true, null, null, extraResponseParms);
    }

    @Override
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        throw new BadRequestException();
    }

    private Map<String, Object> generateSyncDoc(Map<String, String> props, CollectorMessage message)
    throws PublishResponseError
    {
        Subscriber s = _subscriberUtil.getAuthenticatedSubscriber("generateSyncDoc", message);
//_logger.info(">>> generateSyncDoc for subscriber: " + s.getSubscriberId() + ", email: " + s.getEmail());

        Date fromDate;
        fromDate = DateUtil.iso8601ToDate(props.get("fromDate"));
//_logger.info(">>> generateSyndDoc, fromDate: " + fromDate);

        String contextualId = props.get("contextualId");
//_logger.info(">>> generateSyncDoc, contextualId: " + contextualId);

        Date highWatermark = _syncService.sync(s.getSubscriberId(), fromDate, contextualId, props.get(PARM_TO_WDS));
//_logger.info(">>> generateSyncDoc, highWatermark: " + highWatermark);

        return new FastMap<String, Object>("highWatermark", highWatermark);
    }

    private Map<String, Object> privateGenerateSyncDoc(Map<String, String> props, CollectorMessage message)
    throws PublishResponseError
    {
        int subscriberId = Integer.parseInt(props.get("subscriberId"));
//_logger.info(">>> privateGenerateSyncDoc, subscriberId: " + subscriberId);
        String securityKey = props.get("securityKey");
//_logger.info(">>> privateGenerateSyncDoc, securityKey: " + securityKey);

        if (securityKey == null || !securityKey.equals(_securityKey)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), "privateGenerateSyncDoc", false, "securityError", "securityKey does not match");
        }

        Date fromDate;
        fromDate = DateUtil.iso8601ToDate(props.get("fromDate"));
//_logger.info(">>> privateGenerateSyncDoc, fromDate: " + fromDate);

        String contextualId = props.get("contextualId");
//_logger.info(">>> privateGenerateSyncDoc, contextualId: " + contextualId);

        _syncService.generateSyncDocForSubscriber(subscriberId, fromDate, contextualId, props.get(PARM_TO_WDS));

        return null;
    }

}