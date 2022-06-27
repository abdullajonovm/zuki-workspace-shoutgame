package tv.shout.snowyowl.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.notification.domain.NotificationPref;
import com.meinc.notification.exception.InvalidPrefException;
import com.meinc.notification.service.INotificationService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.urlshorten.service.IUrlShortenerService;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import io.socket.client.Socket;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.LocalizationHelper;
import tv.shout.snowyowl.common.ShortUrlGenerator;
import tv.shout.snowyowl.common.SmsSender;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class SubscriberHandler
extends BaseSmMessageHandler
implements SmsSender, ShortUrlGenerator
{
    private static Logger _logger = Logger.getLogger(SubscriberHandler.class);

    private static final List<String> _validFormVars = Arrays.asList(
        "prefType", "prefValue", "phone", "code", "message", "path"
    );

    private static final String SMS_VERIFY_CODE_MESSAGE_UUID = "07362305-9e96-11e5-b784-86e93e99d7ba";

    @Value("${twilio.code.length}")
    private int _verificationCodeLength;

    @Value("${twilio.from.number}")
    private String _twilioFromNumber;

    @Value("${twilio.account.sid}")
    private String _twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String _twilioAuthToken;

    @Value("${twilio.verificationCode.duration}")
    private int _verificationCodeDurationM;

    @Value("${socket.io.ips}")
    private String _socketIoIpsUnparsed;

    @Value("${shorten.url.domain}")
    private String _shortUrlDomain;

    @Value("${shorten.url.short.url.prefix}")
    private String _shortUrlPrefix;

    @Autowired
    private INotificationService _notificationService;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private IUrlShortenerService _urlShortenerService;

    private Socket _socketIoSocket;

    public void setSocketIoSocket(Socket socketIoSocket)
    {
        _socketIoSocket = socketIoSocket;
    }

    @Override
    public String getHandlerMessageType()
    {
        return "SM_SUBSCRIBER";
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

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/notification/setPref", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        setNotificationPref(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/phone/sendVerificationCode", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        sendPhoneVerificationCode(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/phone/verifyCode", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        verifyPhoneCode(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/socketio/echo", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        doSocketIoEcho(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/subscriber/details", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        subscriberDetails(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/snowl/shorturl/get", ConnectionType.ANY))
                    .withValidRoles(new HashSet<>(Arrays.asList()))
                    .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getShortUrl(message.getProperties(), message.getMessageId())),

        };

            for (CollectorEndpointHandler handler : handlers) {
                _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
            }

            return Arrays.stream(handlers)
                    .map(CollectorEndpointHandler::getCollectorEndpoint)
                    .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> setNotificationPref(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "setNotificationPref");

        String prefTypeName = getParamFromProps(props, messageId, "setNotificationPref", "prefType", true);
        String prefValue = getParamFromProps(props, messageId, "setNotificationPref", "prefValue", true);

        //make sure prefTypeName is a supported type
        int prefType = 0;
        switch (prefTypeName)
        {
            case ISnowyowlService.NOTIFICATION_TYPE_NOTIFY_ON_ROUND_START:
                prefType = ISnowyowlService.NOTIFICATION_PREF_TYPE_ROUND_START;
                break;
        }
        if (prefType == 0) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "setNotificationPref", false, "invalidParam", "prefType");
        }

        //make sure prefValue is a supported type
        boolean valid;
        switch (prefValue)
        {
            case "NONE":
            case "APP_PUSH":
            case "EMAIL":
            case "SMS":
                valid = true;
                break;

            default:
                valid = false;
        }
        if (!valid) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "setNotificationPref", false, "invalidParam", "prefValue");
        }

        NotificationPref pref = new NotificationPref();
        pref.setPrefType(prefType);
        pref.setSubscriberId(subscriber.getSubscriberId());
        pref.setValue(prefValue);
        pref.setCreated(new Date());
        pref.setLastUpdated(new Date());

        try {
            _notificationService.setPrefs(Collections.singletonList(pref));
        } catch (InvalidPrefException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "setNotificationPref", false, "unexpectedError", e.getMessage());
        }

        return null;
    }

    private Map<String, Object> sendPhoneVerificationCode(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "sendPhoneVerificationCode");

        String phone = getParamFromProps(props, messageId, "sendPhoneVerificationCode", "phone", true);

        //this isn't needed unless the twilio fromNumber is different based on country of origin
        //if (StringUtil.isEmpty(subscriber.getFromCountryCode())) {
        //    throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "sendPhoneVerificationCode", false, "fromCountryCodeNotSet", null);
        //}

        String code = getVerificationCode(_verificationCodeLength);


        //subscriber.setPhone(phone);
        //subscriber.setPhoneVerified(false);
        _identityService.updatePhone(subscriber.getSubscriberId(), phone);

        String smsMessage;
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //store the code for later
            _dao.clearPhoneVerificationCodeForSubscriber(subscriber.getSubscriberId());
            _dao.addPhoneVerificationCodeForSubscriber(subscriber.getSubscriberId(), phone, code);

            smsMessage = MessageFormat.format(
                    LocalizationHelper.getLocalizedString(BaseSmMessageHandler.tupleListToMap(_dao.getMultiLocalizationValues(SMS_VERIFY_CODE_MESSAGE_UUID, "systemMessage")), subscriber.getLanguageCode()),
                    code);

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        String messageSid = sendSms(_twilioFromNumber, phone, smsMessage, _twilioAccountSid, _twilioAuthToken, _logger, subscriber.getSubscriberId(), _identityService, false);
        //response is a json with "sid" - the id of the message,
        // and "status"
        // and a host of other less useful information
        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format(
                "successfully sent verification sms to subscriber {0,number,#} with phone: {1}, code: {2}, messageSid: {3}",
                subscriber.getSubscriberId(), phone, code, messageSid));
        }

        return null;
    }

    private Map<String, Object> verifyPhoneCode(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(props, messageId, "verifyPhoneCode");

        String code = getParamFromProps(props, messageId, "verifyPhoneCode", "code", true);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            //see if the code is valid
            Date cutoffDate = new Date(System.currentTimeMillis() - (_verificationCodeDurationM * 60 * 1000));
            boolean isCodeValid = _dao.isPhoneVerificationCodeValidForSubscriber(subscriber.getSubscriberId(), subscriber.getPhone(), code, cutoffDate);
            if (!isCodeValid) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format(
                            "phone code verification failed. subscriber: {0,number,#}, phone: {1}, code: {2}",
                            subscriber.getSubscriberId(), subscriber.getPhone(), code));
                }
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "verifyPhoneCode", false, "invalidCode");
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format(
                            "phone code verification succeeded. subscriber: {0,number,#}, phone: {1}, code: {2}",
                            subscriber.getSubscriberId(), subscriber.getPhone(), code));
                }
            }

            //clear the code from the db
            _dao.clearPhoneVerificationCodeForSubscriber(subscriber.getSubscriberId());

            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }

        //mark phone as verified
        _identityService.markPhoneAsVerified(subscriber.getSubscriberId());

        return null;
    }

    private Map<String, Object> doSocketIoEcho(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
//_logger.info("1: doSocketIoEcho");
//
//        //set up the socket.io handlers
////        if (_socket == null) {
//_logger.info("2: socket==null");
//
//            //for now, since there's just 1 server...
//            String[] socketIoIps = _socketIoIpsUnparsed.split(",");
//            String socketIoIp = "http://" + socketIoIps[0];
//            try {
//_logger.info("3: about to init socket: " + socketIoIp);
//                _socket = IO.socket(socketIoIp);
//_logger.info("4: initted socket");
//            } catch (URISyntaxException e) {
//                _logger.error("unable to create socket.io connection to: " + socketIoIp, e);
//                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "doSocketIoEcho", false, "unexpectedError", null);
//            }
//
//            _socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    _logger.info("SOCKETIO: connected");
//                }
//            });
//
//            _socket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    _logger.info("SOCKETIO: connecting");
//                }
//            });
//
//            _socket.on("echo_reply", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    _logger.info("SOCKETIO: echo_reply");
//
//                    JsonNode json;
//                    try {
//                        json = _jsonMapper.readTree((String) args[0]);
//                        String echoMessage = json.get("message").textValue();
//                        _logger.info("SOCKETIO received echo_reply of: " + echoMessage);
//
//                    } catch (IOException e) {
//                        _logger.error("unable to parse echo_reply response. raw:\n" + args[0], e);
//                    }
//
//                    _socket.disconnect();
//                }
//
//            });
////        }
//

    try {
        String paramMsg = getParamFromProps(props, messageId, "doSocketIoEcho", "message", true);
//_logger.info("5: message param: " + message);
//
//        //_socket.open();
////_logger.info("6: socket open");
//        _socket.connect();
//_logger.info("7: socket connected");
//
//_logger.info("8: about to emit echo command");
//        _socket.emit("echo", message);
//_logger.info("9: emitted echo command");

        ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

        Map<String, String> payload = new HashMap<>();
        payload.put("message", paramMsg);

        SyncMessage syncMessage = new SyncMessage(
                1, "6aaa", "test_message",
                ISnowyowlService.GAME_ENGINE, _jsonMapper.writeValueAsString(payload));

        Map<String, Object> msg = new HashMap<>();
        msg.put("recipient", "123abc");
        msg.put("message", syncMessage);

        String message = _jsonMapper.writeValueAsString(msg);
        SocketIoLogger.log(_triggerService, null, "send_sync_message", message, "SENDING");
        _socketIoSocket.emit("send_sync_message", message);
        SocketIoLogger.log(_triggerService, null, "send_sync_message", message, "SENT");

    } catch (Exception e) {
        _logger.error("oops", e);
    }

        return null;
    }

    private Map<String, Object> subscriberDetails(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "subscriberDetails";

        Subscriber subscriber = getSubscriber(props, messageId, docType);

        List<NotificationPref> prefs = _notificationService.getPrefsForSubscriber(subscriber.getSubscriberId());

        List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayers(subscriber.getSubscriberId());

        Double balance = _shoutContestService.getTotalBalance(subscriber.getSubscriberId());
        if (balance == null) balance = 0D;
        Double availableBalance = _shoutContestService.getAvailableBalance(subscriber.getSubscriberId());
        if (availableBalance == null) availableBalance = 0D;

        List<String> roles = _identityService.getSubscriberRoles(subscriber.getSubscriberId());

        Map<String, Object> profileData = new HashMap<>(18);
        profileData.put("subscriberId", subscriber.getSubscriberId());
        profileData.put("firstName", subscriber.getFirstname());
        profileData.put("lastName", subscriber.getLastname());
        profileData.put("nickname", subscriber.getNickname());
        profileData.put("nicknameSet", subscriber.isNicknameSet());
        profileData.put("countryCode", subscriber.getFromCountryCode());
        profileData.put("languageCode", subscriber.getLanguageCode());
        profileData.put("currencyCode", subscriber.getCurrencyCode());
        profileData.put("photoUrl", subscriber.getPhotoUrl());
        profileData.put("photoUrlLarge", subscriber.getPhotoUrlLarge());
        profileData.put("photoUrlSmall", subscriber.getPhotoUrlSmall());
        profileData.put("primaryIdHash", subscriber.getEmailSha256Hash());
        profileData.put("email", subscriber.getEmail());
        profileData.put("emailVerified", subscriber.isEmailVerified());
        profileData.put("phone", subscriber.getPhone());
        profileData.put("phoneVerified", subscriber.isPhoneVerified());
        profileData.put("dateOfBirth", DateUtil.dateToIso8601(subscriber.getDateOfBirth()));
        //profileData.put("role", subscriber.getRole());
        profileData.put("roles", roles);

        return new FastMap<>(
            "prefs", prefs,
            "gamePlayers", gamePlayers,
            "balance", balance,
            "availableBalance", availableBalance,
            "profile", profileData
        );
    }

    private Map<String, Object> getShortUrl(Map<String, String> props, String messageId)
    {
        String docType = "getShortUrl";
        String path = getParamFromProps(props, messageId, docType, "path", true);

        String shortUrl;
        try {
            shortUrl = getShortUrl(_urlShortenerService, _shortUrlDomain, _shortUrlPrefix, "https://" + _shortUrlDomain + "/" + path);
        } catch (UrlShortenerException | IOException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
        }

        return new FastMap<>("shortUrl", shortUrl);
    }

    private String getVerificationCode(int length)
    {
        Random randObj = new Random(UUID.randomUUID().toString().hashCode());

        byte b[] = new byte[length];

        for (int i = 0; i < length; i++)
            b[i] = (byte) rand(randObj, '0', '9');

        return new String(b);
    }

    private int rand(Random randObj, int lowNum, int hiNum)
    {
        int n = hiNum - lowNum + 1;
        int i = randObj.nextInt() % n;
        if (i < 0)
            i = -i;

        return lowNum + i;
    }

}
