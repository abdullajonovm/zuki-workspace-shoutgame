package tv.shout.sc.collector;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.meinc.commons.encryption.HexUtils;
import com.meinc.commons.encryption.IEncryption;
import com.meinc.commons.postoffice.exception.PostOfficeException;
import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.EmailPurpose;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.commons.postoffice.service.TemplateEmail;
import com.meinc.gameplay.domain.App;
import com.meinc.identity.domain.SignupData;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberAddress;
import com.meinc.identity.domain.SubscriberAddress.ADDRESS_TYPE;
import com.meinc.identity.domain.SubscriberEmail;
import com.meinc.identity.domain.SubscriberSession;
import com.meinc.identity.exception.EmailAlreadyUsedException;
import com.meinc.identity.exception.InvalidEmailException;
import com.meinc.identity.exception.InvalidEmailPasswordException;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.exception.InvalidSubscriberUpdateException;
import com.meinc.identity.exception.MissingRequiredParameterException;
import com.meinc.identity.exception.NicknameAlreadyUsedException;
import com.meinc.identity.exception.NicknameInvalidException;
import com.meinc.identity.exception.SubscriberInactiveException;
import com.meinc.identity.exception.SubscriberRequiresEulaException;
import com.meinc.identity.exception.SubscriberRequiresUpdateException;
import com.meinc.identity.service.IIdentityService;
import com.meinc.identity.service.IIdentityService.PASSWORD_SCHEME;
import com.meinc.push.service.IPushService;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

import tv.shout.collector.BaseMessageHandler;
import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.collector.SubscriberUtil;
import tv.shout.sc.dao.IContestDaoMapper;
import tv.shout.sc.domain.LocalizationHelper;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.sc.service.ShoutContestService;
import tv.shout.util.DateUtil;
import tv.shout.util.FastMap;
import tv.shout.util.StringUtil;

public class SubscriberManagementHandler
extends BaseMessageHandler
{
    private static Logger _logger = Logger.getLogger(SubscriberManagementHandler.class);

    //used to lookup the internationalized phone verification code message
    private static final String VERIFY_CODE_UUID = "07362305-9e96-11e5-b784-86e93e99d7ba";
    private static final String EMAIL_SUBJECT_UUID = "0dd0c00a-8c1c-43f5-a598-95f55d32aa93";

    // Valid form post param names
    private static final List<String> validFormVars = Arrays.asList(
        "payload",
        "username",
        "subscriberIds", "fullName", "firstName", "lastName", "nickname", "password", "referrerNickname",
        "email", "phone", "photoUrl", /*"photoUrlSmall", "photoUrlLarge",*/ "birthDate", "languageCode", "countryCode",
        "isAdult", "paypalEmail", "region",
        "homeAddrLine1", "homeAddrLine2", "homeAddrCity", "homeAddrPostalCode", "homeAddrStateProvince", "homeAddrCountryCode",
        "accessToken", "facebookAppId", "deviceToken", "appId", "address",
        "phone", "code",
        "genLoginToken", "c", "newPassword"
    );

    @Value("${shorten.url.domain}")
    private String _serverBaseUrl;

    @Value("${sms.verificationCode.length}")
    private int _verificationCodeLength;

    //minutes
    @Value("${sms.verificationCode.duration}")
    private int _verificationCodeDurationM;

    @Value("${twilio.account.sid}")
    private String _twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String _twilioAuthToken;

    @Value("${twilio.from.number}")
    private String _twilioFromNumber;

    @Value("${login.token.private.key}")
    private String _loginTokenPrivateKeyBase64;
    private PrivateKey loginTokenPrivateKey;

    @Value("${email.pwreset.email}")
    private String _pwResetEmail;

    @Value("${email.pwreset.display}")
    private String _pwResetDisplay;

    @Autowired
    private IContestDaoMapper _dao;

    @Autowired
    private IPushService _pushService;

    @Resource(name="webMediaStoreService")
    private IWebDataStoreService _wmsService;

    @Autowired
    private SubscriberUtil _subscriberUtil;

    @Autowired
    private IShoutContestService _shoutContestService;

    @Autowired
    private IEncryption _encryptionService;

    @Autowired
    private IPostOffice _postOfficeService;

    @PostConstruct
    public void init() {
        byte[] loginTokenPrivateKeyBytes = HexUtils.base64StringToBytes(_loginTokenPrivateKeyBase64);
        PKCS8EncodedKeySpec loginTokenPrivateKeySpec = new PKCS8EncodedKeySpec(loginTokenPrivateKeyBytes);
        try {
            loginTokenPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(loginTokenPrivateKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getHandlerMessageType()
    {
        return "AUTHENTICATION_HANDLER";
    }

    @Override
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        CollectorEndpointHandler[] handlers = new CollectorEndpointHandler[] {
                new CollectorEndpointHandler(new CollectorEndpoint("/auth/signup", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        signup(message.getProperties(), message.getMessageId())),

//                new CollectorEndpointHandler(new CollectorEndpoint("/auth/signup/viaFacebook", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        signupViaFacebook(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/auth/login", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        login(message.getProperties(), message.getMessageId())),

//                new CollectorEndpointHandler(new CollectorEndpoint("/auth/login/viaFacebook", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        loginViaFacebook(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/sendPhoneVerificationCode", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        sendPhoneVerificationCode(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/verifyPhoneCode", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        verifyPhoneCode(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/update", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        updateSubscriber(message.getProperties(), message.getMessageId())),

//not used right now. probably won't come back
//                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/get", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        getSubscriber(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/getEmailsAndAddresses", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getSubscriberEmailsAndAddresses(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/address/add", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        addSubscriberAddress(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/getPublicProfile", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        getPublicProfile(message.getProperties(), message.getMessageId())),

//not used right now. might come back
//                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/changePassword", ConnectionType.ANY))
//                .withMessageHandlerFunction(
//                        (message, logMessageTag) ->
//                        changePassword(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/checkUsername", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        checkUsername(message.getProperties(), message.getMessageId())),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/setPushToken", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        setPushToken(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/password/requestReset", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        requestPasswordReset(message)),

                new CollectorEndpointHandler(new CollectorEndpoint("/subscriber/password/reset", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                        (message, logMessageTag) ->
                        resetPassword(message)),
        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);

    }

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
            .filter(map -> validFormVars.contains(map.getKey()))
            .forEach(map -> props.put(map.getKey(), map.getValue()));

        CollectorMessageResult result;

        //special case: when authenticating, a 1 time use encryption key needs to be sent back to the user (and also passed along in the message) so that they
        // subscriber and session key payloads can be encrypted.
        if (requestPath.startsWith("/auth")) {
            String encryptKey = _identityService.generateEncryptKey(32);
            props.put("__encryptKey", encryptKey);
            m.setProperties(props);
            result = new CollectorMessageResult(m).withJsonKeyValueResponse("encryptKey", encryptKey);

        } else {
            m.setProperties(props);
            result = new CollectorMessageResult(m);
        }

        return result;
    }

    @Override
    public void handleMessage(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        String requestPath = props.get("__requestPath");
        _logger.info("processing " + getHandlerMessageType() + " message: " + requestPath);

        String toWds = props.get(PARM_TO_WDS);

        CollectorEndpointHandler collectorEndpointHandler = Optional
                .ofNullable(_collectorEndpointHandlerByPath.get(requestPath))
                .orElseThrow(BadRequestException::new);

        String logMessageTag = getLogMessageTag(requestPath);

        Map<String,Object> extraResponseParms = collectorEndpointHandler.getMessageHandlerFunction().apply(message, logMessageTag);

        publishResponseWdsDoc(toWds, message.getMessageId(), logMessageTag, true, null, null, extraResponseParms);
    }

    @Override
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        throw new BadRequestException();
    }

    // HANDLER METHODS //

    private Map<String, Object> signup(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "signup";

        int contextId = getAppId(props, messageId, docType);

//shawker - 15 Oct 2018 - the server doesn't even have a way to support this. removing and replacing with a param
//        //see if there is a referrer (using the subdomain as the nickname, for example: darl.millionize.com would use the nickname darl to lookup the potential referrer)
//        Integer mintParentSubscriberId = null;
//        try {
//            if (props.containsKey("HEADER_Origin")) {
//                String potentialReferrerNickname = new URL(props.get("HEADER_Origin")).getHost().split("\\.")[0];
//                Subscriber referrer = _identityService.getSubscriberByNickname(contextId, potentialReferrerNickname);
//                if (referrer != null) {
//                    mintParentSubscriberId = referrer.getSubscriberId();
//_logger.info("setting mintParentSubscriberId to: " + mintParentSubscriberId);
//                }
//            } else {
//                _logger.info("no HEADER_Origin key found on signup request. unable to check for mint");
//            }
//        } catch (MalformedURLException e) {
//            _logger.warn("malformed URL on HEADER_Origin. unable to check for mint on signup request");
//        }

        Long mintParentSubscriberId = null;
        String potentialReferrerNickname = getParamFromProps(props, messageId, docType, "referrerNickname", false);
_logger.info(">>> received referrerNickname: " + potentialReferrerNickname);
        if (potentialReferrerNickname != null) {
            Subscriber referrer = _identityService.getSubscriberByNickname(contextId, potentialReferrerNickname);
            if (referrer != null) {
                mintParentSubscriberId = referrer.getSubscriberId();
_logger.info(">>> setting mintParentSubscriberId to: " + mintParentSubscriberId);
            }
else _logger.warn(">>> no subscriber found with that nickname; NOT setting referrer");
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        Subscriber subscriber = null;
        try {
            subscriber = this.persistSubscriberFromMap(contextId, messageId, docType, props, mintParentSubscriberId, false);

            // Once logged in, we can now get the session -- from this we need the SESSION_KEY
            SubscriberSession session = subscriber.getSubscriberSession();
            if (session == null) {
                _logger.error("subscriber had no session after persistSubscriberFromMap!");
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", "subscriber.getSubscriberSession returned null");
            }

            String encryptKey = props.get("__encryptKey");
            addEncryptedValueToResultMap(resultMap, encryptKey, "sessionKey", session.getSessionKey());
            addEncryptedValueToResultMap(resultMap, encryptKey, "subscriber", subscriber);

        } catch (InvalidSubscriberUpdateException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidSubscriberUpdate", e.getMessage());
        } catch (EmailAlreadyUsedException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "emailAlreadyUsed");
        } catch (InvalidEmailException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidEmail");
        } catch (NicknameAlreadyUsedException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "nicknameAlreadyUsed");
        } catch (NicknameInvalidException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "nicknameInvalid");
        } catch (InvalidSessionException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "subscriberNotFound", e.getMessage());
        } catch (InvalidParameterException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", e.getMessage());
        } catch (MissingRequiredParameterException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredParam", e.getMessage());
        }

//        try {
//            // Enqueue trigger to register the user created event.
//            String subscriberJson = _jsonMapper.writeValueAsString(subscriber);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_user_account_created,
//                    subscriberJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
//        }

        //do post-signup processing (such as setting notification prefs and push tokens)
//if (getAppById(contextId) == null) {
//    _logger.error("getAppById is null for contextId: " + contextId + ", not setting notification prefs");
//} else {
//        setNotificationPrefs(subscriber.getSubscriberId(), _shoutContestService.getAppById(contextId).getVipBoxPushType());
//}

        if (props.containsKey("deviceToken")) {
            setPushTokenRefactor(
                    subscriber.getSubscriberId(), subscriber.getSubscriberSession().getDeviceId(), subscriber.getSubscriberSession().getDeviceName(),
                    props.get("deviceToken"), _shoutContestService.getAppById(contextId),
                    props.get(PARM_TO_WDS), messageId, docType);
        }

        return resultMap;
    }

//not currently supported
//    private Map<String, Object> signupViaFacebook(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        int contextId = getAppId(props, messageId, "signupViaFacebook");
//        SubscriberSession session =  getUnauthenticatedSession(props);
//        String gameEngineName = "APP:" + contextId;
//
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        Subscriber subscriber = null;
//
//        String accessToken = getParamFromProps(props, messageId, "signupViaFacebook", "accessToken", true);
//        String facebookAppId = getParamFromProps(props, messageId, "signupViaFacebook", "facebookAppId", true);
//
//        SignupData signupData = new SignupData();
//        signupData.setFbAccessToken(accessToken);
//        signupData.setFacebookAppId(facebookAppId);
//
//        try {
//            subscriber = _identityService.signup(contextId, signupData, session);
//
//            String encryptKey = props.get("__encryptKey");
//            addEncryptedValueToResultMap(resultMap, encryptKey, "sessionKey", subscriber.getSubscriberSession().getSessionKey());
//            addEncryptedValueToResultMap(resultMap, encryptKey, "subscriber", subscriber);
//
//        } catch (Exception e) {
//            _logger.error("unable to signup via facebook", e);
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "signupViaFacebook", false, "failedAuth", e.getMessage());
//        }
//
//        try {
//            // Enqueue trigger to register the user created event.
//            String subscriberJson = _jsonMapper.writeValueAsString(subscriber);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_user_account_created,
//                    subscriberJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "signupViaFacebook", false, "unexpectedError", e.getMessage());
//        }
//
////        setNotificationPrefs(subscriber.getSubscriberId(), _shoutContestService.getAppById(contextId).getVipBoxPushType());
//
//        if (props.containsKey("deviceToken")) {
//            setPushTokenRefactor(
//                    subscriber.getSubscriberId(), subscriber.getSubscriberSession().getDeviceId(), subscriber.getSubscriberSession().getDeviceName(),
//                    props.get("deviceToken"), _shoutContestService.getAppById(contextId));
//        }
//
//        return resultMap;
//    }

//    //note: this method is duplicated from GamePlayService.SignupHelper. if you update it here, update it there
//    private void setNotificationPrefs(int subscriberId, String vipBoxPushType)
//    {
//        String notificationType = INotificationService.COMM_TYPE_APP_PUSH;
//        NotificationPref pref = new NotificationPref();
//        pref.setPrefType(INotificationService.PREF_TYPE_GENERAL);
//        pref.setSubscriberId(subscriberId);
//        pref.setValue(notificationType);
//        List<NotificationPref> prefs = new ArrayList<NotificationPref>();
//        prefs.add(pref);
//        pref = new NotificationPref();
//        pref.setPrefType(2);//IGameplayService.PREF_TYPE_GAMEPLAY);
//        pref.setSubscriberId(subscriberId);
//        pref.setValue(notificationType);
//        prefs.add(pref);
//        pref = new NotificationPref();
//        pref.setPrefType(3);//IGameplayService.PREF_TYPE_REWARDS);
//        pref.setSubscriberId(subscriberId);
//        pref.setValue(notificationType);
//        prefs.add(pref);
//        pref = new NotificationPref();
//        pref.setPrefType(4);//IGameplayService.PREF_TYPE_VIPBOX);
//        pref.setSubscriberId(subscriberId);
//        pref.setValue(vipBoxPushType);
//        prefs.add(pref);
//        try {
//            _notificationService.setPrefs(prefs);
//        } catch (InvalidPrefException e) {
//            _logger.warn("unable to set subscriber notification type prefs", e);
//        }
//    }

    private Map<String, Object> login(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "login";
        int contextId = getAppId(props, messageId, docType);

//        String gameEngineName = "APP:" + contextId;

        Map<String, Object> resultMap = new HashMap<String, Object>();
        Subscriber subscriber = null;
        String email = this.getParamFromProps(props, messageId, docType, "email", true);
        String password = this.getParamFromProps(props, messageId, docType, "password", true);
        String genLoginToken = this.getParamFromProps(props, messageId, docType, "genLoginToken", false);
        SubscriberSession session =  getUnauthenticatedSession(props, messageId, docType);
        try {
            subscriber = _identityService.authenticate(contextId, email, password, session);
            session = subscriber.getSubscriberSession();

            String encryptKey = props.get("__encryptKey");

            if (genLoginToken != null && genLoginToken.toLowerCase().equals("true")) {
                Map<String,String> loginTokenMap = new FastMap<>("emailSha256Hash", subscriber.getEmailSha256Hash(),
                                                                 "role", subscriber.getRole());
                String loginTokenJson;
                try {
                    loginTokenJson = _jsonMapper.writeValueAsString(loginTokenMap);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("unable to convert subscriber data to json", e);
                }
                String userData = null;
                try {
                    userData = new String(HexUtils.bytesToBase64Bytes(loginTokenJson.getBytes("UTF-8")), "UTF-8");
                } catch (UnsupportedEncodingException neverHappens) { }
                String userDataSignature;
                try {
                    userDataSignature = _encryptionService.rsaSign(loginTokenPrivateKey, userData, "UTF-8");
                } catch (GeneralSecurityException | IOException e) {
                    throw new IllegalStateException(e);
                }
                loginTokenMap = new FastMap<>("userData", userData, "signature", userDataSignature);
                String loginToken;
                try {
                    loginToken = _jsonMapper.writeValueAsString(loginTokenMap);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("unable to convert user-data to json", e);
                }

                addEncryptedValueToResultMap(resultMap, encryptKey, "loginToken", loginToken);
            }

            addEncryptedValueToResultMap(resultMap, encryptKey, "sessionKey", session.getSessionKey());
            addEncryptedValueToResultMap(resultMap, encryptKey, "subscriber", subscriber);

        } catch (InvalidEmailPasswordException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidLogin");
        } catch (SubscriberInactiveException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "accountDeactivated", e.getMessage());
        } catch (SubscriberRequiresEulaException e) {
            //this doesn't actually get thrown in the identity service code and will never happen
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "requiresEula");
        } catch (SubscriberRequiresUpdateException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "passwordChangeRequired");
        } catch (InvalidSessionException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "subscriberNotFound");
        }

//        try {
//            // Enqueue trigger to register the user logged in event.
//            String subscriberJson = _jsonMapper.writeValueAsString(subscriber);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_user_account_loggedin,
//                    subscriberJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
//        }

        if (props.containsKey("deviceToken")) {
            setPushTokenRefactor(
                    subscriber.getSubscriberId(), subscriber.getSubscriberSession().getDeviceId(), subscriber.getSubscriberSession().getDeviceName(),
                    props.get("deviceToken"), _shoutContestService.getAppById(contextId),
                    props.get(PARM_TO_WDS), messageId, docType);
        }

        return resultMap;
    }

//currently not supported
//    private Map<String, Object> loginViaFacebook(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        Subscriber subscriber = null;
//
//        int contextId = getAppId(props, messageId, "loginViaFacebook");
//        String gameEngineName = "APP:" + contextId;
//        SubscriberSession session =  getUnauthenticatedSession(props);
//
//        String accessToken = getParamFromProps(props, messageId, "loginViaFacebook", "accessToken", true);
//        String facebookAppId = getParamFromProps(props, messageId, "loginViaFacebook", "facebookAppId", true);
//
//        try {
//            subscriber = _identityService.authenticateViaFacebook(contextId, accessToken, facebookAppId, session);
//
//            String encryptKey = props.get("__encryptKey");
//            addEncryptedValueToResultMap(resultMap, encryptKey, "sessionKey", subscriber.getSubscriberSession().getSessionKey());
//            addEncryptedValueToResultMap(resultMap, encryptKey, "subscriber", subscriber);
//
//        } catch (SubscriberInactiveException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "loginViaFacebook", false, "accountDeactivated", e.getMessage());
//        } catch (SubscriberRequiresEulaException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "loginViaFacebook", false, "requiresEula", null);
//        } catch (SubscriberRequiresUpdateException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "loginViaFacebook", false, "passwordChangeRequired", null);
//        } catch (InvalidEmailPasswordException | InvalidSessionException | FacebookGeneralException | InvalidAccessTokenException | FacebookAuthenticationNeededException e) {
//            _logger.error("unable to login via facebook", e);
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "loginViaFacebook", false, "failedAuth", e.getMessage());
//        }
//
//        try {
//            // Enqueue trigger to register the user logged in event.
//            String subscriberJson = _jsonMapper.writeValueAsString(subscriber);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_user_account_loggedin,
//                    subscriberJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "loginViaFacebook", false, "unexpectedError", e.getMessage());
//        }
//
//        if (props.containsKey("deviceToken")) {
//            setPushTokenRefactor(
//                    subscriber.getSubscriberId(), subscriber.getSubscriberSession().getDeviceId(), subscriber.getSubscriberSession().getDeviceName(),
//                    props.get("deviceToken"), _shoutContestService.getAppById(contextId));
//        }
//
//        return resultMap;
//    }

    private Map<String, Map<String, String>> _cachedDbPhoneVerificationCodeMessages = new HashMap<>();

    private Map<String, Object> sendPhoneVerificationCode(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        String messageId = message.getMessageId();
        String docType = "sendPhoneVerificationCode";

        Subscriber s = getSubscriber(message, docType);
        String phone = getParamFromProps(props, messageId, docType, "phone", true);

        //generate a verification code
        String code = getVerificationCode(_verificationCodeLength);

        //store the code for later
        @SuppressWarnings("unchecked")
        Map<String, String> dbMessage = (Map<String, String>) wrapInTransaction(
            this::phoneVerificationTransactions, new Object[] {s, phone, code});
        String smsMessage = MessageFormat.format(LocalizationHelper.getLocalizedString(dbMessage, s.getLanguageCode()), code);

        //send the message
        TwilioRestClient client = new TwilioRestClient(_twilioAccountSid, _twilioAuthToken);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("From", _twilioFromNumber));
        params.add(new BasicNameValuePair("To", phone));
        params.add(new BasicNameValuePair("Body", smsMessage));
        MessageFactory messageFactory = client.getAccount().getMessageFactory();
        Message twilioMessage;
        try {
            twilioMessage = messageFactory.create(params);
        } catch (TwilioRestException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
        }
        String messageSid = twilioMessage.getSid();
        if (_logger.isDebugEnabled()) {
            _logger.debug("sent phone verification SMS. message SID: " + messageSid + ", phone#: " + phone + ", code: " + code);
        }

        return null;
    }

    private Map<String, String> phoneVerificationTransactions(Object param)
    {
        Object[] o = (Object[]) param;
        Subscriber s = (Subscriber) o[0];
        String phone = (String) o[1];
        String code = (String) o[2];

        Map<String, String> dbMessage;
        _dao.clearPhoneVerificationCodeForSubscriber(s.getSubscriberId());
        _dao.addPhoneVerificationCodeForSubscriber(s.getSubscriberId(), phone, code);

        //build up the message (check the cache first)
        dbMessage = _cachedDbPhoneVerificationCodeMessages.get(s.getLanguageCode());
        if (dbMessage == null) {
            dbMessage = ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(VERIFY_CODE_UUID, "systemMessage"));
            _cachedDbPhoneVerificationCodeMessages.put(s.getLanguageCode(), dbMessage);
        }

        return dbMessage;
    }

    private Map<String, Object> verifyPhoneCode(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        String messageId = message.getMessageId();
        String docType = "verifyPhoneCode";

        Subscriber s = getSubscriber(message, docType);
        String phone = getParamFromProps(props, messageId, docType, "phone", true);
        String code = getParamFromProps(props, messageId, docType, "code", true);

        Boolean codeValid = (Boolean) wrapInTransaction(this::verifyPhoneCodeTransaction, new Object[] {s, phone, code});
        if (!codeValid) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "codeInvalid");
        }

        //update the subscriber's phone# in the db
        _identityService.updatePhone(s.getSubscriberId(), phone);
        _identityService.markPhoneAsVerified(s.getSubscriberId());

        return null;
    }

    private Boolean verifyPhoneCodeTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        Subscriber s = (Subscriber) o[0];
        String phone = (String) o[1];
        String code = (String) o[2];

        //verify the code
        Date cutoffDate = new Date(System.currentTimeMillis() - (_verificationCodeDurationM * 60 * 1000));
        boolean isCodeValid = _dao.isPhoneVerificationCodeValidForSubscriber(s.getSubscriberId(), phone, code, cutoffDate);
        if (!isCodeValid) {
            return false;
        }

        //clear the code from the db
        _dao.clearPhoneVerificationCodeForSubscriber(s.getSubscriberId());

        return true;
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

    private Map<String, Object> setPushToken(CollectorMessage message)
    throws PublishResponseError
    {
        String docType = "setPushToken";

        Map<String, String> props = message.getProperties();
        int contextId = getAppId(props, message.getMessageId(), docType);
        String deviceToken = getParamFromProps(props, message.getMessageId(), docType, "deviceToken", true);

        Subscriber subscriber = getSubscriber(message, docType);

        setPushTokenRefactor(
            subscriber.getSubscriberId(), subscriber.getSubscriberSession().getDeviceId(), subscriber.getSubscriberSession().getDeviceName(),
            deviceToken, _shoutContestService.getAppById(contextId),
            props.get(PARM_TO_WDS), message.getMessageId(), docType);

        return null;
    }

    private Map<String, Object> requestPasswordReset(CollectorMessage message)
    throws PublishResponseError
    {
        String docType = "requestPasswordReset";
        Map<String, String> props = message.getProperties();
        int contextId = getAppId(props, message.getMessageId(), docType);

        String recipientEmail = getParamFromProps(props, message.getMessageId(), docType, "email", true);
        Subscriber recipientSubscriber = _identityService.getSubscriberByEmail(contextId, recipientEmail);
        if (recipientSubscriber == null) {
            _logger.warn("password reset requested for non existant email: " + recipientEmail);
            return null; //no error, just don't do anything
        }

        String languageCode = recipientSubscriber.getLanguageCode();

        String senderEmail = _pwResetEmail;
        String senderEmailName = _pwResetDisplay;
        String emailSubject = (String) wrapInTransaction(this::getPasswordResetSubjectViaTransaction, languageCode);
        String emailTemplateName = "snowyowl_pw_reset";

        String recipientEmailName = recipientSubscriber.getFirstname() + " " + recipientSubscriber.getLastname();
        if (StringUtil.isEmpty(recipientEmailName)) {
            recipientEmailName = null;
        }

        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.HOUR_OF_DAY, 1);
        String resetCode = _encryptionService.encryptValue(IShoutContestService.SERVICE_NAMESPACE, recipientEmail, expires.getTime());

        TemplateEmail email = new TemplateEmail(
            emailTemplateName,
            EmailPurpose.TRANSACTIONAL,
            new EmailAddress(senderEmail, senderEmailName),
            new EmailAddress(recipientEmail, recipientEmailName),
            emailSubject);

        //https://%{serverBaseUrl}/play/password-reset?prc=%{passwordResetCode}&cid=%{contextId}
        email.addVariable("serverBaseUrl", _serverBaseUrl);
        email.addVariable("passwordResetCode", resetCode);
        email.addVariable("contextId", contextId+"");

        try {
            _postOfficeService.sendTemplateEmail(email, contextId, languageCode);
        } catch (PostOfficeException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), docType, false, "unexpectedError", e.getMessage());
        }

        return null;
    }

    private String getPasswordResetSubjectViaTransaction(Object param)
    {
        String languageCode = (String) param;

        Map<String, String> values = ShoutContestService.tupleListToMap(_dao.getMultiLocalizationValues(EMAIL_SUBJECT_UUID, "systemMessage"));
        return LocalizationHelper.getLocalizedString(values, languageCode);
    }

    private Map<String, Object> resetPassword(CollectorMessage message)
    throws PublishResponseError
    {
        String docType = "resetPassword";
        Map<String, String> props = message.getProperties();

        int contextId = getAppId(props, message.getMessageId(), docType);
        String resetPasswordCode = getParamFromProps(props, message.getMessageId(), docType, "c", true);
        String newPassword = getParamFromProps(props, message.getMessageId(), docType, "newPassword", true);

        String subscriberEmail = _encryptionService.unencryptValue(IShoutContestService.SERVICE_NAMESPACE, resetPasswordCode);
        if (StringUtil.isEmpty(subscriberEmail)) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), docType, false, "invalidResetCode");
        }

        Subscriber subscriber = _identityService.getSubscriberByEmail(contextId, subscriberEmail);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), message.getMessageId(), docType, false, "subscriberNotFound");
        }

        //convert the plain text password into the proper format and save it
        String sha256OfPassword = HexUtils.stringToSha256HexString(newPassword, true);
        String scryptOfSha256OfPassword = _encryptionService.scryptEncode(sha256OfPassword);
        _identityService.setSubscriberPassword(IIdentityService.PASSWORD_SCHEME.USE_PASSWORD_AS_IS, subscriber.getSubscriberId(), scryptOfSha256OfPassword);

        //remove the reset request/code so it can't be used again
        _encryptionService.deleteOriginalValue(IShoutContestService.SERVICE_NAMESPACE, subscriberEmail);

        _logger.info("resetPassword successful for: " + subscriber.getSubscriberId());
        return null;
    }

    //this is a copy of the GamePlayHelper.setPushToken method. If you change this, change that as well
    private void setPushTokenRefactor(
        long subscriberId, String deviceId, String deviceName, String deviceToken, App app,
        String toWds, String messageId, String docType)
    throws PublishResponseError
    {
        if (deviceName != null && deviceToken != null)
        {
            // Update the user's device token in the push service.
            deviceName = deviceName.toLowerCase();
            String deviceType;
            String appBundleId;
            if (deviceName.contains("iphone") || deviceName.contains("ipad")) {
                deviceType = IPushService.DEVICE_TYPE_IOS;
                appBundleId = app.getiOSBundleId();
            } else if (deviceName.contains("windows")) {
                deviceType = IPushService.DEVICE_TYPE_WINDOWS_WNS;
                appBundleId = app.getWindowsBundleId();
                if (deviceToken.indexOf("{") != -1) {
                    _logger.info("invalid windows deviceToken. contained invalid characters intended for local loopback only; IGNORING: " + deviceToken);
                    return;
                }
            } else {
                deviceType = IPushService.DEVICE_TYPE_ANDROID_FCM;
                appBundleId = app.getAndroidBundleId();
            }
            _logger.info("*** registering push service token; deviceType: " + deviceType);
            _pushService.registerHexTokenForSubscriberAndDeviceType(deviceToken, subscriberId, deviceType, deviceId, appBundleId);
        }
        else {
            _logger.error(MessageFormat.format("*** not setting push token, deviceName {0} or deviceToken {1} is null", deviceName, deviceToken));
            throw new PublishResponseError(toWds, messageId, docType, false, deviceName == null ? "missingDeviceName" : "missingDeviceToken");
        }
    }

    private Map<String, Object> updateSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        int contextId = getAppId(props, messageId, "update");
//        String gameEngineName = "APP:" + contextId;

        Subscriber subscriber = null;
        try {
            subscriber = this.persistSubscriberFromMap(contextId, messageId, "update", props, null, true);
        } catch (InvalidSubscriberUpdateException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "invalidSubscriberUpdate", e.getMessage());
        } catch (EmailAlreadyUsedException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "emailAlreadyUsed", e.getMessage());
        } catch (InvalidEmailException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "invalidEmail", e.getMessage());
        } catch (NicknameAlreadyUsedException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "nicknameAlreadyUsed", e.getMessage());
        } catch (NicknameInvalidException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "nicknameInvalid", e.getMessage());
        } catch (InvalidSessionException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "subscriberNotFound", e.getMessage());
        } catch (InvalidParameterException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "invalidParam", e.getMessage());
        } catch (MissingRequiredParameterException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "missingRequiredParam", e.getMessage());
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();

//        try {
//            // Enqueue trigger to register the user updated event.
//            String subscriberJson = _jsonMapper.writeValueAsString(subscriber);
//            _triggerService.enqueue(
//                    IShoutContestAwardService.USER_EVENT_KEY_user_account_updated,
//                    subscriberJson,
//                    IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
//                    gameEngineName,
//                    _shoutContestService.getAppBundleIds(contextId),
//                    contextId);
//        } catch (JsonProcessingException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "update", false, "unexpectedError", e.getMessage());
//        }

        resultMap.put("subscriber", subscriber);

        return resultMap;
    }

    /*private Map<String, Object> getSubscriber(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        Subscriber subscriber = _subscriberUtil.getSubscriberFromSession(props);
        if (subscriber == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "getSubscriber", false, "unexpectedError", "subscriber not extractable from HTTP headers");
        }

        return new FastMap<>("subscriber", subscriber);
    }*/

    /**
     * URL: <pre>&lt;collector&gt;/getSubscriberEmailsAndAddresses</pre>
     *
     * PARAMS:
     * <ul>
     *   <li>toWds</li>
     * </ul>
     *
     * RESPONSE JSON (if successful):
     * <pre>
     * {
     *   "success": true,
     *   "emails": //may be null
     *   [
     *      {@link com.meinc.identity.domain.SubscriberEmail() }, ...
     *   ],
     *   "addresses": //may be null
     *   [
     *      {@link com.meinc.identity.domain.SubscriberAddress() }, ...
     *   ]
     * }
     * </pre>
     *
     * RESPONSE ERRORS:
     * <ul>
     *   <li>missingRequiredParam</li>
     *   <li>unexpectedError</li>
     * </ul>
     *
     * @param props
     * @param messageId
     * @return
     * @throws PublishResponseError
     */
    private Map<String, Object> getSubscriberEmailsAndAddresses(CollectorMessage message)
    throws PublishResponseError
    {
        Subscriber subscriber = getSubscriber(message, "getSubscriberEmailsAndAddresses");

        List<SubscriberEmail> emails = _identityService.getSubscriberEmails(subscriber.getSubscriberId());
        List<SubscriberAddress> addresses = _identityService.getSubscriberAddresses(subscriber.getSubscriberId());

        return new FastMap<>("emails", emails, "addresses", addresses);
    }

    private Map<String, Object> addSubscriberAddress(CollectorMessage message)
    throws PublishResponseError
    {
        Map<String, String> props = message.getProperties();
        Subscriber subscriber = getSubscriber(message, "addSubscriberAddress");

        SubscriberAddress addr = getJsonObjectFromProps(props, message.getMessageId(), "addSubscriberAddress", "address", true, new TypeReference<SubscriberAddress>(){});
        addr.setSubscriberId(subscriber.getSubscriberId());

        int addressId = _identityService.setSubscriberAddress(addr);

        return new FastMap<>("addressId", addressId);
    }

    private Map<String, Object> getPublicProfile(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "getPublicProfile";

        String val = getParamFromProps(props, messageId, docType, "subscriberIds", true);
        int[] subscriberIds;
        try {
            subscriberIds = Arrays.asList(val.split(",")).stream()
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
        } catch (NumberFormatException e) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "invalidParam", "subscriberIds");
        }

        List<Map<String, Object>> profiles = new ArrayList<>(subscriberIds.length);

        for (int subscriberId : subscriberIds) {
            Subscriber s = _identityService.getSubscriberById(subscriberId);
            if (s == null) {
                throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "subscriberNotFound", subscriberId+"");
            }

            Map<String, Object> profileData = new HashMap<>(8);
            profileData.put("subscriberId", s.getSubscriberId());
            profileData.put("nickname", s.getNickname());
            profileData.put("countryCode", s.getFromCountryCode());
            profileData.put("languageCode", s.getLanguageCode());
            profileData.put("photoUrl", s.getPhotoUrl());
            profileData.put("photoUrlLarge", s.getPhotoUrlLarge());
            profileData.put("photoUrlSmall", s.getPhotoUrlSmall());
            profileData.put("primaryIdHash", s.getEmailSha256Hash());

            profiles.add(profileData);
        }


        return new FastMap<>("profiles", profiles);
    }

//    /**
//     * URL: <pre>&lt;collector&gt;/changePassword</pre>
//     *
//     * PARAMS:
//     * <ul>
//     *   <li>toWds</li>
//     *   <li>email: if isOverride AND requester is ADMIN email is target subscriber, else target is the verified session subscriber.</li>
//     *   <li>oldPassword: SHA256 of existing password</li>
//     *   <li>newPassword: SHA256 *AND* sCrypted version of new password</li>
//     *   <li>isOverride: Boolean, ADMIN role can reset password of any user. If isOverride == true, the subscriber associated with the email will have their password set to "password" (SHA256 + sCrypted) </li>
//     * </ul>
//     *
//     * RESPONSE JSON (if successful):
//     * <pre>
//     * {
//     *   "success": true
//     * }
//     * </pre>
//     *
//     * RESPONSE ERRORS:
//     * <ul>
//     *   <li>missingRequiredParam</li>
//     *   <li>invalidParam</li>
//     *   <li>unexpectedError</li>
//     * </ul>
//     *
//     * @param props
//     * @param messageId
//     * @return
//     * @throws PublishResponseError
//     */
//    private Map<String, Object> changePassword(Map<String, String> props, String messageId)
//    throws PublishResponseError
//    {
//        Subscriber subscriber = _subscriberUtil.getSubscriberFromSession(props);
//        if (subscriber == null) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "unexpectedError", "subscriber not extractable from HTTP headers");
//        }
//
//        String toWds = props.get("toWds");
//        Integer contextId  = _shoutContestService.getContextId(defaultContextId, props);
//        String email       = this.getParamFromProps(props, messageId, "changePassword", "email", true);
//        String oldPassword = this.getParamFromProps(props, messageId, "changePassword", "oldPassword", true);
//        String newPassword = this.getParamFromProps(props, messageId, "changePassword", "newPassword", true);
//        boolean isOverride = Boolean.parseBoolean(this.getParamFromProps(props, messageId, "changePassword", "isOverride", false));
//        if (oldPassword.equals(newPassword)) {
//            throw new PublishResponseError(toWds, messageId, "changePassword", false, "invalidParam", "oldPassword matches newPassword");
//        }
//        if (!newPassword.startsWith("$") && !isOverride){
//            throw new PublishResponseError(toWds, messageId, "changePassword", false, "invalidParam", "newPassword must be SHA256 and sCrypted.");
//        }
//        try {
//            //
//            // First authenticate the user using the old password
//            // If override mode: use the subcriber of the email as the reset subscriber
//            //                   force password = "password" that has been SHA256'd and sCrypted
//            //
//            if (subscriber.getRole().equals(ROLE.ADMIN) && isOverride){
//                _logger.info("changePassword with override: _identityService.getSubscriberByEmail: " + email);
//                subscriber = _identityService.getSubscriberByEmail(contextId, email);
//                newPassword = "$s0$f0801$fuYT08Nz78UrE0Lg4wGP7yttPi+iPp+/funYnkDexg95Am6YZsoj6E9qB9gbybkWVKnsJPJstEZjL98Z2gKGwA==$jGWMXp9gbFhYpm7Irzr/RupXVN5GaMe4Bjfqo5r3xmu8AsjbcJcbba5dxjGWktX2+20vc6rqIjLh9pERhsrm9A==";
//                if (subscriber == null){
//                    throw new PublishResponseError(toWds, messageId, "changePassword", false, "invalidParam", "invalid email.");
//                }
//            }else{
//                _logger.info("changePassword: _identityService.authenticate: " + email);
//                SubscriberSession session =  getUnauthenticatedSession(props);
//                _identityService.authenticate(contextId, email, oldPassword, session);
//            }
//
//            //
//            // Now update the password -- it MUST be SHA256 and sCrypted
//            //
//            _identityService.setSubscriberPassword(PASSWORD_SCHEME.USE_PASSWORD_AS_IS , subscriber.getSubscriberId(), newPassword);
//
//        } catch (InvalidEmailPasswordException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "invalidLogin", null);
//        } catch (SubscriberInactiveException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "accountDeactivated", e.getMessage());
//        } catch (SubscriberRequiresEulaException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "requiresEula", null);
//        } catch (SubscriberRequiresUpdateException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "passwordChangeRequired", null);
//        } catch (InvalidSessionException e) {
//            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, "changePassword", false, "subscriberNotFound", null);
//        }
//
//        return new HashMap<String, Object>(); // Empty map == success:true
//
//    }

    private Map<String, Object> checkUsername(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String username = getParamFromProps(props, messageId, "checkUsername", "username", true);

        List<String> partialMatches = _identityService.findPartialNicknameMatches(username.toLowerCase());

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("usernameRequested", username);

        if (partialMatches == null || partialMatches.size() == 0) {
            response.put("usernameIsUnique", true);
        } else {
            //quick lookup
            Set<String> usernameSet = new HashSet<String>();
            for (String un : partialMatches) {
                usernameSet.add(un.toLowerCase());
            }

            //is there an exact match?
            if (!usernameSet.contains(username.toLowerCase())) {
                response.put("usernameIsUnique", true);

            } else {
                //yes there is - so ...
                //generate a suggested username
                //for now this is a simple algorithm: just add a number on the end until it's unique. in the future this can be made more intelligent

                response.put("usernameIsUnique", false);

                int count = 1;

                String suggestedUsername = null;
                while (true) {
                    suggestedUsername = username.toLowerCase() + count;
                    if (usernameSet.contains(suggestedUsername)) {
                        count++;
                    } else {
                        break;
                    }
                }

                response.put("suggestedUsername", suggestedUsername);
            }
        }

        return response;
    }

    // HELPER METHODS //

    private Subscriber persistSubscriberFromMap(int contextId, String messageId, String docType, Map<String, String> map, Long mintParentSubscriberId, boolean isUpdatingSubscriber)
        throws InvalidSubscriberUpdateException, EmailAlreadyUsedException, InvalidEmailException, NicknameAlreadyUsedException, NicknameInvalidException,
        InvalidSessionException, InvalidParameterException, MissingRequiredParameterException,
        PublishResponseError
    {
        // Determine if we already know about this subscriber
        Subscriber existingSubscriber = null;

        if (isUpdatingSubscriber) {
            if (existingSubscriber == null && map.containsKey("email") && map.get("email") != null) {
    //            _logger.info("persistSubscriberFromMap: looking up subscriber via email: " + map.get("email"));
                existingSubscriber = _identityService.getSubscriberByEmail(contextId, map.get("email"));
            }
            if (existingSubscriber == null && map.containsKey("phone") && map.get("phone") != null) {
    //            _logger.info("persistSubscriberFromMap: looking up subscriber via phone: " + map.get("phone"));
                existingSubscriber = _identityService.getSubscriberByPhone(map.get("phone"));
            }
            if (existingSubscriber == null && map.containsKey("nickname") && map.get("nickname") != null) {
    //            _logger.info("persistSubscriberFromMap: looking up subscriber via nickname: " + map.get("nickname"));
                existingSubscriber = _identityService.getSubscriberByNickname(contextId, map.get("nickname"));
            }
            if (existingSubscriber == null) {
                // Changing their email, and nickName and phone, but using their Session_Key
    //            _logger.info("persistSubscriberFromMap: looking up subscriber via session");
                existingSubscriber = _subscriberUtil.getSubscriberFromSession(map).subscriber;

            }
            if (existingSubscriber == null) {
                throw new InvalidSessionException("Subscriber not found for update."); //invalidsessionexception gets exposed to the user as "subscriberNotFound"
            }
        }

        //regardless of insert or update, the photo URL handling is the same
        String photoUrl = null;
        if (map.containsKey("photoUrl")) {
            photoUrl = map.get("photoUrl");
        }
        String photoLargeThumb = null;
        String photoSmallThumb = null;
        if (photoUrl != null) {
            List<String> thumbs = handleImageUpload(map, messageId, docType);
            photoLargeThumb = thumbs.get(0);
            photoSmallThumb = thumbs.get(1);
        }

        //we can't find this subscriber, so we must assume that it is someone new
        if (existingSubscriber == null) {
//_logger.info("persistSubscriberFromMap: subscriber not found; creating a new one...");

            //break fullname into parts and set first and last separately
            String firstName=null, lastName=null;
            if (map.containsKey("fullName")) {
                firstName = map.get("fullName");
                if (firstName != null && firstName.contains(" ")){
                    lastName = firstName.substring(firstName.indexOf(" ") + 1);
                    firstName = firstName.substring(0, firstName.indexOf(" "));
                }
            } else {
                firstName = map.get("firstName");
                lastName = map.get("lastName");
            }

//per bruce (20 Jul 2018) - do NOT (yet) require first/last name on signup (@see DM-192)
//            if (StringUtil.isEmpty(firstName)) {
//                throw new MissingRequiredParameterException("firstName");
//            }
//            if (StringUtil.isEmpty(lastName)) {
//                throw new MissingRequiredParameterException("lastName");
//            }

            String email = map.get("email");
            if (StringUtil.isEmpty(email)) {
                throw new MissingRequiredParameterException("email");
            }
            String username = map.get("nickname");
            if (StringUtil.isEmpty(username)) {
                throw new MissingRequiredParameterException("nickname");
            }

            Date dob = null;
            dob = DateUtil.iso8601ToDate(map.get("birthDate"));
            if (dob == null) {
                throw new MissingRequiredParameterException("birthDate");
            }

            String countryCode = map.get("countryCode");
            if (StringUtil.isEmpty(countryCode)) {
                countryCode = "US"; //via DM-192: inferred as US for now
            }

            boolean isAdult = map.containsKey("isAdult") ? Boolean.parseBoolean(map.get("isAdult")) : false;
            if (!isAdult) {
                throw new InvalidSubscriberUpdateException("isAdult");
            }

            String region = map.get("region");
            if (StringUtil.isEmpty(region)) {
                throw new MissingRequiredParameterException("region");
            }
            //make sure the region is known/supported
            Integer minAgeForRegion = getMinAgeForRegion(countryCode, region);
            if (minAgeForRegion == null) {
                throw new InvalidParameterException("countryCode/region");
            }

            //even though they've said they're an adult, check the lookup table against their DOB to make sure they are the correct age for the given region
            int age = DateUtil.getAge(dob);
            if (age < minAgeForRegion) {
                throw new InvalidSubscriberUpdateException("birthDate");
            }

            SubscriberSession session = getUnauthenticatedSession(map, messageId, docType);
            session.setContextId(contextId);
            SignupData signupData = new SignupData();
            signupData.setAppName("ShoutMillionaire");
            signupData.setDeviceToken(session.getDeviceId());
            signupData.setFirstName(firstName);
            signupData.setLastName(lastName);
            signupData.setUsername(username);
            signupData.setPassword(map.get("password"));
            signupData.setPasswordSet(true);
            signupData.setEmail(email);
            signupData.setPhone(map.get("phone"));
            signupData.setPhotoUrl(photoUrl);
            signupData.setPhotoUrlLarge(photoLargeThumb);
            signupData.setPhotoUrlSmall(photoSmallThumb);
            signupData.setDateOfBirth(dob);
            signupData.setLanguageCode(map.get("languageCode"));
            signupData.setFromCountryCode(countryCode);
            signupData.setRegion(region);
            signupData.setAdult(isAdult);

            existingSubscriber = _identityService.signupForeignHostSubscriber(contextId, session, signupData);
//            _logger.info("persistSubscriberFromMap: newly added subscriber, session: " + existingSubscriber.getSubscriberSession());

            //set the mint parent, if there is one
            if (mintParentSubscriberId != null) {
                _identityService.setSubscriberMintParentId(existingSubscriber.getSubscriberId(), mintParentSubscriberId);
            }

        } else {
//            _logger.debug("persistSubscriberFromMap: subscriber found; doing an update...");

            // Cannot update a subscriber when trying to Signup!!!
            if (!isUpdatingSubscriber){
                String errorMessage = MessageFormat.format("persistSubscriberFromMap from signup forbidden. Subscriber already exists, email:{0}, hash:{1}", existingSubscriber.getEmail(), existingSubscriber.getEmailSha256Hash());
                _logger.error(errorMessage);
                throw new PublishResponseError(map.get(PARM_TO_WDS), messageId, docType, false, "emailAlreadyUsed");
            }

            if (existingSubscriber.getContextId() != contextId) {
                //there is some type of discrepancy. bail out!
                _logger.error("client passed contextId: " + contextId + ", but located subscriber is in contextId: " + existingSubscriber.getContextId());
                throw new PublishResponseError(map.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", "given contextId does not match found contextId");
            }

            SubscriberSession session = getUnauthenticatedSession(map, messageId, docType);
            session = _identityService.getSubscriberSession(existingSubscriber.getSubscriberId(), session.getDeviceId());
            if (session == null){
//                _logger.info("persistSubscriberFromMap: session not found; adding a new session");
                session = getUnauthenticatedSession(map, messageId, docType);
                session.setSessionKey(UUID.randomUUID().toString());
                session.setAddedDate(new Date());
                session.setSubscriberId(existingSubscriber.getSubscriberId());
                session.setLastAuthenticatedDate(new Date());
                session.setContextId(contextId);
                _identityService.addSubscriberSession(session);
            }

            String firstName=null, lastName = null;
            if (map.containsKey("fullName")) {
                firstName = map.get("fullName");
                if (firstName != null && firstName.contains(" ")){
                    lastName = firstName.substring(firstName.indexOf(" ") + 1);
                    firstName = firstName.substring(0, firstName.indexOf(" "));
                }
                if (firstName == null) firstName = "";
                if (lastName == null) lastName = "";
            }

            if (map.containsKey("firstName")) {
                firstName = map.get("firstName");
            }

            if (map.containsKey("lastName")) {
                lastName = map.get("lastName");
            }

            boolean mainSubDataUpdated = false;
            boolean setNickname = false;
            boolean setPassword = false;
            boolean countryAndOrRegionAndOrDobUpdated = false;

            existingSubscriber.setSubscriberSession(session);
            if (firstName != null) {
                mainSubDataUpdated = true;
//_logger.info("setting firstName to: " + firstName);
                existingSubscriber.setFirstname(firstName);
            }
            if (lastName != null) {
                mainSubDataUpdated = true;
//_logger.info("setting lastName to: " + lastName);
                existingSubscriber.setLastname(lastName);
            }
            if (map.containsKey("email")) {
                mainSubDataUpdated = true;
                existingSubscriber.setEmail(map.get("email"));
            }
            if (map.containsKey("nickname")) {
                setNickname = true;
            }
            if (map.containsKey("password")) {
                setPassword = true;
            }
            if (photoUrl != null) {
                mainSubDataUpdated = true;
                existingSubscriber.setPhotoUrl(photoUrl);
            }
            if (photoLargeThumb != null) {
                mainSubDataUpdated = true;
                existingSubscriber.setPhotoUrlLarge(photoLargeThumb);
            }
            if (photoSmallThumb != null) {
                mainSubDataUpdated = true;
                existingSubscriber.setPhotoUrlSmall(photoSmallThumb);
            }
            if (map.containsKey("languageCode")) {
                mainSubDataUpdated = true;
                existingSubscriber.setLanguageCode(map.get("languageCode"));
            }
            if (map.containsKey("countryCode")) {
                mainSubDataUpdated = true;
                countryAndOrRegionAndOrDobUpdated = true;
                existingSubscriber.setFromCountryCode(map.get("countryCode"));
            }
            if (map.containsKey("phone")) {
                mainSubDataUpdated = true;
                existingSubscriber.setPhone(map.get("phone"));
            }
            if (map.containsKey("birthDate")) {
                mainSubDataUpdated = true;
                countryAndOrRegionAndOrDobUpdated = true;
                Date dob = null;
                dob = DateUtil.iso8601ToDate(map.get("birthDate"));
                existingSubscriber.setDateOfBirth(dob);
            }

            if ( map.containsKey("isAdult")) {
                boolean isAdult = map.containsKey("isAdult") ? Boolean.parseBoolean(map.get("isAdult")) : false;
                if (!isAdult) {
                    throw new InvalidParameterException("isAdult");
                } else {
                    mainSubDataUpdated = true;
                    existingSubscriber.setAdultFlag(true);
                }
            }

            if (map.containsKey("region")) {
                mainSubDataUpdated = true;
                countryAndOrRegionAndOrDobUpdated = true;
                existingSubscriber.setRegion(map.get("region"));
            }

            if (countryAndOrRegionAndOrDobUpdated) {
                //make  sure the country/region combo is valid and that their age is valid
                Integer minAgeForRegion = getMinAgeForRegion(existingSubscriber.getFromCountryCode(), existingSubscriber.getRegion());
                if (minAgeForRegion == null) {
                    throw new InvalidParameterException("countryCode/region");
                }

                //even though they've said they're an adult, check the lookup table against their DOB to make sure they are the correct age for the given region
                int age = DateUtil.getAge(existingSubscriber.getDateOfBirth());
                if (age < minAgeForRegion) {
                    throw new InvalidSubscriberUpdateException("birthDate");
                }
            }

            if (mainSubDataUpdated) {
//_logger.info("updating main subscriber data");
                _identityService.updateSubscriber(existingSubscriber);
            }

            if (setNickname) {
//_logger.info("setting nickname to: " + map.get("nickName") + ", contextId: " + contextId);
                existingSubscriber.setNickname(map.get("nickname"));
                _identityService.setSubscriberNickname(contextId, existingSubscriber.getSubscriberId(), map.get("nickname"));
            }

            if (setPassword) {
//_logger.info("setting new password for " + existingSubscriber.getSubscriberId());
                existingSubscriber.setPasswd(map.get("password"));
                _identityService.setSubscriberPassword(PASSWORD_SCHEME.USE_PASSWORD_AS_IS, existingSubscriber.getSubscriberId(), (map.get("password")));
            }

//_logger.info("persistSubscriberFromMap: updated subscriber, session: " + existingSubscriber.getSubscriberSession());

            //paypal email
            if (map.containsKey("paypalEmail")) {
                SubscriberEmail paypalEmail = new SubscriberEmail();
                paypalEmail.setEmailType(SubscriberEmail.EMAIL_TYPE.PAYPAL);
                paypalEmail.setSubscriberId(existingSubscriber.getSubscriberId());
                paypalEmail.setEmail(map.get("paypalEmail"));
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("adding paypal email {0} for subscriber {1}", map.get("paypalEmail"), existingSubscriber.getSubscriberId()));
                }
                _identityService.addSubscriberEmail(paypalEmail);
            }

            //home address
            if (map.containsKey("homeAddrLine1")) {
                SubscriberAddress homeAddr = new SubscriberAddress();
                homeAddr.setSubscriberId(existingSubscriber.getSubscriberId());
                homeAddr.setType(ADDRESS_TYPE.HOME);
                homeAddr.setAddr1(map.get("homeAddrLine1"));
                if (map.containsKey("homeAddrLine2")) homeAddr.setAddr2(map.get("homeAddrLine2"));
                if (!map.containsKey("homeAddrCity")) throw new MissingRequiredParameterException("homeAddrCity");
                homeAddr.setCity(map.get("homeAddrCity"));
                if (!map.containsKey("homeAddrPostalCode")) throw new MissingRequiredParameterException("homeAddrPostalCode");
                homeAddr.setZip(map.get("homeAddrPostalCode"));
                if (!map.containsKey("homeAddrStateProvince")) throw new MissingRequiredParameterException("homeAddrStateProvince");
                homeAddr.setState(map.get("homeAddrStateProvince"));
                if (!map.containsKey("homeAddrCountryCode")) throw new MissingRequiredParameterException("homeAddrCountryCode");
                homeAddr.setCountryCode(map.get("homeAddrCountryCode"));

                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("adding home addr {0} for subscriber {1}", map.get("homeAddrLine1"), existingSubscriber.getSubscriberId()));
                }
                _identityService.setSubscriberAddress(homeAddr);
            }
        }

        return existingSubscriber;
    }

    private Integer getMinAgeForRegion(String countryCode, String region)
    {
        return (Integer) wrapInTransaction(this::getMinAgeForRegionTransaction, new Object[] {countryCode, region});
    }

    private Integer getMinAgeForRegionTransaction(Object param)
    {
        Object[] o = (Object[]) param;
        String countryCode = (String) o[0];
        String region = (String) o[1];

        return _dao.getMinAgeForRegion(countryCode, region);
    }

    private void addEncryptedValueToResultMap(Map<String, Object> resultMap, String encryptKey, String resultKey, Object resultValue)
    {
        String plainTextMessage;
        if (resultValue instanceof String) {
            plainTextMessage = (String) resultValue;

        } else if (resultValue instanceof Subscriber) {
            try {
                plainTextMessage = _jsonMapper.writeValueAsString(resultValue);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("unable to convert subscriber to json", e);
            }

        } else {
            throw new IllegalArgumentException("only String and Subscriber supported. resultValue is of type: " + resultValue.getClass().getName());
        }

        String key = encryptKey.substring(0,16);
        String initVector = encryptKey.substring(16);
        String encryptedValueAsStr = _shoutContestService.aesEncrypt(key, initVector, plainTextMessage);

        resultMap.put(resultKey, encryptedValueAsStr);
    }

    private int getAppId(Map<String, String> props, String messageId, String logTag)
    {
        if (!props.containsKey("appId")) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, logTag, false, "missingRequiredParam", "appId");
        }
        Integer contextId = _shoutContestService.getContextId(props);
        if (contextId == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, logTag, false, "invalidParam", "appId");
        }

        return contextId;
    }

    /**
     *
     * @param map
     * @param messageId
     * @param docType
     * @return idx 0 = largeThumb, idx 1 = smallThumb
     * @throws PublishResponseError
     */
    private List<String> handleImageUpload(Map<String, String> map, String messageId, String docType)
    throws PublishResponseError
    {
        String photoUrl = map.get("photoUrl");
        String photoLargeThumb = null;
        String photoSmallThumb = null;
        if (photoUrl != null) {
            try {
                //calculate the large and small thumbnail URL's
                URL url = new URL(photoUrl);
                int idx = url.getPath().lastIndexOf(".");
                String filetype = url.getPath().substring(idx+1);
                String filename = url.getPath().substring(0, idx);
                URL largeThumbUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), filename + ".512." + filetype);
                URL smallThumbUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), filename + ".256." + filetype);
                photoLargeThumb = largeThumbUrl.toExternalForm();
                photoSmallThumb = smallThumbUrl.toExternalForm();

                //create the thumbnails and move the images from the sandbox to the public web directory
                WebDataStoreObject.Endpoint e = new WebDataStoreObject.Endpoint(url.getHost(), Root.USER_UPLOAD, url.getPath());
                _wmsService.operateObjectSync(
                    e,
                    new WebDataStoreObjectOperation.CreateThumbnailOperation(1024, 1024, Root.WWW, largeThumbUrl.getPath()),
                    new WebDataStoreObjectOperation.CreateThumbnailOperation(512, 512, Root.WWW, smallThumbUrl.getPath()),
                    new WebDataStoreObjectOperation.ResizeOperation(2048, 2048),
                    new WebDataStoreObjectOperation.StripOperation(),
                    new WebDataStoreObjectOperation.SetRootOperation(Root.WWW)
                );

            } catch (MalformedURLException e) {
                _logger.warn("unable to parse image url: " + photoUrl, e);
                throw new PublishResponseError(map.get(PARM_TO_WDS), messageId, docType, false, "malformedUrl", photoUrl);
            } catch (WebDataStoreException e) {
                _logger.warn("unexpected exception while parsing images", e);
                throw new PublishResponseError(map.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
            } catch (InterruptedException e) {
                _logger.warn("unexpected exception while parsing images", e);
                throw new PublishResponseError(map.get(PARM_TO_WDS), messageId, docType, false, "unexpectedError", e.getMessage());
            }
        }

        return Arrays.asList(photoLargeThumb, photoSmallThumb);
    }

    public class InvalidParameterException
    extends Exception
    {
        private static final long serialVersionUID = 1L;
        public InvalidParameterException(){}
        public InvalidParameterException(String message)
        {
            super(message);
        }
    }

    private Subscriber getSubscriber(CollectorMessage message, String logTag)
    {
        Map<String, String> props = message.getProperties();
        Subscriber subscriber;
        SubscriberUtil.SubscriberResponse response = _subscriberUtil.getSubscriberFromSession(props);
        if (response.subscriber == null) {
            throw new PublishResponseError(
                props.get(PARM_TO_WDS),
                message.getMessageId(),
                logTag,
                false,
                "subscriberNotFound",
                response.noSubscriberReason.toString()
            );
        } else {
            subscriber = response.subscriber;
        }

        return subscriber;
    }

    private SubscriberSession getUnauthenticatedSession(Map<String, String> props, String messageId, String docType)
    throws PublishResponseError
    {
        SubscriberSession session = _subscriberUtil.getUnauthenticatedSession(props);

        if (session.getDeviceId() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", SubscriberUtil.X_REST_DEVICE_ID);
        } else if (session.getAppId() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", SubscriberUtil.X_REST_APPLICATION_ID);
        } else if (session.getAppVersion() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", SubscriberUtil.X_REST_APPLICATION_VERSION);
        } else if (session.getDeviceModel() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", "deviceModel");
        } else if (session.getDeviceName() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", "deviceName");
        } else if (session.getDeviceVersion() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", "deviceVersion");
        } else if (session.getOsName() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", "deviceOsName");
        } else if (session.getOsType() == null) {
            throw new PublishResponseError(props.get(PARM_TO_WDS), messageId, docType, false, "missingRequiredHeader", "deviceOsType");
        }

        return session;
    }
}
