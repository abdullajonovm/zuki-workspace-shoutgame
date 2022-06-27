package com.meinc.push.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.api.client.http.HttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.push.dao.IPushServiceDao;
import com.meinc.push.domain.SubscriberToken;
import com.meinc.push.exception.PayloadInvalidException;
import com.meinc.push.exception.PayloadTooLargeException;
import com.meinc.push.exception.PushNetworkIoException;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.ReconnectPolicy;

import tv.shout.util.JsonUtil;

@Service(name = PushService.SERVICE_NAME, interfaces = PushService.PUSH_INTERFACE, version = "1.0", exposeAs = IPushService.class)
public class PushService
implements IPushService, ApnsDelegate, GcmDelegate, WnsDelegate
{
    private static final int MAX_PAYLOAD_SIZE = 2048;

    public static final String SERVICE_NAME = "PushService";
    public static final String PUSH_INTERFACE = "IPushService";
    public static final String CONFIG_TYPE_SANDBOX = "sandbox";
    public static final String CONFIG_TYPE_PRODUCTION = "production";
    public static final String TOKEN_TEST_PREFIX = "_test_";
    private static Logger _logger = Logger.getLogger(PushService.class);
    private IPushServiceDao _dao;
    private String _sandboxKeyFilename;
    private String _sandboxKeyPassword;
    private int _sandboxNumConnections;
    private String _prodKeyFilename;
    private String _prodKeyPassword;
    private int _prodNumConnections;
    protected Thread _inactiveChecker;
    private boolean _running;
    private long _inactiveCheckInterval;
    private String _fakeToken;
    private PlatformTransactionManager _transactionManager;
    private ConcurrentHashMap<String, ApnsService> _apnsByAppBundleId;
    private ConcurrentHashMap<String, GcmPushHandler> _gcmByAppBundleId;
    private ConcurrentHashMap<String, WnsPushHandler> _wnsByAppBundleId;
//    private ITriggerService _triggerService;
    private RmiPhoneServer _rmiPushServer;
    private String _configType;
    private String _rmiDeviceTokenPrefix;
    private Timer _updateTimer;
    private FcmPushHandler _fcm;

    public IPushServiceDao getDao() {
        return _dao;
    }

    public void setDao(IPushServiceDao dao) {
        _dao = dao;
    }

//    public void setTriggerService(ITriggerService service)
//    {
//        _triggerService = service;
//    }

    public String getSandboxKeyFilename() {
        return _sandboxKeyFilename;
    }

    public void setSandboxKeyFilename(String sandboxKeyFilename) {
        _sandboxKeyFilename = sandboxKeyFilename;
    }

    public void setSandboxKeyPassword(String keyPassword) {
        _sandboxKeyPassword = keyPassword;
    }

    public String getSandboxKeyPassword() {
        return _sandboxKeyPassword;
    }

    public void setSandboxNumConnections(int numConnections) {
        _sandboxNumConnections = numConnections;
    }

    public int getSandboxNumConnections() {
        return _sandboxNumConnections;
    }

    public String getProdKeyFilename() {
        return _prodKeyFilename;
    }

    public void setProdKeyFilename(String prodKeyFilename) {
        _prodKeyFilename = prodKeyFilename;
    }

    public String getProdKeyPassword() {
        return _prodKeyPassword;
    }

    public void setProdKeyPassword(String prodKeyPassword) {
        _prodKeyPassword = prodKeyPassword;
    }

    public void setConfigType(String configType)
    {
        _configType = configType;
    }

    public int getProdNumConnections() {
        return _prodNumConnections;
    }

    public void setProdNumConnections(int prodNumConnections) {
        _prodNumConnections = prodNumConnections;
    }

    public void setInactiveCheckInterval(long inactiveCheckInterval) {
        _inactiveCheckInterval = inactiveCheckInterval;
    }

    public long getInactiveCheckInterval() {
        return _inactiveCheckInterval;
    }

    public String getFakeToken() {
        return _fakeToken;
    }

    public void setFakeToken(String fakeToken) {
        _fakeToken = fakeToken;
    }

    private static final char base[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private static String encodeHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; ++i) {
            int b = (bytes[i]) & 0xFF;
            chars[2 * i] = base[b >>> 4];
            chars[2 * i + 1] = base[b & 0xF];
        }

        return new String(chars);
    }

    private ApnsService apnsByAppBundleId(String bundleId) {
        if (bundleId == null || bundleId.trim().length() == 0) {
            bundleId = "com.meinc.shout";
        }
        ApnsService apns = null;
        apns = _apnsByAppBundleId.get(bundleId);
        return apns;
    }

    private static java.util.logging.Logger julHttpTransportLogger;

    @Override
    @ServiceMethod
    @OnStart
    public void load()
    throws FileNotFoundException
    {
        //turn down the logging for google's http transport (used when sending pushes, for example)
        julHttpTransportLogger = java.util.logging.Logger.getLogger(HttpTransport.class.getName());
        julHttpTransportLogger.setLevel(Level.WARNING);

        _updateTimer = new Timer(false);
        boolean enableRmi = Boolean.valueOf(ServerPropertyHolder.getProperty("push.service.rmi.enabled", "false").trim().toLowerCase());
        if (enableRmi) {
            _rmiPushServer = new RmiPhoneServer();
        }
        _rmiDeviceTokenPrefix = ServerPropertyHolder.getProperty("push.service.rmi.device.token.prefix", "19992").trim();

        _apnsByAppBundleId = new ConcurrentHashMap<String, ApnsService>();
        _gcmByAppBundleId = new ConcurrentHashMap<String, GcmPushHandler>();
        _wnsByAppBundleId = new ConcurrentHashMap<String, WnsPushHandler>();

        Properties props = ServerPropertyHolder.getProps();
        String key = "push.supported_app_ids";
        String supportedProp = props.getProperty(key);
        if (supportedProp == null) {
            throw new IllegalStateException("Missing required server property " + key);
        }
        String[] appIds = supportedProp.split(",");

        for (String appId : appIds) {

            //APNs
            key = String.format("push.%s.ios.keyfile", appId);
            if (key != null) {
                String fileName = props.getProperty(key);
                key = String.format("push.%s.ios.keyfile.password", appId);
                String keyPassword = props.getProperty(key);

                InputStream certStream = new FileInputStream(new File(fileName));
                int numConnections;

                ApnsServiceBuilder builder = new ApnsServiceBuilder();
                if (_configType.equals(CONFIG_TYPE_PRODUCTION)) {
                    builder = builder.withProductionDestination();
                    numConnections = _prodNumConnections;
                } else {
                    builder = builder.withSandboxDestination();
                    numConnections = _sandboxNumConnections;
                }

                try {
                    ApnsService apns = builder
                            .withCert(certStream, keyPassword)
                            .asPool(numConnections)
                            .withReconnectPolicy(ReconnectPolicy.Provided.EVERY_HALF_HOUR)
                            .withDelegate(this)
                            .build();
                    apns.start();
                    _apnsByAppBundleId.put(appId, apns);

                    _logger.info("APNs initialized for appId: " + appId);
                } catch (Exception e) {
                    _logger.error("unable to initialize APNS for appId: " + appId, e);
                }

            } else {
                _logger.warn("unable to initialize APNs: no APNs config found for appId: " + appId);
            }


//            //GCM
//            key = String.format("push.%s.android.gcm.apikey", appId);
//            String apiKey = props.getProperty(key);
//            if (apiKey != null && apiKey.length() > 0) {
//                GcmPushHandler gcm = new GcmPushHandler();
//                gcm.setApiKey(apiKey);
//                gcm.setDelegate(this);
//                gcm.setTransactionManager(_transactionManager);
////_logger.info(MessageFormat.format(">>> initialized ANDROID PUSH for appId: {0}, with apiKey: {1}", appId, apiKey));
//                _gcmByAppBundleId.put(appId, gcm);
//            } else {
//                _logger.warn("unable to initialize GCM: no GCM config found");
//            }
//
//            //WNS
//            String wnsSidKey = props.getProperty(String.format("push.%s.wns.sid", appId));
//            String wnsClientSecret = props.getProperty(String.format("push.%s.wns.clientSecret", appId));
//            if (wnsSidKey != null && wnsSidKey.length() > 0 && wnsClientSecret != null && wnsClientSecret.length() > 0) {
//                WnsPushHandler wns = new WnsPushHandler();
//                wns.setSid(wnsSidKey);
//                wns.setClientSecret(wnsClientSecret);
//                _wnsByAppBundleId.put(appId, wns);
//            }

            //FCM
            key = props.getProperty(String.format("push.%s.android.fcm.keyfile", appId));
            if (key != null) {
                String fcmDbName = props.getProperty(String.format("push.%s.android.fcm.dbname", appId));
                FileInputStream serviceAccount = new FileInputStream(key);
                try {
                    FirebaseOptions options;
                    options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl(String.format("https://%s.firebaseio.com/", fcmDbName))
                        .build();
                    FirebaseApp.initializeApp(options);

                    _fcm = new FcmPushHandler(this);

                    _logger.info("FCM initialized for appId: " + appId);

                } catch (IOException e) {
                    _logger.warn("unable to initialize FCM for AppId: " + appId, e);
                }
            } else {
                _logger.warn("unable to initialize FCM: no FCM config found for appId: " + appId);
            }
        }

        _running = true;
        _inactiveChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (_running) {
                    try {
                        Thread.sleep(_inactiveCheckInterval);
                        for (ApnsService apns : _apnsByAppBundleId.values()) {
                            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                            try {
                                Map<String,Date> inactiveDevices = apns.getInactiveDevices();
                                if (inactiveDevices != null && inactiveDevices.size() > 0) {
                                    for (Map.Entry<String, Date> entry : inactiveDevices.entrySet()) {
                                        String key = entry.getKey();
                                        Date value = entry.getValue();
                                        SubscriberToken token = _dao.getToken(key);
                                        if (token != null) {
                                            if (token.getLastRegistration().before(value)) {
                                                _logger.debug("setting device for token '" + key +"' as inactive (last registration " + token.getLastRegistration().toString() + ", inactive date " + value.toString());
                                                _dao.setDeviceInactive(key);
                                            }
                                            else {
                                                _logger.debug("not setting device '" + key + "' inactive as the last registration date " + token.getLastRegistration() + " is later than the inactive date " + value);
                                            }
                                        }
                                    }
                                }
                                else {
                                    _logger.debug("no inactive devices");
                                }
                                _transactionManager.commit(txStatus);
                                txStatus = null;
                            }
                            catch (Throwable t) {
                                _logger.error("error while checking for inactive devices: ", t);
                            }
                            finally {
                                if (txStatus != null) {
                                    _transactionManager.rollback(txStatus);
                                }
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        _running = false;
                    }
                    catch (Throwable t) {
                        _logger.error("error while checking for inactive devices: ", t);
                    }
                }
                _logger.info("InactiveDeviceChecker thread exiting");
            }
        });
        _inactiveChecker.start();
        _logger.debug("PushService loaded");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void unload() {
        if (_inactiveChecker != null) {
            _logger.info("interrupting inactive device thread");
            _inactiveChecker.interrupt();
        }
        if (_updateTimer != null) {
            _updateTimer.cancel();
            _updateTimer = null;
        }

        _fcm.stop();
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void pushNotificationToSubscriber(Map<String, Object> msgValues, long subscriberId, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException
    {
        String payload = createPayload(msgValues);
//_logger.info(MessageFormat.format(">>> PAYLOAD:\n{0}", payload));
        List<Long> subscriberList = new ArrayList<>();
        subscriberList.add(subscriberId);
        pushPayloadToSubscribers(payload, subscriberList, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void pushNotificationToSubscribers(Map<String, Object> msgValues, List<Long> idList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException
    {
        if (idList == null || idList.size() == 0) {
            _logger.debug("nobody in the list; not pushing out any notifications");
            return;
        } else {
            String payload = createPayload(msgValues);
            pushPayloadToSubscribers(payload, idList, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void pushMessageToSubscribers(String message, List<Long> subscriberList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException
    {
        if (subscriberList == null || subscriberList.size() == 0) {
            _logger.debug("empty subscriber list; no push notifications");
            return;
        } else {
            Map<String, Object> msgValues = new HashMap<String, Object>();
            Map<String, Object> aps = new HashMap<String, Object>();
            aps.put("alert", message);
            msgValues.put("aps", aps);
            String payload = createPayload(msgValues);
            pushPayloadToSubscribers(payload, subscriberList, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void pushPayloadToSubscribers(String payload, List<Long> subscriberList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException
    {
        pushPayloadToSubscribers(payload, subscriberList, bundleIds, false, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
    }

    private interface PushBatchRunnable {
        public void run(List<Long> subscriberIdBatch) throws PushNetworkIoException, PayloadTooLargeException;
    }

    private void batchSubscribers(List<Long> subscriberIds, PushBatchRunnable runnable) throws PushNetworkIoException, PayloadTooLargeException {
        final int BATCH_SIZE = 5000;

        int batchId = 1;
        for (int i = 0; i < subscriberIds.size(); i += BATCH_SIZE) {
            int i1 = i, i2 = Math.min(i+BATCH_SIZE, subscriberIds.size());
            List<Long> subIdsBatch = new ArrayList<>(subscriberIds.subList(i1, i2));
            _logger.info(String.format("Batching pushes (batch #%d: %d..%d)", batchId, i1+1, i2));
            runnable.run(subIdsBatch);
            if (_logger.isDebugEnabled())
                _logger.debug(String.format("Done with push batch (batch #%d)", batchId));
            batchId += 1;
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void pushPayloadToSubscribers(final String payload, List<Long> subscriberList, final Set<String> bundleIds, final boolean priorityMessage,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException {
        batchSubscribers(subscriberList, new PushBatchRunnable() {
            @Override
            public void run(List<Long> subscriberIdBatch) throws PushNetworkIoException, PayloadTooLargeException {
                pushPayloadToSubscribersInternal(payload, subscriberIdBatch, bundleIds, priorityMessage, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
            }
        });
    }

    private void pushPayloadToSubscribersInternal(
        String payload, List<Long> subscriberList, Set<String> bundleIds, boolean priorityMessage,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException
    {
        if (subscriberList == null || subscriberList.size() == 0) {
            _logger.debug("empty subscriber list; not sending push notifications");
            return;
        }
        if (payload.length() > MAX_PAYLOAD_SIZE) {
            throw new PayloadTooLargeException("The payload '" + payload + "' is too large (must be <= "+MAX_PAYLOAD_SIZE+", length is " + payload.length() + ")");
        }

        List<SubscriberToken> tokens = null;
        boolean filterByIds = (bundleIds != null && bundleIds.size() > 0);
        boolean multipleIdsPossible = (bundleIds == null || bundleIds.size() > 1);

        //send APNS pushes
        if (!filterByIds) {
            tokens = _dao.getTokensForSubscriberListAndDeviceType(subscriberList, IPushService.DEVICE_TYPE_IOS);
        }
        else {
            tokens = _dao.getTokensForSubscriberListAndBundleIdList(subscriberList, IPushService.DEVICE_TYPE_IOS, new ArrayList<>(bundleIds));
        }
        if (tokens != null && tokens.size() > 0) {
            //multipleIdsPossible is not checked in this case since iOS pushes are sent one at a time and so there's not ever going to be multiple possible
            //_logger.info(">>> pushing APNs payload '" + payload + "' to " + (tokens != null ? tokens.size() : 0) + " iOS devices");
            for (SubscriberToken token : tokens) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(">>> pushing to " + token.getDeviceUuid() + " / " + token.getDeviceToken());
                }
                pushToIOSSubscriber(token, payload);
            }
        }

//        //send GCM pushes
//        if (!filterByIds) {
//            tokens = _dao.getTokensForSubscriberListAndDeviceType(subscriberList, IPushService.DEVICE_TYPE_ANDROID_GCM);
//        }
//        else {
//            tokens = _dao.getTokensForSubscriberListAndBundleIdList(subscriberList, IPushService.DEVICE_TYPE_ANDROID_GCM, new ArrayList<>(bundleIds));
//        }
//        if (tokens != null && tokens.size() > 0) {
//            if (multipleIdsPossible) {
//                pushToAndroidGcm(tokens, payload, priorityMessage);
//            }
//            else {
//                String appId = new ArrayList<>(bundleIds).get(0);
//                GcmPushHandler gcm = _gcmByAppBundleId.get(appId);
//                if (gcm != null) {
//                    _logger.debug(String.format("pushing payload '%s' for appBundleId %s to %d Android GCM devices", payload, appId, tokens != null ? tokens.size() : 0));
//                    gcm.push(tokens, payload, priorityMessage);
//                }
//            }
//        }

        //send FCM pushes
        if (!filterByIds) {
            tokens = _dao.getTokensForSubscriberListAndDeviceType(subscriberList, IPushService.DEVICE_TYPE_ANDROID_FCM);
        }
        else {
            tokens = _dao.getTokensForSubscriberListAndBundleIdList(subscriberList, IPushService.DEVICE_TYPE_ANDROID_FCM, new ArrayList<>(bundleIds));
        }
        if (tokens != null && tokens.size() > 0) {
            if (multipleIdsPossible) {
                pushToAndroidFcm(tokens, payload, priorityMessage, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
            }
            else {
                String appId = new ArrayList<>(bundleIds).get(0);
                if (_fcm != null) {
                    _logger.info(String.format(">>> pushing FCM payload '%s' for appBundleId %s to %d devices", payload, appId, tokens != null ? tokens.size() : 0));
                    _fcm.push(tokens, payload, priorityMessage, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
                }
            }
        }

//        //send WNS pushes
//        if (!filterByIds) {
//            tokens = _dao.getTokensForSubscriberListAndDeviceType(subscriberList, IPushService.DEVICE_TYPE_WINDOWS_WNS);
//        } else {
//            tokens = _dao.getTokensForSubscriberListAndBundleIdList(subscriberList, IPushService.DEVICE_TYPE_WINDOWS_WNS, new ArrayList<>(bundleIds));
//        }
//        if (tokens != null && tokens.size() > 0) {
//            if (multipleIdsPossible) {
//                pushToWindowsWns(tokens, payload);
//            }
//            else {
//                String appId = new ArrayList<>(bundleIds).get(0);
//                WnsPushHandler wns = _wnsByAppBundleId.get(appId);
//                if (wns != null) {
//                    _logger.debug(String.format("pushing payload '%s' for appBundleId %s to %d Windows WNS", payload, appId, tokens != null ? tokens.size() : 0));
//                    wns.push(this, tokens, payload);
//                }
//            }
//        }
    }

    @Override
    @ServiceMethod
    public void registerTokenForSubscriberAndDeviceType(byte[] deviceToken, long subscriberId, String deviceType, String deviceUuid, String appBundleId) {
        String tokenStr = encodeHex(deviceToken);
        registerHexTokenForSubscriberAndDeviceType(tokenStr, subscriberId, deviceType, deviceUuid, appBundleId);
    }

    @Override
    @ServiceMethod
    public void registerHexTokenForSubscriberAndDeviceType(String deviceToken, long subscriberId, String deviceType, String deviceUuid, String appBundleId) {
        if (deviceToken == null || deviceToken.length() == 0) {
            _logger.info("invalid device token '" + deviceToken + "' for deviceUuid '" + deviceUuid + "'");
            // invalid token, don't register
            return;
        }

//        _logger.debug("subscriberId: " + subscriberId);
//        _logger.debug("deviceUuid: " + deviceUuid);
//        _logger.debug("deviceType: " + deviceType);
//        _logger.debug("deviceToken: " + deviceToken);

        SubscriberToken token = new SubscriberToken();
        token.setAppBundleId(appBundleId);
        token.setSubscriberId(subscriberId);
        token.setDeviceToken(deviceToken);
        token.setDeviceType(deviceType);
        token.setDeviceUuid(deviceUuid);
        if (deviceToken.equals(_fakeToken) ||
                deviceToken.startsWith(TOKEN_TEST_PREFIX) ||
                deviceUuid.startsWith(TOKEN_TEST_PREFIX)) {
            token.setDeviceActive(false);
        }
        else {
            token.setDeviceActive(true);
        }
//      _logger.debug("DEVICE TOKEN: " + token);

        //the following statements are intentionally broken into 2 transactions because doing a delete then an insert can lead to deadlocks
        // so delete first, commit, then insert.  if the delete succeeds but the insert fails, that's ok in this case.

        //if any other devices with this UUID exist, remove them.
        // (otherwise it's possible duplicates can get in, especially if upgrading from Android to Android2 as the deviceType
        // because of the funky way the table is setup with a triple column primary key)
//        _dao.removeTokensForSubscriberIdAndDeviceUuid(subscriberId, deviceUuid);
//        _dao.removeTokensForDeviceUuidExceptForThisSubscriber(subscriberId, deviceUuid);

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            List<SubscriberToken> tokens = _dao.getTokensForDeviceOrToken(deviceUuid, deviceToken);
            if (tokens.size() == 0) {
                // just do an insert
                _dao.insertToken(token);
            }
            else if (tokens.size() == 1) {
                SubscriberToken existing = tokens.get(0);
                token.setTokenId(existing.getTokenId());
                _dao.updateToken(token);
            }
            else {
               final Set<Integer> tokensToDeactivate = new HashSet<Integer>(tokens.size() - 1);
               SubscriberToken toUpdate = null;
               for (SubscriberToken existing : tokens) {
                   if (toUpdate == null) {
                       toUpdate = existing;
                   }
                   if (existing.getDeviceToken().equals(deviceToken)) {
                       toUpdate = existing;
                   }
                   tokensToDeactivate.add(existing.getTokenId());
               }
               tokensToDeactivate.remove(toUpdate.getTokenId());
               token.setTokenId(toUpdate.getTokenId());
               _dao.updateToken(token);
               _updateTimer.schedule(new TimerTask() {
                   @Override
                public void run() {
                       for (Integer tokenId : tokensToDeactivate) {
                           DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                           TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                           try {
                               _dao.removeToken(tokenId);
                               _transactionManager.commit(txStatus);
                               txStatus = null;
                           }
                           catch (Throwable t) {
                               _logger.error(String.format("could not remove token for id %d", tokenId), t);
                           }
                           finally {
                               if (txStatus != null) {
                                   _transactionManager.rollback(txStatus);
                               }
                           }
                       }
                   }
               }, 0);
            }
            _transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
            }
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void unregisterTokenForSubscriberDevice(long subscriberId, String deviceUuid) {
        _dao.removeTokensForSubscriberIdAndDeviceUuid(subscriberId, deviceUuid);
        //-_logger.debug("unregistered " + rowsDeleted + " token(s) for subscriberId" + subscriberId + " and deviceUuid " + deviceUuid);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void unregisterTokensForDeviceExceptForThisSubscriber(long subscriberId, String deviceUuid)
    {
        /*int numRowsRemoved =*/ _dao.removeTokensForDeviceUuidExceptForThisSubscriber(subscriberId, deviceUuid);
        //_logger.debug("removed " + numRowsRemoved + " extra rows for device: " + deviceUuid);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public List<Long> getSubscriberIdsByType(String type)
    {
        return _dao.getSubscriberIdsByType(type);
    }

    protected String createPayload(Map<String, Object> msgValues) throws PayloadInvalidException, PayloadTooLargeException {
        String payload = null;
        try {
            payload = JsonUtil.getObjectMapper().writeValueAsString(msgValues);
        } catch (JsonGenerationException e) {
            _logger.info("could not serialize payload", e);
            throw new PayloadInvalidException(e);
        } catch (JsonMappingException e) {
            _logger.info("could not serialize payload", e);
            throw new PayloadInvalidException(e);
        } catch (IOException e) {
            _logger.info("could not serialize payload", e);
            throw new PayloadInvalidException(e);
        }
        if (payload.length() > MAX_PAYLOAD_SIZE) {
            throw new PayloadTooLargeException("The payload '" + payload + "' is too large (must be <= "+MAX_PAYLOAD_SIZE+", length is " + payload.length() + ")");
        }
        return payload;
    }

//    protected void pushToAndroidList(List<SubscriberToken> tokens, String payload)
//    throws PushNetworkIoException, PayloadTooLargeException
//    {
//        _androidPushHelper.push(tokens, payload);
//    }

    protected void pushToIOSSubscriber(SubscriberToken token, String payload)
    {
        if (token.getDeviceToken().startsWith(_rmiDeviceTokenPrefix)) {
            if (_rmiPushServer == null) {
                _logger.warn(String.format("Push sent to RMI device %s but RMI is disabled: %s", token.getDeviceToken(), payload));
            } else {
                _rmiPushServer.pushToIOS(token, payload);
            }
        } else {
            try {
            ApnsService apns = apnsByAppBundleId(token.getAppBundleId());
            if (apns != null) {
//_logger.info(">>> about to send ios push...");
                apns.push(token.getDeviceToken(), payload);
//_logger.info(">>> sent ios push");
            }
            else {
                _logger.warn(String.format("unable to send APNs push to subscriber %s: no APNS service avaiable for package %s", token.getSubscriberId(), token.getAppBundleId()));
            }
            } catch (RuntimeException e) {
                if (e != null && e.getMessage() != null && e.getMessage().contains("Invalid hex character")) {
                    _logger.warn(MessageFormat.format("unable to send APNs push to subscriber: {0}. Invalid token?", token.getSubscriberId()), e);
                } else
                    throw e;
            }
        }
    }

    protected void pushToAndroidGcm(List<SubscriberToken> tokens, String payload, boolean priorityMessage)
    {
        ArrayList<SubscriberToken> rmiTokens = new ArrayList<SubscriberToken>(tokens.size());
        Map<String, List<SubscriberToken>> tokensByAppBundleId = new HashMap<String, List<SubscriberToken>>();

        for (SubscriberToken token : tokens) {
            if (token.getDeviceToken().startsWith(_rmiDeviceTokenPrefix)) {
                rmiTokens.add(token);
            }
            else {
                List<SubscriberToken> appTokens = tokensByAppBundleId.get(token.getAppBundleId());
                if (appTokens == null) {
                    appTokens = new ArrayList<SubscriberToken>();
                    tokensByAppBundleId.put(token.getAppBundleId(), appTokens);
                }
                appTokens.add(token);
            }
        }
        if (!rmiTokens.isEmpty()) {
            if (_rmiPushServer == null) {
                _logger.warn(String.format("Push sent to %d RMI devices (%s) but RMI is disabled: %s", rmiTokens.size(), rmiTokens.get(0), payload));
            } else {
                _rmiPushServer.pushToAndroid(rmiTokens, payload);
            }
        }
        for (String appId : tokensByAppBundleId.keySet()) {
            List<SubscriberToken> appTokens = tokensByAppBundleId.get(appId);
            GcmPushHandler gcm = _gcmByAppBundleId.get(appId);
            if (gcm != null) {
                _logger.debug(String.format("pushing payload '%s' for appBundleId %s to %d Android GCM devices", payload, appId, appTokens != null ? appTokens.size() : 0));
                gcm.push(appTokens, payload, priorityMessage);
            }
        }
    }

    protected void pushToAndroidFcm(List<SubscriberToken> tokens, String payload, boolean priorityMessage,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    {
        ArrayList<SubscriberToken> rmiTokens = new ArrayList<SubscriberToken>(tokens.size());
        Map<String, List<SubscriberToken>> tokensByAppBundleId = new HashMap<String, List<SubscriberToken>>();

        for (SubscriberToken token : tokens) {
            if (token.getDeviceToken().startsWith(_rmiDeviceTokenPrefix)) {
                rmiTokens.add(token);
            }
            else {
                List<SubscriberToken> appTokens = tokensByAppBundleId.get(token.getAppBundleId());
                if (appTokens == null) {
                    appTokens = new ArrayList<SubscriberToken>();
                    tokensByAppBundleId.put(token.getAppBundleId(), appTokens);
                }
                appTokens.add(token);
            }
        }
        if (!rmiTokens.isEmpty()) {
            if (_rmiPushServer == null) {
                _logger.warn(String.format("Push sent to %d RMI devices (%s) but RMI is disabled: %s", rmiTokens.size(), rmiTokens.get(0), payload));
            } else {
                _rmiPushServer.pushToAndroid(rmiTokens, payload);
            }
        }
        for (String appId : tokensByAppBundleId.keySet()) {
            List<SubscriberToken> appTokens = tokensByAppBundleId.get(appId);
            if (_fcm != null) {
                _logger.info(String.format("pushing FCM payload '%s' for appBundleId %s to %d Android devices", payload, appId, appTokens != null ? appTokens.size() : 0));
                _fcm.push(appTokens, payload, priorityMessage, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
            }
        }
    }

    protected void pushToWindowsWns(List<SubscriberToken> tokens, String payload)
    {
        ArrayList<SubscriberToken> rmiTokens = new ArrayList<SubscriberToken>(tokens.size());
        Map<String, List<SubscriberToken>> tokensByAppBundleId = new HashMap<String, List<SubscriberToken>>();

        for (SubscriberToken token : tokens) {
            if (token.getDeviceToken().startsWith(_rmiDeviceTokenPrefix)) {
                rmiTokens.add(token);
            }
            else {
                List<SubscriberToken> appTokens = tokensByAppBundleId.get(token.getAppBundleId());
                if (appTokens == null) {
                    appTokens = new ArrayList<SubscriberToken>();
                    tokensByAppBundleId.put(token.getAppBundleId(), appTokens);
                }
                appTokens.add(token);
            }
        }
        if (!rmiTokens.isEmpty()) {
            if (_rmiPushServer == null) {
                _logger.warn(String.format("Push sent to %d RMI devices (%s) but RMI is disabled: %s", rmiTokens.size(), rmiTokens.get(0), payload));
            } else {
                _rmiPushServer.pushToWindows(rmiTokens, payload);
            }
        }
        for (String appId : tokensByAppBundleId.keySet()) {
            List<SubscriberToken> appTokens = tokensByAppBundleId.get(appId);
            WnsPushHandler wns = _wnsByAppBundleId.get(appId);
            if (wns != null) {
                _logger.debug(String.format("pushing payload '%s' for appBundleId %s to %d Windows WNS devices", payload, appId, appTokens != null ? appTokens.size() : 0));
                wns.push(this, appTokens, payload);
            }
        }
    }

    @Override
    @ServiceMethod
    public void pushMessageToDevices(Map<String,Object> msgValues, List<String> deviceUuids,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PayloadInvalidException, PayloadTooLargeException
    {
        String payload = createPayload(msgValues);
        pushPayloadToDevices(payload, deviceUuids, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
    }

    @Override
    @ServiceMethod
    public void pushPayloadToDevices(String payload, List<String> deviceUuids,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PayloadTooLargeException
    {
        if (payload.length() > MAX_PAYLOAD_SIZE)
            throw new PayloadTooLargeException("The payload '" + payload + "' is too large (must be <= "+MAX_PAYLOAD_SIZE+", length is " + payload.length() + ")");

        List<SubscriberToken> subscriberTokens = _dao.getTokensForDeviceUuidList(deviceUuids);

        //List<SubscriberToken> gcmPhones = new ArrayList<>();
        //List<SubscriberToken> wnsPhones = new ArrayList<>();
        List<SubscriberToken> fcmPhones = new ArrayList<>();
        for (SubscriberToken subscriberToken : subscriberTokens) {
            if (IPushService.DEVICE_TYPE_IOS.equals(subscriberToken.getDeviceType())) {
                pushToIOSSubscriber(subscriberToken, payload);
//            } else if (IPushService.DEVICE_TYPE_ANDROID_GCM.equals(subscriberToken.getDeviceType())) {
//                gcmPhones.add(subscriberToken);
            } else if (IPushService.DEVICE_TYPE_ANDROID_FCM.equals(subscriberToken.getDeviceType())) {
                fcmPhones.add(subscriberToken);
//            } else if (IPushService.DEVICE_TYPE_WINDOWS_WNS.equals(subscriberToken.getDeviceType())) {
//                wnsPhones.add(subscriberToken);
            }
        }
//        if (!gcmPhones.isEmpty()) {
//            pushToAndroidGcm(gcmPhones, payload, false);
//        }
        if (!fcmPhones.isEmpty()) {
            pushToAndroidFcm(fcmPhones, payload, false, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
        }
//        if (!wnsPhones.isEmpty()) {
//            pushToWindowsWns(wnsPhones, payload);
//        }
    }

    //ApnsDelegate
    @Override
    public void connectionClosed(DeliveryError e, int messageIdentifier) {
        _logger.warn("connection to Apple servers closed with error '" + e + "' while trying to deliver messageId " + messageIdentifier);
//        _triggerService.process(new Trigger(AnalyticsPayload.KEY_ANALYTICS, AnalyticsPayload.pushError("APNS", e.toString(), messageIdentifier+"")));
    }

    //ApnsDelegate
    @Override
    public void messageSendFailed(ApnsNotification message, Throwable e) {
        _logger.warn("delivery to device " + encodeHex(message.getDeviceToken()) + " failed: ", e);
//        _triggerService.process(new Trigger(AnalyticsPayload.KEY_ANALYTICS, AnalyticsPayload.pushError("APNS", new String(message.getDeviceToken()), e.getMessage())));
    }

    //ApnsDelegate
    @Override
    public void messageSent(ApnsNotification message) {
        //_logger.debug("message sent to device " + encodeHex(message.getDeviceToken()));
//        _triggerService.process(new Trigger(AnalyticsPayload.KEY_ANALYTICS, AnalyticsPayload.pushSent("APNS", new String(message.getDeviceToken()), message.getIdentifier()+"")));
    }

    //GcmDelegate
    @Override
    public void messageSentGcm(String deviceId, String messageId)
    {
        //_logger.debug("Android GCM message successfully sent to device: " + deviceId + ", messageId: " + messageId);
    }

    //GcmDelegate
    @Override
    @Transactional(propagation=NESTED)
    public void updateDeviceTokenGcm(String oldDeviceId, String newDeviceId)
    {
        //if record already exists with newDeviceId, just remove the row with oldDeviceId
        //else update the oldDeviceId to the newDeviceId
        SubscriberToken oldToken = _dao.getToken(oldDeviceId);
        SubscriberToken newToken = _dao.getToken(newDeviceId);

        if (oldToken == null) {
            //should never happen
            _logger.warn("attempting to update out of date token, but old token doesn't exist: " + oldDeviceId);
            return;
        }

        if (newToken == null) {
            //update oldToken to newToken
            _logger.debug("updating gcm token from " + oldDeviceId + " to " + newDeviceId);
            _dao.updateDeviceToken(oldDeviceId, newDeviceId);

        } else {
            //both tokens exist; remove the old one
            _logger.debug("removing duplicate deviceId: " + oldDeviceId);
            _dao.deleteUsingDeviceToken(oldDeviceId);
        }
    }

    //GcmDelegate
    @Override
    @Transactional(propagation=NESTED)
    public void messageSendFailedGcm(String deviceId)
    {
        _logger.info("Android device has unregistered: " + deviceId);
        _dao.deleteUsingDeviceToken(deviceId);
//        _triggerService.process(new Trigger(AnalyticsPayload.KEY_ANALYTICS, AnalyticsPayload.pushError("GCM", deviceId, "device unregistered")));
    }

    //GcmDelegate
    @Override
    public void serverExceptionGcm(String deviceId, String error)
    {
        //@see http://developer.android.com/guide/google/gcm/gcm.html / "Interpreting an error response" for explanations of each
        //ERROR_QUOTA_EXCEEDED
        //ERROR_DEVICE_QUOTA_EXCEEDED
        //ERROR_MISSING_REGISTRATION
        //ERROR_INVALID_REGISTRATION
        //ERROR_MISMATCH_SENDER_ID
        //ERROR_MESSAGE_TOO_BIG
        //ERROR_MISSING_COLLAPSE_KEY
        //ERROR_UNAVAILABLE
        //ERROR_INTERNAL_SERVER_ERROR
        //ERROR_INVALID_TTL
        _logger.error("error sending Android GCM push to: " + deviceId + ": " + error);
//        _triggerService.process(new Trigger(AnalyticsPayload.KEY_ANALYTICS, AnalyticsPayload.pushError("GCM", deviceId, error)));
    }

    //WnsDelegate
    @Override
    public void removeWnsToken(String wnsToken)
    {
        _dao.deleteUsingDeviceToken(wnsToken);
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }

    @Override
    @ServiceMethod
    public boolean doesSubscriberHavePushTokens(int subscriberId) {
        List<SubscriberToken> subscriberTokens = _dao.getAllActiveSubscriberTokens(subscriberId);
        return subscriberTokens != null && !subscriberTokens.isEmpty();
    }

    @Override
    @ServiceMethod
    public void sendAllPlayerPush(String message, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException
    {
        List<Long> subscriberIds = _dao.getSubscribersWithPushToken();
        pushMessageToSubscribers(message, subscriberIds, bundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
    }

}
