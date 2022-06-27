package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.gameplay.domain.Tuple;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.HandlerStyle;
import com.meinc.webdatastore.service.IWebDataStoreService;

import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.CashPoolTransaction2;
import tv.shout.sc.domain.CouponBatch;
import tv.shout.sc.domain.CouponCode;
import tv.shout.snowyowl.common.WdsPublisher;
import tv.shout.snowyowl.domain.AffiliatePlan;
import tv.shout.snowyowl.domain.IneligibleSubscriber;
import tv.shout.snowyowl.domain.ProhibitedSubscriber;
import tv.shout.snowyowl.domain.SponsorCashPool;
import tv.shout.snowyowl.forensics.FGame;
import tv.shout.snowyowl.forensics.ForensicsDao;
import tv.shout.util.FastMap;

public class SuperuserHandler
extends BaseSmMessageHandler
implements WdsPublisher
{
    private static Logger _logger = Logger.getLogger(SuperuserHandler.class);

    @Autowired
    private IIdentityService _identityService;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    @Autowired
    private ForensicsDao _forensicsDao;

    // Valid form post param names
    private static final List<String> _validFormVars = Arrays.asList(
        "appId", "processId", "nickname",
        "amount", "quantity", "expireDate", "batchName", "batchId", "couponCode", "outstanding", "redeemedSince", "subscriberIds",
        "email", "linkedEmail", "isId", "reason", "note", "subscriberId", "role",
        "affiliateOwnerEmail", "affiliateDirectPayoutPct", "affiliateSecondaryPayoutPct", "affiliateTertiaryPayoutPct", "playerInitialPayoutPct",
        "sponsorEmail"
    );

    @Override
    public String getHandlerMessageType()
    {
        return "SO_SUPERUSER_HANDLER";
    }

    @Override
    protected List<String> getValidFormVars()
    {
        return _validFormVars;
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {
                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/subscriber/setPassword", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER)))
                .withSyncRequestHandlerFunction(
                        (request, logMessageTag) ->
                        setSubscriberPassword(request)),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/subscriber/giveBonusCash", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        subscriberGiveBonusCash(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/createBatch", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponCreateBatch(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/cancelBatch", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponCancelBatch(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/cancelCoupon", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponCancelCoupon(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/retrieveBatches", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponRetrieveBatches(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/retrieve", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponRetrieve(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/coupon/assign", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        couponAssign(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/forensics/getGame", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER)))
                .withSyncRequestHandlerFunction(
                        (request, logMessageTag) ->
                        getGameForensics(request)),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/forensics/getMultiLocalizationValue", ConnectionType.ANY, HandlerStyle.SYNC_REQUEST))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER)))
                .withSyncRequestHandlerFunction(
                        (request, logMessageTag) ->
                        getMultiLocalizationValueForensics(request)),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/ineligibleSubscriber/get", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getIneligibleSubscribers(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/ineligibleSubscriber/insert", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        insertIneligibleSubscriber(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/ineligibleSubscriber/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deleteIneligibleSubscriber(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/prohibitedSubscriber/get", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getProhibitedSubscribers(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/prohibitedSubscriber/insert", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        insertProhibitedSubscriber(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/prohibitedSubscriber/delete", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(ADMIN)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        deleteProhibitedSubscriber(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/role/list", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER, ROLE_SETTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        roleList(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/role/get", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER, ROLE_SETTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        roleGet(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/role/remove", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER, ROLE_SETTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        roleRemove(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/role/add", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER, ROLE_SETTER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        roleAdd(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/affiliateplan/add", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        addAffiliatePlan(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowladmin/sponsor/addCash", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList(SUPERUSER)))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        sponsorAddCash(message.getProperties(), message.getMessageId())),

        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private HttpResponse setSubscriberPassword(HttpRequest request)
    {
        Map<String, Object> result;

        try {
            int appId = getAppId();

            String email = request.getFirstParameter("email");
            String newPassword = request.getFirstParameter("password");

            Subscriber s = _identityService.getSubscriberByEmail(appId, email);
            if (s == null) {
                throw new PublishResponseError(null, null, null, false, "subscriberNotFound");
            }
            _identityService.setSubscriberPassword(IIdentityService.PASSWORD_SCHEME.USE_PASSWORD_AS_IS, s.getSubscriberId(), newPassword);

            result = new FastMap<>("success", true);

        } catch (PublishResponseError e) {
            result = new FastMap<>("success", false, e.getFailureType(), true, "message", e.getFailureMessage());
        }

        HttpResponse response = new HttpResponse();
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            writer.print(_jsonMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            _logger.error("unable to write response json", e);
        }
        writer.flush();

        return response;
    }

    private Map<String, Object> subscriberGiveBonusCash(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "subscriberGiveBonusCash";

        String nickname = getParamFromProps(props, messageId, docType, "nickname", true);
        double amount = getDoubleParamFromProps(props, messageId, docType, "amount", true);
        int appId = getAppId();

        //look up the person receiving the bonus
        Subscriber subscriber = _identityService.getSubscriberByNickname(appId, nickname);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "subscriberNotFound");
        }

        //make sure the amount is positive
        if (amount <= 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "amount");
        }

        //give them the bonus
        _shoutContestService.addCashPoolTransaction(
                subscriber.getSubscriberId(), amount, CashPoolTransaction2.TYPE.BONUS, "manual admin API call", null, null);

        return null;
    }

    private Map<String, Object> couponCreateBatch(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "couponCreateBatch";

        double amount = getDoubleParamFromProps(props, messageId, docType, "amount", true);
        int quantity = getIntParamFromProps(props, messageId, docType, "quantity", true);
        Date expireDate = getDateParamFromProps(props, messageId, docType, "expireDate", false);
        String batchName = getParamFromProps(props, messageId, docType, "batchName", true);

        CouponBatch couponBatch = new CouponBatch();
        couponBatch.setBatchName(batchName);
        couponBatch.setAmount(amount);
        couponBatch.setExpireDate(expireDate);

        couponBatch = _shoutContestService.createCouponBatch(couponBatch, quantity);

        return new FastMap<>("couponBatch", couponBatch);
    }

    private Map<String, Object> couponCancelBatch(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "couponCancelBatch";
        int batchId = getIntParamFromProps(props, messageId, docType, "batchId", true);

        //will only cancel anything not already redeemed
        _shoutContestService.cancelCouponBatch(batchId);

        return null;
    }

    private Map<String, Object> couponCancelCoupon(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "couponCancelCoupon";

        String couponCode = getParamFromProps(props, messageId, docType, "couponCode", true);

        CouponCode coupon = _shoutContestService.getCouponCode(couponCode);
        if (coupon == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "couponCode");
        }

        //will only cancel if it hasn't already been redeemed
        _shoutContestService.cancelCouponCode(couponCode);

        return null;
    }

    private Map<String, Object> couponRetrieveBatches(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        //String docType = "couponRetrieveBatch";

        List<CouponBatch> couponBatches = _shoutContestService.getCouponBatches();

        return new FastMap<>("couponBatches", couponBatches);
    }

    private Map<String, Object> couponRetrieve(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "couponRetrieve";

        Integer batchId = getIntParamFromProps(props, messageId, docType, "batchId", false);
        if (batchId != null) {
            //retrieve all coupons for that batch
            return new FastMap<>("coupons", _shoutContestService.getCouponsForBatch(batchId));
        }

        Boolean outstanding = getBooleanParamFromProps(props, messageId, docType, "outstanding", false);
        if (outstanding != null && outstanding) {
            //retrieve all coupons that aren't expired, aren't cancelled, and aren't redeemed
            return new FastMap<>("coupons", _shoutContestService.getUnusedCoupons());
        }

        String couponCode = getParamFromProps(props, messageId, docType, "couponCode", false);
        if (couponCode != null) {
            //retrieve the specific coupon
            return new FastMap<>("coupon", _shoutContestService.getCouponCode(couponCode));
        }

        Date redeemedSince = getDateParamFromProps(props, messageId, docType, "redeemedSince", false);
        if (redeemedSince != null) {
            //retrieve all that have been redeemed since the given date
            return new FastMap<>("coupons", _shoutContestService.getCouponsRedeemedSince(redeemedSince));
        }

        _logger.warn("call to /smadmin/coupon/retrieve, but no valid query given. ignoring");
        return null;
    }

    private Map<String, Object> couponAssign(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "couponAssign";

        String sArg = getParamFromProps(props, messageId, docType, "subscriberIds", true);
        List<String> sList = Arrays.asList(sArg.split(","));
        List<Long> subscriberIds = sList.stream().map(s -> Long.parseLong(s)).collect(Collectors.toList());

        int batchId = getIntParamFromProps(props, messageId, docType, "batchId", true);

        _shoutContestService.assignCouponsToSubscribersFromBatch(subscriberIds, batchId);

        return null;
    }

    private HttpResponse getGameForensics(HttpRequest request)
    {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);

        FGame game = null;
        try {
            String gameId = request.getFirstParameter("gameId");

            game = _forensicsDao.getGame(gameId);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        HttpResponse response = new HttpResponse();
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        try {
            writer.print(_jsonMapper.writeValueAsString(game));
        } catch (JsonProcessingException e) {
            _logger.error("unable to write response json", e);
        }
        writer.flush();

        return response;
    }

    private HttpResponse getMultiLocalizationValueForensics(HttpRequest request)
    {
        String uuid = request.getFirstParameter("uuid");
        String type = request.getFirstParameter("type");
        String languageCode = request.getFirstParameter("languageCode");

        boolean wrapInJson = Boolean.parseBoolean(request.getFirstParameter("json"));

        String value = (String) wrapInTransaction(this::getMultiLocalizationValueForensicsTransaction, new Object[] {uuid, type, languageCode});

        HttpResponse response = new HttpResponse();
        PrintWriter writer = response.getWriter();

        if (wrapInJson) {
            response.setContentType("application/json");
            Map<String, Object> body = new FastMap<>("content", value);
            try {
                writer.print(_jsonMapper.writeValueAsString(body));
            } catch (JsonProcessingException e) {
                _logger.error("unable to write response json", e);
            }
        } else {
            response.setContentType("text/plain");
            writer.print(value);
        }

        writer.flush();
        return response;
    }

    private String getMultiLocalizationValueForensicsTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        String uuid = (String) o[0];
        String type = (String) o[1];
        String languageCode = (String) o[2];

        List<Tuple<String>> vals = _dao.getMultiLocalizationValues(uuid, type);
        return vals.stream().filter(t -> t.getKey().equals(languageCode)).map(t -> t.getVal()).findFirst().orElse(null);
    }

    private Map<String, Object> getIneligibleSubscribers(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        //String docType = "getIneligibleSubscribers";

        @SuppressWarnings("unchecked")
        List<IneligibleSubscriber> is = (List<IneligibleSubscriber>) wrapInTransaction(this::getIneligibleSubscribersDao, null);

        return new FastMap<>("ineligibleSubscribers", is);
    }

    private List<IneligibleSubscriber> getIneligibleSubscribersDao(Object params)
    {
        return _dao.getIneligibleSubscribers();
    }

    private Map<String, Object> insertIneligibleSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "insertIneligibleSubscriber";

        //lookup via email
        String email = getParamFromProps(props, messageId, docType, "email", true);
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, email);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "email");
        }

        //see if there is a linked subscriber (also via email lookup)
        Long linkedSubscriberId = null;
        String linkedEmail = getParamFromProps(props, messageId, docType, "linkedEmail", false);
        if (linkedEmail != null) {
            Subscriber linkedSubscriber = _identityService.getSubscriberByEmail(appId, linkedEmail);
            if (linkedSubscriber == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "linkedEmail");
            }
            linkedSubscriberId = linkedSubscriber.getSubscriberId();
        }

        //require a description, and make sure it's a known value
        String reason = getParamFromProps(props, messageId, docType, "reason", true);
        switch (reason)
        {
            case "EMPLOYEE":
            case "IMMEDIATE_FAMILY_MEMBER":
                break;

            default:
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "reason");
        }

        wrapInTransaction(this::insertIneligibleSubscriberDao, new IneligibleSubscriber(subscriber.getSubscriberId(), subscriber.getEmail(), linkedSubscriberId, linkedEmail, reason));

        return null;
    }

    private Void insertIneligibleSubscriberDao(Object params)
    {
        IneligibleSubscriber is = (IneligibleSubscriber) params;
        _dao.insertIneligibleSubscriber(is);
        return null;
    }

    private Map<String, Object> deleteIneligibleSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "deleteIneligibleSubscriber";

        wrapInTransaction(
            this::deleteIneligibleSubscriberDao,
            getLongParamFromProps(props, messageId, docType, "isId", true));

        return null;
    }

    private Void deleteIneligibleSubscriberDao(Object params)
    {
        long isId = (Long) params;
        _dao.deleteIneligibleSubscriber(isId);
        return null;
    }

    private Map<String, Object> getProhibitedSubscribers(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        @SuppressWarnings("unchecked")
        List<ProhibitedSubscriber> ps = (List<ProhibitedSubscriber>) wrapInTransaction(this::getProhibitedSubscribersDao, null);

        return new FastMap<>("prohibitedSubscribers", ps);
    }

    private List<ProhibitedSubscriber> getProhibitedSubscribersDao(Object params)
    {
        return _dao.getProhibitedSubscribers();
    }

    private Map<String, Object> insertProhibitedSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "insertProhibitedSubscriber";

        //lookup via nickname
        String nickname = getParamFromProps(props, messageId, docType, "nickname", true);
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByNickname(appId, nickname);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "nickname");
        }

        //require a reason, and make sure it's a known value
        String reason = getParamFromProps(props, messageId, docType, "reason", true);
        switch (reason)
        {
            case "CHEATING":
            case "WAGERING":
            case "DUPLICATE_ACCOUNT":
            case "OTHER":
                break;

            default:
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "reason");
        }

        //if reason is OTHER, require a note (otherwise, note is optional)
        String note = getParamFromProps(props, messageId, docType, "note", reason.equals("OTHER"));

        wrapInTransaction(
            this::insertProhibitedSubscriberDao,
            new ProhibitedSubscriber(subscriber, reason, note));

        return null;
    }

    private Void insertProhibitedSubscriberDao(Object params)
    {
        ProhibitedSubscriber ps = (ProhibitedSubscriber) params;
        _dao.insertProhibitedSubscriber(ps);

        //add to the action log
        _dao.addSubscriberActionLog(ps.getSubscriberId(), "ACCOUNT_DEACTIVATED", ps.getReason(), ps.getNote());

        return null;
    }

    private Map<String, Object> deleteProhibitedSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "deleteProhibitedSubscriber";

        //lookup via id
        long subscriberId = getLongParamFromProps(props, messageId, docType, "subscriberId", true);
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "subscriberId");
        }

        //require a reason, and make sure it's a known value
        String reason = getParamFromProps(props, messageId, docType, "reason", true);
        switch (reason)
        {
            case "WASNT_CHEATING":
            case "WASNT_WAGERING":
            case "DUPLICATE_ACCOUNT_CLEANED_UP":
            case "OTHER":
                break;

            default:
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "reason");
        }

        //if reason is OTHER, require a note (otherwise, note is optional)
        String note = getParamFromProps(props, messageId, docType, "note", reason.equals("OTHER"));

        wrapInTransaction(this::deleteProhibitedSubscriberDao, new ProhibitedSubscriber(subscriber, reason, note));

        return null;
    }

    private Void deleteProhibitedSubscriberDao(Object params)
    {
        ProhibitedSubscriber ps = (ProhibitedSubscriber) params;
        _dao.deleteProhibitedSubscriber(ps.getSubscriberId());

        //add to the action log
        _dao.addSubscriberActionLog(ps.getSubscriberId(), "ACCOUNT_REACTIVATED", ps.getReason(), ps.getNote());

        return null;
    }

    private Map<String, Object> roleList(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        //String docType = "roleList";

        return new FastMap<>("roles", ROLES);
    }

    private Map<String, Object> roleGet(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "roleGet";

        //subscriber lookup via email
        String email = getParamFromProps(props, messageId, docType, "email", true);
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, email);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "email");
        }

        List<String> roles = _identityService.getSubscriberRoles(subscriber.getSubscriberId());

        return new FastMap<>("roles", roles);
    }

    private Map<String, Object> roleRemove(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "roleRemove";

        //subscriber lookup via email
        String email = getParamFromProps(props, messageId, docType, "email", true);
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, email);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "email");
        }

        String role = getParamFromProps(props, messageId, docType, "role", true);

        _identityService.removeRole(subscriber.getSubscriberId(), role);

        return null;
    }

    private Map<String, Object> roleAdd(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "roleAdd";

        //subscriber lookup via email
        String email = getParamFromProps(props, messageId, docType, "email", true);
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, email);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "email");
        }

        //make sure the role is valid
        String role = getParamFromProps(props, messageId, docType, "role", true);
        if (!ROLES.contains(role)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "role");
        }

        _identityService.addRole(subscriber.getSubscriberId(), role);

        return null;
    }

    private Map<String, Object> addAffiliatePlan(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "addAffiliatePlan";

        double affiliateDirectPayoutPct = getDoubleParamFromProps(props, messageId, docType, "affiliateDirectPayoutPct", true);
        double affiliateSecondaryPayoutPct = getDoubleParamFromProps(props, messageId, docType, "affiliateSecondaryPayoutPct", true);
        double affiliateTertiaryPayoutPct = getDoubleParamFromProps(props, messageId, docType, "affiliateTertiaryPayoutPct", true);
        double playerInitialPayoutPct = getDoubleParamFromProps(props, messageId, docType, "playerInitialPayoutPct", true);

        if ((playerInitialPayoutPct + affiliateSecondaryPayoutPct) > 1D) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "payoutsTooLarge");
        }

        if ((playerInitialPayoutPct + affiliateTertiaryPayoutPct) > 1D) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "payoutsTooLarge");
        }

        AffiliatePlan plan = new AffiliatePlan();
        plan.setAffiliateDirectPayoutPct(affiliateDirectPayoutPct);
        plan.setAffiliateSecondaryPayoutPct(affiliateSecondaryPayoutPct);
        plan.setAffiliateTertiaryPayoutPct(affiliateTertiaryPayoutPct);
        plan.setPlayerInitialPayoutPct(playerInitialPayoutPct);
        plan.setCurrent(true);

        //to get back the primary key
        plan = (AffiliatePlan) wrapInTransaction(this::addAffiliatePlanDao, plan);

        //publish this affiliate plan
        publishJsonWdsDoc(_logger, _wdsService, null, "/affiliatePlan.json", plan);

        return null;
    }

    private AffiliatePlan addAffiliatePlanDao(Object param)
    {
        AffiliatePlan plan = (AffiliatePlan) param;

        _dao.clearCurrentAffiliatePlan();
        _dao.addAffiliatePlan(plan); //this will set the primary key on the object

        return plan;
    }

    private Map<String, Object> sponsorAddCash(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "sponsorAddCash";

        String sponsorEmail = getParamFromProps(props, messageId, docType, "sponsorEmail", true);
        double amount = getFloatParamFromProps(props, messageId, docType, "amount", true);

        //look up the subscriber via the email
        int appId = getAppId();
        Subscriber subscriber = _identityService.getSubscriberByEmail(appId, sponsorEmail);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "sponsorEmail");
        }

        //make sure the given subscriber is a sponsor
        if (!_identityService.hasRole(subscriber.getSubscriberId(), new HashSet<>(Arrays.asList(BaseSmMessageHandler.SPONSOR)), true)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "notASponsor");
        }

        //make sure the amount is positive
        if (amount <= 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "amountMustBePositive");
        }

        //add the amount to the sponsor pool
        wrapInTransaction(this::sponsorAddCashDao, new Object[] {subscriber.getSubscriberId(), amount});

        return null;
    }

    private Void sponsorAddCashDao(Object param)
    {
        Object[] params = (Object[]) param;
        int subscriberId = (Integer) params[0];
        double amount = (Double) params[1];

        //add or update the sponsor cash pool row
        SponsorCashPool pool = _dao.getSponsorCashPoolByPoolOwnerSubscriberId(subscriberId);
        if (pool == null) {
            pool = new SponsorCashPool();
            pool.setSubscriberId(subscriberId);
            pool.setAmount(amount);
            _dao.insertSponsorCashPool(pool); //this will set the id
        } else {
            pool.setAmount(pool.getAmount() + amount);
            _dao.updateSponsorCashPool(pool);
        }

        //add a transaction row
        _dao.addSponsorCashPoolTransaction(pool.getSponsorCashPoolId(), amount, "DEPOSIT");

        return null;
    }

}
