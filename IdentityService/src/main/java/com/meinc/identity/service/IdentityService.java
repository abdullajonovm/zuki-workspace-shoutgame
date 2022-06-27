package com.meinc.identity.service;

import static com.meinc.identity.domain.Subscriber.PRIMARY_ID_SCHEME.EMAIL;
import static com.meinc.identity.domain.Subscriber.PRIMARY_ID_SCHEME.FACEBOOK;
import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.commons.encryption.HexUtils;
import com.meinc.commons.encryption.IEncryption;
import com.meinc.commons.postoffice.service.IPostOffice;
import com.meinc.facebook.domain.FbSubscriber;
import com.meinc.facebook.exception.FacebookAuthenticationNeededException;
import com.meinc.facebook.exception.FacebookGeneralException;
import com.meinc.facebook.exception.FacebookPostInvalidException;
import com.meinc.facebook.exception.FacebookUserExistsException;
import com.meinc.facebook.exception.InvalidAccessTokenException;
import com.meinc.facebook.exception.PostLimitExceededException;
import com.meinc.facebook.service.IFacebookService;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.http.domain.NotAuthorizedException;
import com.meinc.identity.dao.ISubscriberDaoMapper;
import com.meinc.identity.dao.IXmlSubscriberDaoMapper;
import com.meinc.identity.domain.FacebookIdentityInfo;
import com.meinc.identity.domain.ForeignHostIdentityInfo;
import com.meinc.identity.domain.NicknameContext;
import com.meinc.identity.domain.SignupData;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.Subscriber.ROLE;
import com.meinc.identity.domain.SubscriberAddress;
import com.meinc.identity.domain.SubscriberEmail;
import com.meinc.identity.domain.SubscriberIdAndLanguageCode;
import com.meinc.identity.domain.SubscriberProfile;
import com.meinc.identity.domain.SubscriberSession;
import com.meinc.identity.domain.SubscriberSessionLight;
import com.meinc.identity.exception.DeviceNotFoundException;
import com.meinc.identity.exception.EmailAlreadyUsedException;
import com.meinc.identity.exception.FacebookAuthTokenNotFoundException;
import com.meinc.identity.exception.FacebookLogoutProhibitedException;
import com.meinc.identity.exception.InvalidEmailException;
import com.meinc.identity.exception.InvalidEmailPasswordException;
import com.meinc.identity.exception.InvalidSessionException;
import com.meinc.identity.exception.InvalidSubscriberUpdateException;
import com.meinc.identity.exception.MissingRequiredParameterException;
import com.meinc.identity.exception.NicknameAlreadyUsedException;
import com.meinc.identity.exception.NicknameInvalidException;
import com.meinc.identity.exception.RecruiterAlreadySetException;
import com.meinc.identity.exception.SubscriberInactiveException;
import com.meinc.identity.exception.SubscriberRequiresEulaException;
import com.meinc.identity.exception.SubscriberRequiresUpdateException;
import com.meinc.identity.helper.FacebookHandler;
import com.meinc.identity.helper.PasswordGenerator;
import com.meinc.identity.helper.UsernameGenerator;
import com.meinc.identity.helper.Util;
import com.meinc.jdbc.effect.TransactionSideEffectManager;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnService;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;

import clientproxy.epshttpconnectorservice.EpsHttpConnectorServiceClientProxy;

@Service(
    namespace =      IdentityService.MEINC_NAMESPACE,
    name =           IdentityService.SERVICE_NAME,
    interfaces =     IdentityService.SERVICE_INTERFACE,
    version =        IdentityService.SERVICE_VERSION,
    exposeAs =       IIdentityService.class)
public class IdentityService
implements IIdentityService
{
    public static final String MEINC_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "IdentityService";
    public static final String SERVICE_INTERFACE = "IIdentityService";
    public static final String SERVICE_VERSION = "1.0";

    private static Logger _logger = Logger.getLogger(IdentityService.class);
//    private static Logger _statsLogger = Logger.getLogger("IdentityServiceStats");
    private DistributedMap<String, Subscriber> _subscriberByNicknameAndContextId;
    private DistributedMap<String, Subscriber> _subscriberByEmailAndContextId;
    private DistributedMap<Long, Subscriber> _subscriberById;
    private DistributedMap<String, SubscriberSession> _sessionByIdAndDevice;
    private DistributedMap<String, SubscriberSession> _sessionBySessionKey;
//    private AtomicLongArray _methodElapsedTime = new AtomicLongArray(5);
//    private AtomicLongArray _methodInvocationCounts = new AtomicLongArray(5);
    private Map<ServiceEndpoint, String> _signupCallbackMap = new HashMap<>();

    private List<String> _invalidNicknames;
    private Set<Character> _validNonAlphaNumericCharacters;
    //private DistributedMap<String, Boolean> subscriberNicknameMap;
    private ConcurrentHashMap<String,Boolean> subscriberNicknames;

    private enum IDENTITY_METHOD {
        getSubscriberById(0),
        getSubscriberByEmail(1),
        getSubscriberByNickname(2),
        getSubscriberSession(3),
        getSubscriberSessionByDeviceSessionKey(4);

        private final int _value;

        private IDENTITY_METHOD(int value) {
            _value = value;
        }

        public int value() {
            return _value;
        }
    };

    private ScheduledExecutorService _scheduledExecutor = Executors.newScheduledThreadPool(2);

    @Autowired
    private ISubscriberDaoMapper subscriberDaoMapper;

    @Autowired
    private IXmlSubscriberDaoMapper xmlSubscriberDaoMapper;

    @Autowired
    private IFacebookService facebookService;

    @Autowired
    private IPostOffice postofficeService;

    @Autowired
    private PasswordGenerator passwordGenerator;

    @Autowired
    private UsernameGenerator usernameGenerator;

    @Autowired
    private IEncryption encryptionService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Pattern _duplicateKeyRegex;

    //facebook /fb http handler stuff
    private Map<String, FacebookHandler> _facebookHttpRequestHandlers = new HashMap<String, FacebookHandler>();
    private Pattern _pathPattern;
    private ObjectMapper _mapper = new ObjectMapper();

    public IdentityService()
    {
        initFacebookHttpRequestHandler();

        _pathPattern = Pattern.compile("^.+?/eps(/fb/[^/]+)(.*)");

        _duplicateKeyRegex = Pattern.compile("Duplicate entry (.+) for key '([^']+)'", Pattern.CASE_INSENSITIVE);

        _validNonAlphaNumericCharacters = new HashSet<Character>();
        _validNonAlphaNumericCharacters.add(' ');
        _validNonAlphaNumericCharacters.add('!');
        _validNonAlphaNumericCharacters.add('@');
        _validNonAlphaNumericCharacters.add('#');
        _validNonAlphaNumericCharacters.add('$');
        _validNonAlphaNumericCharacters.add('%');
        _validNonAlphaNumericCharacters.add('^');
        _validNonAlphaNumericCharacters.add('&');
        _validNonAlphaNumericCharacters.add('*');
        _validNonAlphaNumericCharacters.add('(');
        _validNonAlphaNumericCharacters.add(')');
        _validNonAlphaNumericCharacters.add('`');
        _validNonAlphaNumericCharacters.add('~');
        _validNonAlphaNumericCharacters.add('-');
        _validNonAlphaNumericCharacters.add('_');
        _validNonAlphaNumericCharacters.add('=');
        _validNonAlphaNumericCharacters.add('+');
        _validNonAlphaNumericCharacters.add('[');
        _validNonAlphaNumericCharacters.add(']');
        _validNonAlphaNumericCharacters.add('{');
        _validNonAlphaNumericCharacters.add('}');
        _validNonAlphaNumericCharacters.add('|');
        _validNonAlphaNumericCharacters.add('\\');
        _validNonAlphaNumericCharacters.add(':');
        _validNonAlphaNumericCharacters.add(';');
        _validNonAlphaNumericCharacters.add('\'');
        _validNonAlphaNumericCharacters.add('"');
        _validNonAlphaNumericCharacters.add(',');
        _validNonAlphaNumericCharacters.add('.');
        _validNonAlphaNumericCharacters.add('<');
        _validNonAlphaNumericCharacters.add('>');
        _validNonAlphaNumericCharacters.add('/');
        _validNonAlphaNumericCharacters.add('?');

    }

    private void notifyCachesOfSubscriberChange(long subscriberId) {
        Subscriber sub = _subscriberById.remove(subscriberId);
        if (sub != null) {
            _subscriberByNicknameAndContextId.remove(subToNicknameAndContextIdKey(sub));
            _subscriberByEmailAndContextId.remove(subToEmailAndContextIdKey(sub));
        }
    }

    private void notifyCachesOfSessionChange(String mapKey) {
        SubscriberSession session = _sessionByIdAndDevice.remove(mapKey);
        if (session != null) {
            _sessionBySessionKey.remove(session.getSessionKey());
        }
    }

    @Override
    @OnStart
    @ServiceMethod
    public void start()
    {
        _subscriberById = DistributedMap.getMap("subscriberById");
        _subscriberByNicknameAndContextId = DistributedMap.getMap("subscriberByNicknameAndContextId");
        _subscriberByEmailAndContextId = DistributedMap.getMap("subscriberByEmailAndContextId");
        _sessionByIdAndDevice = DistributedMap.getMap("sessionByIdAndDevice");
        _sessionBySessionKey = DistributedMap.getMap("sessionBySessionKey");

//this constantly just fills up the logs. not sure what it was used for, but it seems that whatever purpose it once served is gone.
//        _scheduledExecutor.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                StringBuilder message = new StringBuilder(1024);
//                message.append("------------------------------------------------------------------------\n");
//                for (IDENTITY_METHOD method : IDENTITY_METHOD.values()) {
//                    long totalTime = _methodElapsedTime.get(method.value());
//                    long invocations = _methodInvocationCounts.get(method.value());
//                    message.append(String.format("|%70s|\n", String.format("%s: %.2f ms (%d invocations)", method.name(), invocations > 0 ? (double)totalTime/(double)invocations : 0, invocations)));
//                }
//                message.append("------------------------------------------------------------------------\n");
//                _statsLogger.info(String.format("IdentityServiceStats:\n%s", message.toString()));
//            }
//        }, 60, 60, TimeUnit.SECONDS);

        passwordGenerator.start();
        usernameGenerator.start();

        _logger.debug("registering /fb endpoint...");
        //register to handle get/post events for the /fb endpoint
        ServiceEndpoint myEndpoint = new ServiceEndpoint();
        myEndpoint.setNamespace(MEINC_NAMESPACE);
        myEndpoint.setServiceName(SERVICE_NAME);
        myEndpoint.setVersion(SERVICE_VERSION);

        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        boolean success = httpConnector.registerHttpCallback(myEndpoint, "doGetFb", "doPostFb", "/fb", "");
        if (success) {
            _logger.info("registered " + SERVICE_NAME + " to receive doGet/doPost for /fb endpoint");
        }
        else {
            _logger.error("unable to register " + SERVICE_NAME + " to receive doGet/doPost for /fb endpoint");
        }

        subscriberNicknames = new ConcurrentHashMap<String,Boolean>();
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        txDef.setReadOnly(false);
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
            List<NicknameContext> allNicknames = subscriberDaoMapper.getAllNicknames();
            for (NicknameContext nc : allNicknames) {
                subscriberNicknames.put(makeNicknameKey(nc.getContextId(), nc.getNickname()), true);
            }
            transactionManager.commit(txStatus);
            txStatus = null;
        } finally {
            if (txStatus != null)
                transactionManager.rollback(txStatus);
        }

        //wait for the postoffice service to load and then register for callbacks
        ServiceEndpoint ep = new ServiceEndpoint();
        ep.setServiceName("PostOfficeService");
        _logger.debug("waiting for PostOffice service to load...");
        ServiceMessage.waitForServiceRegistration(ep);
        _logger.debug("registering for postoffice callbacks...");
        ep = new ServiceEndpoint();
        ep.setServiceName(SERVICE_NAME);
        ep.setVersion(SERVICE_VERSION);
        ep.setNamespace(IdentityService.MEINC_NAMESPACE);
        postofficeService.registerEmailVerifiedCallback(ep, "emailVerified");
    }

    @Override
    @OnStop(depends=@OnService(proxy=EpsHttpConnectorServiceClientProxy.class))
    @ServiceMethod
    public void stop()
    {
        passwordGenerator.stop();
        usernameGenerator.stop();
        _scheduledExecutor.shutdown();

        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        boolean success = httpConnector.unregisterHttpCallback("/fb");
        if (success) {
            _logger.debug("unregister for handling requests to '/fb'");
        }
    }

    @Override
    @ServiceMethod
    public void registerSignupCallback(ServiceEndpoint endpoint, String methodName)
    {
        _signupCallbackMap.put(endpoint, methodName);
    }

    @Override
    @ServiceMethod
    public HttpResponse doGetFb(HttpRequest request) {
        return doPostFb(request);
    }

    @Override
    @ServiceMethod
    public HttpResponse doPostFb(HttpRequest request) {
        Matcher matcher = _pathPattern.matcher(request.getRequestURL());
        String path = "";
        if (matcher.matches()) {
            path = matcher.group(1);
        }
        _logger.info("request received for URI " + request.getRequestURL() + " (path=" + path + ")");
        FacebookHandler handler = _facebookHttpRequestHandlers.get(path);
        if (handler != null) {
//            Map<String, String> headers = request.getHeaders();
//            _logger.info("headers:");
//            for (String key : headers.keySet()) {
//                _logger.info(key + ": " + headers.get(key));
//            }
            HttpResponse response = new HttpResponse();
            try {
                long subscriberId = 0L;
                if (handler.requiresAuthentication()) {
                    //TODO: refactor this ugliness
                    //subscriberId = request.getSubscriberIdFromSession();
                    final String SESSION_KEY_HEADER = "X-REST-SESSION-KEY";
                    final String DEVICE_ID_HEADER = "X-REST-DEVICE-ID";
                    final String APPLICATION_ID_HEADER = "X-REST-APPLICATION-ID";
                    final String APPLICATION_VERSION_HEADER = "X-REST-APPLICATION-VERSION";
                    String sessionKey = request.getHeader(SESSION_KEY_HEADER);
                    String deviceId = request.getHeader(DEVICE_ID_HEADER);
                    String applicationId = request.getHeader(APPLICATION_ID_HEADER);
                    String applicationVersion = request.getHeader(APPLICATION_VERSION_HEADER);
                    try {
                        subscriberId = getSubscriberIdByDeviceSessionKey(deviceId, sessionKey, applicationId, applicationVersion);
                    } catch (InvalidSessionException e) {
                        throw new NotAuthorizedException("error getting subscriber id: " + e.getMessage(), e);
                    }
                }
                response = handler.handleRequest(request, subscriberId);
            }
            catch (FacebookAuthenticationNeededException e) {
                response.setContentType("application/json; charset=utf-8");
                String s = "{\"success\":false, \"needNewAccessToken\" : true}";
                response.getWriter().println(s);
            } catch (FacebookPostInvalidException e) {
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().println("{\"success\": false, \"postInvalid\": true}");
//            } catch (FacebookOAuthException e) {
//                _logger.error("error contacting Facebook", e);
//                response.setContentType("application/json");
//                String s = "{\"success\":false, \"needNewAccessToken\" : true}";
//                response.getWriter().println(s);
//            } catch (FacebookException e) {
//                response.setContentType("application/json");
//                StringBuilder buf = new StringBuilder();
//                buf.append("{\"success\": false, \"cannotConnectToFb\" : true}");
//                response.getWriter().println(buf.toString());
            } catch (FacebookGeneralException e) {
                response.setContentType("application/json; charset=utf-8");
                StringBuilder buf = new StringBuilder();
                buf.append("{\"success\": false, \"cannotConnectToFb\" : true}");
                response.getWriter().println(buf.toString());
            } catch (NumberFormatException e) {
                _logger.info("number format exception", e);
                response.errorOut(400, "invalid parameter type");
            } catch (IllegalArgumentException e) {
                _logger.info("missing parameter", e);
                response.errorOut(400, "missing parameter");
            } catch (IOException e) {
                _logger.error("error handling request", e);
                response.setError(500);
                response.setContentType("text/html");
                response.getWriter().print("<html><head><title>Internal Server Errror</title></head><body>Internal Server error</body></html>");
                return response;
            } catch (NotAuthorizedException e) {
                _logger.error("error getting subscriberId: " + e.getMessage());
                response.errorOut(401, "subscriber not authorized");
            } catch (PostLimitExceededException e) {
                response.setContentType("application/json; charset=utf-8");
                String s = "{\"success\": false, \"postLimitExceeded\": true}";
                response.getWriter().println(s);
            }
            if (response != null) {
                return response;
            }
        }
        HttpResponse response = new HttpResponse();
        response.setContentType("text/html");
        PrintWriter buf = response.getWriter();
        buf.print("<html><head><title>Not Found</title><body><div>URL ");
        buf.print(request.getRequestURL());
        buf.print(" not found on this server</div></body></html>");
        return response;
    }

//    private String getStringParam(Parameters params, String name, boolean required) {
//        String value = params.getString(name);
//        if (required && (value == null || "".equalsIgnoreCase(value.trim()))) {
//            throw new IllegalArgumentException(String.format("missing parameter '%s'", name));
//        }
//        if (value == null) {
//            return "";
//        }
//        return value;
//    }

    private void initFacebookHttpRequestHandler()
    {
//        _facebookHttpRequestHandlers.put("/fb/signup", new FacebookHandler() {
//            public HttpResponse handleRequest(HttpRequest request, int subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
//                return handleSignupStart(request);
//            }
//            public boolean requiresAuthentication() {
//                return false;
//            }
//        });
//        _facebookHttpRequestHandlers.put("/fb/signup_finished", new FacebookHandler() {
//            public HttpResponse handleRequest(HttpRequest request, int subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
//                return handleSignupFinished(request);
//            }
//            public boolean requiresAuthentication() {
//                return false;
//            }
//        });
//
//        _facebookHttpRequestHandlers.put("/fb/testSignup", new FacebookHandler() {
//            public boolean requiresAuthentication() {
//                return false;
//            }
//            public HttpResponse handleRequest(HttpRequest request, int subscriberId)
//                    throws IOException, FacebookGeneralException,
//                    NotAuthorizedException, FacebookAuthenticationNeededException,
//                    PostLimitExceededException, FacebookPostInvalidException
//            {
//                Parameters params = request.getParameters();
//                Map<String, Object> returnParams = new HashMap<String, Object>();
//
//                String appName = getStringParam(params, "appId", false);
//                if ("".equals(appName)) {
//                    _logger.warn("client did not pass appId param; hardcoding to 'SHOUT'");
//                    appName = "SHOUT";
//                }
//
//                String deviceToken = getStringParam(params, "deviceToken", false);
//                if ("null".equalsIgnoreCase(deviceToken)) {
//                    deviceToken = null;
//                }
//                String deviceId = getStringParam(params, "deviceId", false);
//                String applicationId = getStringParam(params, "applicationId", false);
//                String applicationVersion = getStringParam(params, "applicationVersion", false);
//
//                SubscriberSession subscriberDevice = new SubscriberSession();
//                subscriberDevice.setAppId(applicationId);
//                subscriberDevice.setAppVersion(applicationVersion);
//
//                if (deviceId != null && deviceId.trim().length() > 0)
//                {
//                    String deviceModel = getStringParam(params, "deviceModel", false);
//                    String deviceName = getStringParam(params, "deviceName", false);
//                    String deviceVersion = getStringParam(params, "deviceVersion", false);
//                    String osName = getStringParam(params, "deviceOsName", false);
//                    String osType = getStringParam(params, "deviceOsType", false);
//
//                    subscriberDevice.setDeviceId(deviceId);
//                    subscriberDevice.setDeviceModel(deviceModel);
//                    subscriberDevice.setDeviceName(deviceName);
//                    subscriberDevice.setDeviceVersion(deviceVersion);
//                    subscriberDevice.setOsName(osName);
//                    subscriberDevice.setOsType(osType);
//                }
//
//                String fbAccessToken = getStringParam(params, "fbAccessToken", false);
//                if ("".equals(fbAccessToken)) fbAccessToken = null;
//                String email = getStringParam(params, "email", false);
//                if ("".equals(email)) email = null;
//                String password = getStringParam(params, "password", false);
//                if ("".equals(password)) password = null;
//                String username = getStringParam(params, "username", false);
//                if ("".equals(username)) username = null;
//
//                //either 1 of fbAccessToken or email are required
//                if (fbAccessToken == null && email == null) {
//                    throw new IllegalArgumentException("need either fbAccessToken or email");
//                }
//                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
//                try {
//                    signup(1, fbAccessToken, email, password, username, subscriberDevice);
//                    transactionManager.commit(txStatus);
//                    txStatus = null;
//                } catch (InvalidEmailException e) {
//                    returnParams.put("success", false);
//                    returnParams.put("emailInvalid", true);
//                    _logger.error("will return emailInvalid", e);
//                } catch (EmailAlreadyUsedException e) {
//                    returnParams.put("success", false);
//                    returnParams.put("emailAlreadyUsed", true);
//                    _logger.error("will return emailAlreadyUsed", e);
//                } catch (InvalidAccessTokenException e) {
//                    returnParams.put("sucess", false);
//                    returnParams.put("invalidAccessToken", true);
//                    _logger.error("will return invalidAccessToken", e);
//                } catch (FacebookUserExistsException e) {
//                    returnParams.put("success", false);
//                    returnParams.put("facebookSubscriberExists", true);
//                    _logger.error("will return facebookSubscriberExists", e);
//                } catch (InvalidSessionException e) {
//                    _logger.error("will return invalidSession", e);
//                    returnParams.put("success", false);
//                    returnParams.put("invalidSession", true);
//                } catch (NicknameInvalidException e) {
//                    _logger.error("will return invalidNickname", e);
//                    returnParams.put("success", false);
//                    returnParams.put("invalidNickname", true);
//                }
//                finally {
//                    if (txStatus != null) {
//                        transactionManager.rollback(txStatus);
//                    }
//                }
//                HttpResponse response = new HttpResponse(request);
//                response.setContentType("application/json; charset=utf-8");
//                String json = _mapper.writeValueAsString(returnParams);
//                response.getWriter().println(json);
//                return response;
//            }
//        });
        _facebookHttpRequestHandlers.put("/fb/deauthorize", new FacebookHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    String facebookId = facebookService.handleDeauthorize(request);
                    if (facebookId != null) {
                        subscriberDaoMapper.deleteAllFacebookMappingsForFacebookId(facebookId);
                    }
                    transactionManager.commit(txStatus);
                    txStatus = null;

                    return new HttpResponse();
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
            @Override
            public boolean requiresAuthentication() {
                return false;
            }
        });
        _facebookHttpRequestHandlers.put("/fb/family", new FacebookHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    Map<String,Object> ret = new HashMap<String, Object>();
                    HttpResponse response = new HttpResponse();
                    response.setContentType("application/json; charset=utf-8");
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        ret.put("success", false);
                        ret.put("noFacebookAppId", true);
                    }
                    else {
                        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);

                        if (fbInfo == null) {
                            ret.put("success", false);
                        } else {
                            String fbId = fbInfo.getFacebookId();
                            List<FbSubscriber> family = facebookService.getFamily(fbId);
                            ret.put("success", true);
                            ret.put("family", family);
                        }
                    }

                    String json = _mapper.writeValueAsString(ret);
                    response.getWriter().println(json);

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
        });
        _facebookHttpRequestHandlers.put("/fb/logout", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, final long subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                try {
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        _ret.put("success", false);
                        _ret.put("noFacebookAppId", true);
                    }
                    else {
                        removeFacebookAccessToken(subscriberId, facebookAppId);
                        _ret.put("success", true);
                    }
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } catch (URISyntaxException e) {
                    _ret.put("success", false);
                    _ret.put("invalidPrimaryId", true);
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);
                    return response;
                } catch (FacebookLogoutProhibitedException e) {
                    _ret.put("success", false);
                    _ret.put("facebookLogoutProhibited", true);
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);
                    return response;
                } catch (FacebookAuthTokenNotFoundException e) {
                    _ret.put("success", false);
                    _ret.put("noAuthTokenForSubscriber", true);
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/setToken", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, final long subscriberId) throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {
                String accessToken = request.getFirstParameter("accessToken");
                if (accessToken == null || accessToken.trim().length() == 0) {
                    throw new IllegalArgumentException("missing accessToken");
                }
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    HttpResponse response = new HttpResponse();
                    response.setContentType("application/json; charset=utf-8");
                    Map<String,Object> _ret = new HashMap<String, Object>();
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        _ret.put("success", false);
                        _ret.put("noFacebookAppId", true);
                    }
                    else {
                        try {
                            String facebookId = setFacebookAccessToken(subscriberId, accessToken, facebookAppId);

                            _ret.put("success", true);
                            _ret.put("fbId", facebookId);
                        } catch (FacebookUserExistsException e) {
                            _ret.put("success", false);
                            _ret.put("facebookSubscriberExists", true);
                        } catch (FacebookGeneralException e) {
                            _ret.put("success", false);
                        } catch (InvalidAccessTokenException e) {
                            _ret.put("success", false);
                        }
                    }
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/info", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                    throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException {

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    HttpResponse response = new HttpResponse();
                    response.setContentType("application/json; charset=utf-8");
                    Map<String,Object> _ret = new HashMap<String, Object>();
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        _ret.put("success", false);
                        _ret.put("noFacebookAppId", true);
                    }
                    else {
                        _ret.put("success", true);
                        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
                        if (fbInfo == null) {
                            _ret.put("noFacebookInfo", true);
                        } else {
                            String facebookId = fbInfo.getFacebookId();
                            FbSubscriber fbSub = facebookService.getFacebookUser(facebookId);
                            if (fbSub != null) {
                                _ret.put("fbId", "" + fbSub.getFbId());
                                _ret.put("accessToken", facebookService.getAccessToken(facebookId));
                            }
                            else {
                                _ret.put("noFacebookInfo", true);
                            }
                        }
                    }
                    String val = _mapper.writeValueAsString(_ret);
                    response.getWriter().println(val);

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/post", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                    throws IOException, FacebookGeneralException, FacebookAuthenticationNeededException, PostLimitExceededException, FacebookPostInvalidException {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    Map <String,Object> ret = new HashMap<String, Object>();
                    HttpResponse response = null;
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        ret.put("success", false);
                        ret.put("noFacebookAppId", true);
                    }
                    else {
                        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
                        if (fbInfo != null) {
                            String facebookId = fbInfo.getFacebookId();
                            response = facebookService.handleFbPost(request, facebookId);
                        } else {
                            ret.put("success", false);
                        }
                    }
                    if (response == null) {
                        String val = _mapper.writeValueAsString(ret);
                        response = new HttpResponse();
                        response.setContentType("application/json; charset=utf-8");
                        response.getWriter().println(val);
                    }

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/updatePhotos", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                throws IOException, FacebookGeneralException, NotAuthorizedException, FacebookAuthenticationNeededException
            {
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                updateFacebookPhotos(subscriberId);
                response.getWriter().println("{\"success\": true}");
                return response;
            }
        });
        _facebookHttpRequestHandlers.put("/fb/limits", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return false;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                throws IOException, FacebookGeneralException, NotAuthorizedException,
                FacebookAuthenticationNeededException, PostLimitExceededException
            {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    HttpResponse response = new HttpResponse();
                    response.setContentType("application/json; charset=utf-8");
                    Map<String,Object> vals = new HashMap<String, Object>();
                    vals.put("success", true);
                    vals.put("postMaxFriends", facebookService.getPostLimit());
                    response.getWriter().println(_mapper.writeValueAsString(vals));

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/permissions", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                throws IOException, FacebookGeneralException, NotAuthorizedException,
                FacebookAuthenticationNeededException, PostLimitExceededException,
                FacebookPostInvalidException
            {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    Map <String,Object> ret = new HashMap<String, Object>();
                    HttpResponse response = null;
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        ret.put("success", false);
                        ret.put("noFacebookAppId", true);
                    }
                    else {
                        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
                        if (fbInfo != null) {
                            String facebookId = fbInfo.getFacebookId();
                            response = facebookService.handleGetPermissions(request, facebookId);
                        } else {
                            ret.put("success", false);
                        }
                    }
                    if (response == null) {
                        response = new HttpResponse();
                        response.setContentType("application/json; charset=utf-8");
                        String val = _mapper.writeValueAsString(ret);
                        response.getWriter().println(val);
                    }

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });
        _facebookHttpRequestHandlers.put("/fb/get", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                    throws IOException, FacebookGeneralException, NotAuthorizedException,
                    FacebookAuthenticationNeededException, PostLimitExceededException,
                    FacebookPostInvalidException
            {
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                try {
                    Map <String,Object> ret = new HashMap<String, Object>();
                    HttpResponse response = null;
                    String facebookAppId = request.getFirstParameter("facebookAppId");
                    if (facebookAppId == null || facebookAppId.trim().length() == 0 || "null".equalsIgnoreCase(facebookAppId)) {
                        ret.put("success", false);
                        ret.put("noFacebookAppId", true);
                    }
                    else {
                        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
                        if (fbInfo != null) {
                            String facebookId = fbInfo.getFacebookId();
                            response = facebookService.handleFbGet(request, facebookId);
                        } else {
                            ret.put("success", false);
                        }
                    }
                    if (response == null) {
                        String val = _mapper.writeValueAsString(ret);
                        response = new HttpResponse();
                        response.setContentType("application/json; charset=utf-8");
                        response.getWriter().println(val);
                    }

                    transactionManager.commit(txStatus);
                    txStatus = null;
                    return response;
                } finally {
                    if (txStatus != null)
                        transactionManager.rollback(txStatus);
                }
            }
        });

        //TODO: remove this before deploying friend box
        _facebookHttpRequestHandlers.put("/fb/infoForFacebookUsers", new FacebookHandler() {
            @Override
            public boolean requiresAuthentication() {
                return true;
            }
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId)
                    throws IOException, FacebookGeneralException,
                    NotAuthorizedException, FacebookAuthenticationNeededException,
                    PostLimitExceededException, FacebookPostInvalidException
            {
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json); charset=utf-8");
                Map <String,Object> ret = new HashMap<String, Object>();
                String facebookIdsJson = request.getFirstParameter("facebookIds");
                if (facebookIdsJson == null || facebookIdsJson.trim().length() == 0 || "null".equalsIgnoreCase(facebookIdsJson)) {
                    ret.put("success", false);
                }
                else {
                    List<String> facebookIds = _mapper.readValue(facebookIdsJson, new TypeReference<List<String>>(){});
                    List<SubscriberProfile> info = getProfileInfoForFacebookUsers(facebookIds, 1);
                    ret.put("success", true);
                    ret.put("info", info);
                    response.getWriter().println(_mapper.writeValueAsString(ret));
                }
                return response;
            }
        });
    }

    private static final int POOL_SIZE = 16;
    private int _fbPhotoUpdateCount;
    private Lock _fbPhotoCountLock = new ReentrantLock();
    private void updateSubscriberPhotoUrls()
    {
        new Thread() {
            @Override
            public void run() {
                _fbPhotoUpdateCount = 0;
                final List<FacebookIdentityInfo> subs = subscriberDaoMapper.getFacebookSubscriberIds();
                final CountDownLatch cdl = new CountDownLatch(subs.size());
                final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);

                for (final FacebookIdentityInfo fbId : subs) {
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run()
                        {
                            try {
                                updateSubscriberFacebookPhotoUrl(fbId, subs.size(), cdl);
                            } catch (FacebookAuthenticationNeededException e) {
                            }
                        }
                    });
                }

                //wait until all processing threads have completed
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadPool.shutdown();
            }
        }.start();
    }
    private void updateSubscriberFacebookPhotoUrl(FacebookIdentityInfo info, int size, CountDownLatch cdl) throws FacebookAuthenticationNeededException
    {
        Subscriber s = null;
        try {
            FbSubscriber fullSub = null;
            s = getSubscriberById(info.getSubscriberId());
            fullSub = facebookService.getFacebookUser(info.getFacebookId());
            if (fullSub == null) {
                return;
            }
            if (fullSub.getPicture() != null && fullSub.getPicture().trim().length() > 0) {
                _fbPhotoCountLock.lock();
                try {
                    _logger.info("updating photo url of subscriber ("+ (++_fbPhotoUpdateCount) +" of "+size+") '" + s.getSubscriberId() + "' (facebookId '" +info.getFacebookId() + "') to " + fullSub.getPicture());
                } finally {
                    _fbPhotoCountLock.unlock();
                }
                s.setPhotoUrl(fullSub.getPicture());
                updateSubscriber(s);
            }
        } catch (InvalidSubscriberUpdateException e) {
            _logger.error(String.format("unable to update subscriber %s: %s", s != null ? s.toString() : "null", e.getMessage()), e);
        } catch (EmailAlreadyUsedException e) {
            _logger.error(String.format("unable to update subscriber %s: %s", s != null ? s.toString() : "null", e.getMessage()), e);
        } catch (InvalidEmailException e) {
            _logger.error(String.format("unable to update subscriber %s: %s", s != null ? s.toString() : "null", e.getMessage()), e);
        } catch (NicknameAlreadyUsedException e) {
            _logger.error(String.format("unable to update subscriber %s: %s", s != null ? s.toString() : "null", e.getMessage()), e);
        } catch (NicknameInvalidException e) {
            _logger.error(String.format("unable to update subscriber %s: %s", s != null ? s.toString() : "null", e.getMessage()), e);
        } finally {
            cdl.countDown();
        }
    }

    private void removeDeviceSessionsForThisDeviceAndTheseSubscribers(final String deviceId, final List<Long> subscriberIds, final long currentSubscriber) {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                    TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                    try {
                        for (final Long subscriberId : subscriberIds) {
                            if (subscriberId.intValue() != currentSubscriber) {
                                _logger.info(String.format("removing all sessions for subscriber %d/deviceId %s", subscriberId, deviceId));
                                subscriberDaoMapper.removeDeviceSessionsForThisDeviceAndThisSubscriber(deviceId, subscriberId);
                                TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyCachesOfSessionChange(stringAndIntToCompoundKey(deviceId, subscriberId));
                                    }
                                });
                            }
                        }
                        transactionManager.commit(txStatus);
                        txStatus = null;
                    }
                    finally {
                        if (txStatus != null) {
                            transactionManager.rollback(txStatus);
                            txStatus = null;
                        }
                    }
                }
                catch (Throwable t) {
                    _logger.error("error removing sessions", t);
                }
            }
        };
        _scheduledExecutor.schedule(tt, 0, TimeUnit.MILLISECONDS);
    }

    private String subToNicknameAndContextIdKey(Subscriber sub) {
        return stringAndIntToCompoundKey(sub.getNickname(), sub.getContextId());
    }

    private String subToEmailAndContextIdKey(Subscriber sub) {
        return stringAndIntToCompoundKey(sub.getEmail(), sub.getContextId());
    }

    private String stringAndIntToCompoundKey(String s, long i) {
        return String.format("%s\t%s", s, i);
    }

    private void populateCacheWithSubscriber(Subscriber sub) {
        _subscriberById.put(sub.getSubscriberId(), sub);
        _subscriberByNicknameAndContextId.put(subToNicknameAndContextIdKey(sub), sub);
        _subscriberByEmailAndContextId.put(subToEmailAndContextIdKey(sub), sub);
    }

    private void updateSubscriberSession(final SubscriberSession session, boolean synchronous) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    final Long subId = session.getSubscriberId();
                    final String deviceId = session.getDeviceId();
                    final String mapKey = stringAndIntToCompoundKey(deviceId, subId);
                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                    TransactionStatus txStatus = transactionManager.getTransaction(txDef);
                    try {
                        subscriberDaoMapper.updateSubscriberSession(session);
                        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                            @Override
                            public void run() {
                                notifyCachesOfSessionChange(mapKey);
                            }
                        });
                        transactionManager.commit(txStatus);
                        txStatus = null;
                    }
                    finally {
                        if (txStatus != null) {
                            transactionManager.rollback(txStatus);
                            txStatus = null;
                        }
                    }
                }
                catch (Throwable t) {
                    _logger.error("error while updating subscriber session", t);
                }
            }
        };
        if (synchronous) {
            task.run();
        }
        else {
            _scheduledExecutor.schedule(task, 0, TimeUnit.MILLISECONDS);
        }
    }

    private String makeNicknameKey(int contextId, String username) {
        return contextId + "_" + username;
    }

    private void doActualSignup(int contextId, Subscriber subscriber, IIdentityService.PASSWORD_SCHEME pwScheme)
    throws NicknameInvalidException, EmailAlreadyUsedException, NicknameAlreadyUsedException
    {
        String username = subscriber.getNickname();
        //make sure username is unique (adding numbers on the end if needed)
        int counter = 1;
        String origUsername = username;
        //while (subscriberDaoMapper.isNicknameUsed(username)) {
        String nicknameKey = makeNicknameKey(contextId, username);
        //while (subscriberNicknameMap.containsKey(nicknameKey)) {
        while (subscriberNicknames.contains(nicknameKey)) {
            _logger.debug("username '" + username + "' was already taken in our system; incrementing and trying again...");
            username = origUsername + counter++;
            nicknameKey = makeNicknameKey(contextId, username);
        }

        // Commented out on 2/28/2014 by Grant per Bruce's instruction for performance reasons - revisit when we can have a dedicated bank of servers
//        //salt password (@see https://github.com/wg/scrypt)
//        String encryptedPassword = encryptionService.scryptEncode(password);

        if (!isNicknameValid(username)) {
            // won't actually get here, isNicknameValid throws the exception itself
            throw new NicknameInvalidException();
        }

        //add subscriber (sans mint data, using defaults for anything not set here)
        subscriber.setNickname(username);
        if (subscriber.getLanguageCode() == null) subscriber.setLanguageCode("en");
        try {
            switch (pwScheme)
            {
                case USE_MYSQL_PASSWORD:
                    subscriberDaoMapper.addSubscriberFromSignup(subscriber);
                    break;

                case USE_PASSWORD_AS_IS:
                    subscriberDaoMapper.addSubscriberFromSignupUsingCleartextPassword(subscriber);
                    break;
            }
        }
        catch (Exception e) {
            //determine whether the nickname was invalid, or the email
            _logger.warn(String.format("got Exception for sub %s", subscriber.toString()));
            if (e.getCause() != null) {
                String message = e.getCause().getMessage();
                if (message != null) {
                    Matcher matcher = _duplicateKeyRegex.matcher(message);
                    if (matcher.matches()) {
                        String duplicateKey = matcher.group(1);
                        String index = matcher.group(2);
                        _logger.info(String.format("duplicate key %s on index '%s' when trying to add subscriber", duplicateKey, index));
                        if (index.equalsIgnoreCase("email_context_id") || (duplicateKey.startsWith("'email://") && index.equalsIgnoreCase("primary_id"))) {
                            throw new EmailAlreadyUsedException(String.format("email '%s' already used when trying to create subscriber %s", subscriber.getEmail(), subscriber.toString()));
                        }
                        else if (index.equalsIgnoreCase("nickname_context_id")) {
                            // if we get here, it means we are missing a nickname from our local cache, add it for future use
                            // this can happen because of subscriber updates on the sync cluster of servers
                            _logger.info(String.format("adding nickname key '%s' to the list of used nicknames", nicknameKey));
                            subscriberNicknames.put(nicknameKey, true);
                            throw new NicknameAlreadyUsedException(String.format("nickname '%s' already used when trying to create subscriber %s", subscriber.getNickname(), subscriber.toString()));
                        }
                        else {
                            _logger.warn(String.format("unknown duplicate key %s on index '%s' when trying to add subscriber", duplicateKey, index), e);
                        }
                    }
                    else {
                        _logger.warn("message for Exception does not matach pattern", e);
                    }
                }
                else {
                    _logger.warn("no message on Exception", e);
                }
            }
            throw e;
        }

        //all new subscribers start with a role of USER
        subscriber.setRole(Subscriber.ROLE.USER);

        //let any registered endpoints know a new user has been added
        for (ServiceEndpoint endpoint : _signupCallbackMap.keySet()) {
            String methodName = _signupCallbackMap.get(endpoint);
            ServiceMessage.send(endpoint, methodName, subscriber.getSubscriberId());
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void emailVerified(final long subscriberId, String email)
    {
        _logger.debug("emailVerified, subscriberId: " + subscriberId + ", email: " + email);

        subscriberDaoMapper.setSubscriberEmailVerified(subscriberId, email);

        //clear out the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber signup(
        int contextId, SignupData signupData, SubscriberSession session)
    throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException,
    InvalidSessionException, NicknameInvalidException, FacebookAuthenticationNeededException
    {
        return signup(contextId, signupData, session, generateEncryptKey(32));
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber signup(
        int contextId, SignupData signupData, SubscriberSession session, String encryptKey)
    throws InvalidEmailException, EmailAlreadyUsedException, FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException,
    InvalidSessionException, NicknameInvalidException, FacebookAuthenticationNeededException
    {
        _logger.debug("doing signup for device: " + session.getDeviceId());

        String fbAccessToken = signupData.getFbAccessToken();
        String email = signupData.getEmail();
        String username = signupData.getUsername();
        String password = signupData.getPassword();

        Subscriber subscriber = new Subscriber();
        subscriber.setContextId(contextId);
        subscriber.setFirstname(NO_NAME);
        subscriber.setLastname(NO_NAME);
        subscriber.setPasswdSet(signupData.isPasswordSet());
        subscriber.setPhone(signupData.getPhone());
        subscriber.setFromCountryCode(signupData.getFromCountryCode());

        FbSubscriber fbSub = null;

        //grab facebook information if an access token was provided
        if (fbAccessToken != null) {
            _logger.debug("doing registration via facebook credentials");

            // check for Facebook already associated with another subscriber
            String facebookId = null;
            facebookId = facebookService.addFacebookUser(fbAccessToken, true);

            fbSub = facebookService.getFacebookUser(facebookId);
            if (fbSub != null) {
                subscriber.setFacebookUserFlag(true);

                if (fbSub.getFirstName() != null) {
                    subscriber.setFirstname(fbSub.getFirstName());
                    _logger.debug("grabbed firstname from facebook: " + subscriber.getFirstname());
                }
                if (fbSub.getLastName() != null) {
                    subscriber.setLastname(fbSub.getLastName());
                    _logger.debug("grabbed lastname from facebook: " + subscriber.getLastname());
                }
                if (fbSub.getEmail() != null) {
                    email = fbSub.getEmail();
                    _logger.debug("grabbed email from facebook: " + email);
                }
                if (fbSub.getUsername() != null) {
                    username = fbSub.getUsername();
                    subscriber.setNicknameSet(true);
                    _logger.debug("grabbed username from facebook: " + username);
                }
                if (fbSub.getPicture() != null) {
                    subscriber.setPhotoUrl(fbSub.getPicture());
                    _logger.debug("grabbed photo from facebook");
                }
                try {
                    subscriber.setPrimaryId(FACEBOOK, ""+contextId, signupData.getFacebookAppId() + "/" + facebookId);
                } catch (URISyntaxException e) {
                    throw new FacebookGeneralException(String.format("could not create primary refId for scheme '%s', contextId %d, facebookAppId '%s' and facebookId '%s'", FACEBOOK.getScheme(), contextId, signupData.getFacebookAppId(), facebookId), e);
                }
            }
        }
        //FUTURE: other types: twitter, google+, etc..

        //make sure email is provided
        if (subscriber.getPrimaryId() == null) {
            if (email == null)
                throw new InvalidEmailException("email is required when not doing Facebook signup");
            try {
                subscriber.setPrimaryId(EMAIL, ""+contextId, email);
            } catch (URISyntaxException e) {
                throw new InvalidEmailException(String.format("could not create primary_identifier for scheme '%s' contextId %d and email '%s'", EMAIL.getScheme(), contextId, email), e);
            }
        }

//caller must have already taken care of this
//        //check for duplicate email address
//        if (subscriberDaoMapper.getSubscriberByEmail(email) != null) {
//            _logger.debug("subscriber has a duplicate email address; ignoring request");
//            throw new EmailAlreadyUsedException(email);
//        }

        //if no password provided, use a generated value
        if (password == null) {
            _logger.debug("user did not provide password; using fuzzybear pw default");
            password = passwordGenerator.getRandomPassword();
        }
        subscriber.setEmail(email);
        subscriber.setPasswd(password);
        subscriber.setEncryptKey(encryptKey);
        subscriber.setEmailSha256Hash(HexUtils.stringToSha256HexString(subscriber.getPrimaryId(), true));
        subscriber.setEmailHashPrefix(subscriber.getEmailSha256Hash().substring(0, 3));
        subscriber.setLanguageCode(signupData.getLanguageCode());

        // New as 7/20/2016
        subscriber.setDateOfBirth(signupData.getDateOfBirth());
        subscriber.setPhotoUrl(signupData.getPhotoUrl());
        subscriber.setPhotoUrlSmall(signupData.getPhotoUrlSmall());
        subscriber.setPhotoUrlLarge(signupData.getPhotoUrlLarge());

        // if username provided, make sure it's valid
        if (username != null) {
            username = Util.makeNicknameValid(username);
            subscriber.setNicknameSet(true);
        } else {
            //set an initial generated username
            username = usernameGenerator.getRandomUsername(signupData.getLanguageCode());
        }
        subscriber.setNickname(username);

        boolean shouldContinue = true;
        int counterSanityCheck = 0;
        while (shouldContinue) {
            counterSanityCheck++;
            try {
                doActualSignup(contextId, subscriber, IIdentityService.PASSWORD_SCHEME.USE_MYSQL_PASSWORD);
                shouldContinue = false;
            } catch (NicknameAlreadyUsedException e) {
                if (counterSanityCheck >= 100) {
                    shouldContinue = false;
                    _logger.warn(String.format("nickname '%s' already used, and we have tried %d times.  Giving up.  Exception message is '%s'", subscriber.getNickname(), counterSanityCheck, e.getMessage()));
                    throw new NicknameInvalidException(String.format("after %d tries, could not get a valid nickname out of starting nickname '%s', ended on nickname '%s", counterSanityCheck, username, subscriber.getNickname()));
                }
                else {
                    _logger.info(String.format("nickname '%s' already used, trying again.  Exception message is '%s'", subscriber.getNickname(), e.getMessage()));
                    subscriber.setNickname(incrementValueOfStringAsIfNumeric(subscriber.getNickname()));
                }
            }
        }


        //add to the username map to keep it up to date
        final String subscriberNicknameKey = makeNicknameKey(contextId, subscriber.getNickname());
//        subscriberNicknameMap.put(subscriberNicknameKey, true);
        subscriberNicknames.put(subscriberNicknameKey, true);

        //add subscriber_session
        if (session == null || session.getDeviceId() == null || session.getDeviceId().trim().length() == 0) {
            throw new InvalidSessionException("missing deviceId");
        }
        session.setSubscriberId(subscriber.getSubscriberId());
        session.setContextId(contextId);
        session.setSessionKey(UUID.randomUUID().toString());
        session.setAddedDate(new Date());
        session.setLastAuthenticatedDate(new Date());
        subscriberDaoMapper.addSubscriberSession(session);
        subscriber.setSubscriberSession(session);

        //cache, since it's going to be asked for soon upon signup completing
        _sessionByIdAndDevice.put(stringAndIntToCompoundKey(session.getDeviceId(), session.getSubscriberId()), session);
        _sessionBySessionKey.put(session.getSessionKey(), session);

        //add subscriber_nickname_history
        //subscriberDaoMapper.addSubscriberNicknameHistory(subscriber.getSubscriberId(), null, username);

        //if facebook user, add facebook identity information
        if (fbAccessToken != null && fbSub != null && fbSub.getFbId() != null) {
            //add identity_fb
            subscriberDaoMapper.addIdentityMappingFacebook(subscriber.getSubscriberId(), fbSub.getFbId(), signupData.getFacebookAppId(), subscriber.getContextId());
        }

        return subscriber;
    }

    /**
     * Given a string, if the last part of the string is numeric, increment it by 1. if it's not numeric, add a 0.
     * @param val
     * @return
     */
    private String incrementValueOfStringAsIfNumeric(String val)
    {
        if (val == null || val.trim().length() == 0) return null;

        //see if the final character is numeric
        if (Character.isDigit(val.charAt(val.length()-1))) {
            String[] parts = val.split("[^\\d]");
            String digitPart = parts[parts.length-1];
            int lastDigit = Integer.parseInt(digitPart);
            lastDigit++;

            String valWithoutLastDigitPart = val.substring(0, val.length() - digitPart.length());
            val = valWithoutLastDigitPart + lastDigit;

        } else {
            return val += "0";
        }

        return val;
    }

    /**
     * Just like signup, but using a different password scheme, and not using any facebook auth
     */
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber signupForeignHostSubscriber(int contextId, SubscriberSession session, SignupData signupData)
    throws InvalidEmailException, NicknameInvalidException, EmailAlreadyUsedException, InvalidSessionException, MissingRequiredParameterException
    {
        //make sure email is provided
        if (signupData.getEmail() == null)
            throw new InvalidEmailException("email is required");

        Subscriber subscriber = new Subscriber();
        try {
            subscriber.setEmail(signupData.getEmail());
            subscriber.setPrimaryId(EMAIL, "" + contextId, signupData.getEmail());
        } catch (URISyntaxException e) {
            throw new InvalidEmailException(String.format("could not create primary_identifier for scheme '%s' contextId %d and email '%s'", EMAIL.getScheme(), contextId, signupData.getEmail()), e);
        }
        subscriber.setContextId(contextId);
        subscriber.setFirstname(signupData.getFirstName() != null ? signupData.getFirstName() : NO_NAME);
        subscriber.setLastname(signupData.getLastName() != null ? signupData.getLastName() : NO_NAME);
        subscriber.setPasswdSet(signupData.isPasswordSet());
        subscriber.setDateOfBirth(signupData.getDateOfBirth());
        subscriber.setAdultFlag(signupData.isAdult());
        subscriber.setPhotoUrl(signupData.getPhotoUrl());
        subscriber.setPhotoUrlSmall(signupData.getPhotoUrlSmall());
        subscriber.setPhotoUrlLarge(signupData.getPhotoUrlLarge());
        subscriber.setPhone(signupData.getPhone());
        subscriber.setPasswd(signupData.getPassword());
        subscriber.setEncryptKey(generateEncryptKey(32));
        subscriber.setEmailSha256Hash(HexUtils.stringToSha256HexString(subscriber.getPrimaryId(), true));
        subscriber.setEmailHashPrefix(subscriber.getEmailSha256Hash().substring(0, 3));
        if (signupData.getFromCountryCode() == null || signupData.getFromCountryCode().length() == 0){
            throw new MissingRequiredParameterException("countryCode required");
        }
        subscriber.setFromCountryCode(signupData.getFromCountryCode());
        subscriber.setRegion(signupData.getRegion());
        if (signupData.getFromCountryCode() == null || signupData.getFromCountryCode().length() == 0){
            throw new MissingRequiredParameterException("languageCode required");
        }
        subscriber.setLanguageCode(signupData.getLanguageCode());

        // if username provided, make sure it's valid
        String username = signupData.getUsername();
        if (username != null) {
            username = Util.makeNicknameValid(username);
            subscriber.setNicknameSet(true);
        } else {
            //set an initial generated username
            username = usernameGenerator.getRandomUsername(signupData.getLanguageCode());
        }
        subscriber.setNickname(username);

        boolean shouldContinue = true;
        int counterSanityCheck = 0;
        while (shouldContinue) {
            counterSanityCheck++;
            try {
                doActualSignup(contextId, subscriber, IIdentityService.PASSWORD_SCHEME.USE_PASSWORD_AS_IS);
                shouldContinue = false;
            } catch (NicknameAlreadyUsedException e) {
                if (counterSanityCheck >= 100) {
                    shouldContinue = false;
                    _logger.warn(String.format("nickname '%s' already used, and we have tried %d times.  Giving up.  Exception message is '%s'", subscriber.getNickname(), counterSanityCheck, e.getMessage()));
                    throw new NicknameInvalidException(String.format("after %d tries, could not get a valid nickname out of starting nickname '%s', ended on nickname '%s", counterSanityCheck, username, subscriber.getNickname()));
                }
                else {
                    _logger.info(String.format("nickname '%s' already used, trying again.  Exception message is '%s'", subscriber.getNickname(), e.getMessage()));
                }
            }
        }

        //add to the username map to keep it up to date
        final String subscriberNicknameKey = makeNicknameKey(contextId, subscriber.getNickname());
        subscriberNicknames.put(subscriberNicknameKey, true);

        //add subscriber_session
        if (session == null || session.getDeviceId() == null || session.getDeviceId().trim().length() == 0) {
            throw new InvalidSessionException("missing deviceId");
        }
        session.setSubscriberId(subscriber.getSubscriberId());
        session.setContextId(contextId);
        session.setSessionKey(UUID.randomUUID().toString());
        session.setAddedDate(new Date());
        session.setLastAuthenticatedDate(new Date());
        subscriberDaoMapper.addSubscriberSession(session);
        subscriber.setSubscriberSession(session);

        //cache, since it's going to be asked for soon upon signup completing
        _sessionByIdAndDevice.put(stringAndIntToCompoundKey(session.getDeviceId(), session.getSubscriberId()), session);
        _sessionBySessionKey.put(session.getSessionKey(), session);

        return subscriber;
    }

    @Override
    @ServiceMethod
    public String generateEncryptKey(int length)
    {
        byte[] randBytes = new byte[length];
        Random rand = new Random();
        for (int i = 0; i < randBytes.length; i++) {
            if (i == randBytes.length / 2)
                // Try to avoid random seed collisions
                rand = new Random();
            randBytes[i] = (byte) (rand.nextInt(94) + 33);
        }
        String s = new String(randBytes);
        s = s.replace("\"", "_"); //replace any " characters with an underscore. makes serialization easier
        s = s.replace("\\", "_"); //replace any \ characters with an underscore. makes dealing with java strings easier
        return s;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public boolean hasRole(long subscriberId, Set<String> validRoles)
    {
        return hasRole(subscriberId, validRoles, false);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public boolean hasRole(long subscriberId, Set<String> validRoles, boolean ignoreSuperuserRole)
    {
        if (!ignoreSuperuserRole) {
            validRoles.add("SUPERUSER");
        }
        String validRolesAsCommaDelimitedList = validRoles.stream().collect(Collectors.joining(","));

        return subscriberDaoMapper.hasRole(subscriberId, validRolesAsCommaDelimitedList);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addRole(long subscriberId, String role)
    {
        subscriberDaoMapper.addRole(subscriberId, role);

        // and then clear out the subscriber information from the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void removeRole(long subscriberId, String role)
    {
        subscriberDaoMapper.removeRole(subscriberId, role);

        // and then clear out the subscriber information from the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<String> getSubscriberRoles(long subscriberId)
    {
        return subscriberDaoMapper.getSubscriberRoles(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Long> getSubscriberIdsWithRole(String role)
    {
        return subscriberDaoMapper.getSubscriberIdsWithRole(role);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber authenticate(int contextId, String email, String password, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException, InvalidSessionException
    {
        _logger.debug(String.format("IdentityService::authenticate::%s begin", email));
        if (session == null) {
            _logger.warn("Unable to authenticate; no SubscriberSession given");
            throw new IllegalArgumentException("SubscriberSession may not be null");
        }

        if (session.getDeviceId() == null)
            throw new InvalidSessionException("no deviceId");
        String deviceId = session.getDeviceId();
        _logger.debug(MessageFormat.format("Authenticating {0} / {1}", email, deviceId));
        _logger.debug(String.format("IdentityService::authenticate::%s calling getSubscriberByEmail", email));
        Subscriber s = getSubscriberByEmail(contextId, email);
        if (s == null) throw new InvalidEmailPasswordException();
        _logger.debug(String.format("IdentityService::authenticate::%s checking password", email));
        boolean passwordValid;
        String existingPassword = s.getPasswd();
        if (existingPassword.startsWith("$s0$")) {
            //new-style password
            passwordValid = encryptionService.scryptCheck(password, existingPassword);
        } else {
            //old-style password
            passwordValid = subscriberDaoMapper.isSubscriberOldstylePasswordValid(s.getSubscriberId(), password);
        }
        if (!passwordValid) throw new InvalidEmailPasswordException();
        _logger.debug(String.format("IdentityService::authenticate::%s calling authenticateRefactor", email));
        authenticateRefactor(contextId, s, session, deviceId);

        return s;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String authenticateForToolUse(int contextId, String email, String password)
    throws InvalidEmailPasswordException, InvalidSessionException
    {
        _logger.info("TOOLSAUTH: doing tools auth for: " + email);

        //make sure subscriber exists
        Subscriber s = getSubscriberByEmail(contextId, email);
        if (s == null) {
            _logger.warn("TOOLSAUTH: no subscriber found for context: " + contextId + ", email: " + email);
            throw new InvalidEmailPasswordException();
        }

        //make sure password is valid
        boolean passwordValid;
        String existingPassword = s.getPasswd();
        if (existingPassword.startsWith("$s0$")) {
            //new-style password
            passwordValid = encryptionService.scryptCheck(password, existingPassword);
        } else {
            //old-style password
            passwordValid = subscriberDaoMapper.isSubscriberOldstylePasswordValid(s.getSubscriberId(), password);
        }
        if (!passwordValid) {
            _logger.warn("TOOLSAUTH: password is invalid");
            throw new InvalidEmailPasswordException();
        }

        //only ADMIN/SUPER can use this method
        if (s.getRole() != Subscriber.ROLE.ADMIN) {
            _logger.warn("TOOLSAUTH: user is not ADMIN, role: " + s.getRole());
            throw new InvalidEmailPasswordException();
        }
        if (s.getAdminRole() != Subscriber.ADMIN_ROLE.SUPER) {
            _logger.warn("TOOLSAUTH: user is not SUPER, admin_role: " + s.getAdminRole());
            throw new InvalidEmailPasswordException();
        }

        //grab the sessions
        List<SubscriberSession> sessions = subscriberDaoMapper.getSubscriberSessions(s.getSubscriberId());
        if (sessions == null || sessions.size() == 0) {
            _logger.warn("TOOLSAUTH: user has no active sessions");
            throw new InvalidSessionException();
        }

        //find the first one with a session key
        for (SubscriberSession ss : sessions) {
            if (ss.getSessionKey() != null && ss.getSessionKey().trim().length() > 0) {
                _logger.info("TOOLSAUTH: tools auth successful, returning session key: " + ss.getSessionKey());
                return ss.getSessionKey();
            }
        }

        //if the code gets here, there are no valid sessions (shouldn't get here)
        _logger.warn("TOOLSAUTH: user has no active sessions (default fallthrough)");
        throw new InvalidSessionException();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber authenticateViaFacebook(int contextId, String accessToken, String facebookAppId, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException,
        FacebookGeneralException, InvalidAccessTokenException, InvalidSessionException, FacebookAuthenticationNeededException
    {
        if (session == null) {
            _logger.warn("Unable to authenticate; no SubscriberSession given");
            throw new IllegalArgumentException("SubscriberSession may not be null");
        }

        if (session.getDeviceId() == null)
            throw new InvalidSessionException("no deviceId");
        String deviceId = session.getDeviceId();
        _logger.debug(MessageFormat.format("Authenticating via facebook. contextId: {0}, accessToken: {1}, deviceId: {2}", contextId, accessToken, deviceId));

        FbSubscriber fbSubscriber = facebookService.getSubscriberInfoByAccessToken(accessToken);
        if (fbSubscriber == null) {
            throw new FacebookGeneralException("unable to authenticate to Facebook using access token");
        }
        _logger.debug(MessageFormat.format(
            "FbSubscriber from Facebook auth: fname: {0}, lname: {1}, email: {2}, username: {3}, fbId: {4}, subscriberId: {5}",
            fbSubscriber.getFirstName(), fbSubscriber.getLastName(), fbSubscriber.getEmail(), fbSubscriber.getUsername(), fbSubscriber.getFbId(), fbSubscriber.getSubscriberId()));

        String fbId = fbSubscriber.getFbId();
        Long subscriberId = subscriberDaoMapper.getSubscriberIdFromFacebookId(fbId, contextId);
        if (subscriberId == null) throw new InvalidEmailPasswordException();

        Subscriber s = getSubscriberById(subscriberId);
        if (s == null) throw new InvalidEmailPasswordException();

        authenticateRefactor(contextId, s, session, deviceId);

        return s;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public Subscriber authenticateViaFacebookOrLink(int contextId, String accessToken, String email, String facebookId, String facebookAppId, SubscriberSession session)
    throws InvalidEmailPasswordException, SubscriberInactiveException, SubscriberRequiresEulaException, SubscriberRequiresUpdateException, FacebookGeneralException, InvalidAccessTokenException,
            InvalidSessionException
    {
        if (session == null) {
            _logger.warn("Unable to authenticate; no SubscriberSession given");
            throw new IllegalArgumentException("SubscriberSession may not be null");
        }

        if (session.getDeviceId() == null)
            throw new InvalidSessionException("no deviceId");
        String deviceId = session.getDeviceId();
        _logger.debug(MessageFormat.format("Authenticating via facebook {0} / {1}", accessToken, deviceId));

        Long subscriberId = subscriberDaoMapper.getSubscriberIdFromFacebookId(facebookId, contextId);
        Subscriber s = null;
        if (subscriberId == null) {
            _logger.info(String.format("no linkage found for fbId %s, checking email '%s' and contextId %d", facebookId, email, contextId));
            s = getSubscriberByEmail(contextId, email);
            if (s == null) throw new InvalidEmailPasswordException();
            else {
                facebookService.addWithAccessTokenAndId(accessToken, facebookId, true);
                List<FacebookIdentityInfo> info = subscriberDaoMapper.getFacebookIdentityInfo(s.getSubscriberId());
                if (info != null && info.size() > 0) {
                    subscriberDaoMapper.updateFacebookMappingForSubscriber(s.getSubscriberId(), facebookId);
                }
                else {
                    subscriberDaoMapper.addIdentityMappingFacebook(s.getSubscriberId(), facebookId, facebookAppId, contextId);
                }
            }
        }
        else {
            s = getSubscriberById(subscriberId);
            if (s == null) throw new InvalidEmailPasswordException();
        }
        authenticateRefactor(contextId, s, session, deviceId);

        return s;
    }

    public void authenticateRefactor(int contextId, Subscriber s, SubscriberSession session, String deviceId)
    throws SubscriberInactiveException, SubscriberRequiresUpdateException
    {
        String email = s.getEmail();

        if (!s.isActiveFlag()) {
            _logger.warn(MessageFormat.format("unable to authenticate subscriber {0,number,#}: subscriber is inactive", s.getSubscriberId()));
            throw new SubscriberInactiveException();
        }
        //if (!s.isEulaFlag()) throw new SubscriberRequiresEulaException(); //FUTURE: don't require this check right now
        if (s.isChangePassword()) {
            _logger.warn(MessageFormat.format("unable to authenticate subscriber: {0,number,#}: subscriber must change password", s.getSubscriberId()));
            throw new SubscriberRequiresUpdateException();
        }

        _logger.debug(String.format("IdentityService::authenticateRefactor::%s(%d) calling getSubscriberSession", email, s.getSubscriberId()));
        //add in session information
        SubscriberSession existingSession = getSubscriberSession(s.getSubscriberId(), deviceId);
        if (existingSession == null) {
            session.setDeviceId(deviceId);
            String newSessionKey = UUID.randomUUID().toString();
            _logger.info(MessageFormat.format("Adding new email/device/session for {0} / {1} / {2}", email, deviceId, newSessionKey));
            session.setSessionKey(newSessionKey);
            session.setContextId(contextId);
            session.setAddedDate(new Date());
            session.setSubscriberId(s.getSubscriberId());
            session.setLastAuthenticatedDate(new Date());
            subscriberDaoMapper.addSubscriberSession(session);
            existingSession = session;
        } else {
            //update it with the latest info from the client
            existingSession.setAppId(session.getAppId());
            existingSession.setAppVersion(session.getAppVersion());
            existingSession.setDeviceModel(session.getDeviceModel());
            existingSession.setDeviceName(session.getDeviceName());
            existingSession.setDeviceVersion(session.getDeviceVersion());
            existingSession.setOsName(session.getOsName());
            existingSession.setOsType(session.getOsType());

            _logger.info(MessageFormat.format("Updating email/device/session for {0} / {1} / {2}", email, deviceId, existingSession.getSessionKey()));
            existingSession.setLastAuthenticatedDate(new Date());
            _sessionByIdAndDevice.put(stringAndIntToCompoundKey(existingSession.getDeviceId(), existingSession.getSubscriberId()), existingSession);
            _sessionBySessionKey.put(existingSession.getSessionKey(), existingSession);
            updateSubscriberSession(existingSession, false);
        }

        //just in case this deviceId has been used by another subscriber before (someone switched phones), remove any other mappings
        List<Long> idsAlreadyUsingThisDeviceId = subscriberDaoMapper.getSubscriberIdsUsingAnOldDeviceId(deviceId);
        if (idsAlreadyUsingThisDeviceId != null) {
            removeDeviceSessionsForThisDeviceAndTheseSubscribers(deviceId, idsAlreadyUsingThisDeviceId, s.getSubscriberId());
        }

        s.setSubscriberSession(existingSession);
    }

    @Override
    @ServiceMethod
    public Subscriber deviceCheckin(SubscriberSession session)
    throws DeviceNotFoundException, InvalidSessionException
    {
        _logger.info(MessageFormat.format("Performing device checkin for subscriber/deviceId: {0,number,#} / {1}", session.getSubscriberId(), session.getDeviceId()));

        //find the device
        SubscriberSession existingSession = getSubscriberSession(session.getSubscriberId(), session.getDeviceId());
        if (existingSession == null) throw new DeviceNotFoundException();

        //verify the session key
        if (session == null || session.getSessionKey() == null || existingSession.getSessionKey() == null ||
            ! existingSession.getSessionKey().equalsIgnoreCase(session.getSessionKey())) {
            throw new InvalidSessionException();
        }

        //update the last auth date
        session.setLastAuthenticatedDate(new Date());
        _sessionByIdAndDevice.put(stringAndIntToCompoundKey(session.getDeviceId(), session.getSubscriberId()), session);
        _sessionBySessionKey.put(session.getSessionKey(), session);
        updateSubscriberSession(session, false);

        Subscriber subscriber = getSubscriberById(session.getSubscriberId());
        subscriber.setSubscriberSession(session);

        return subscriber;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void logout(long subscriberId, String deviceId) throws InvalidSessionException
    {
        if (deviceId == null)
            throw new InvalidSessionException("no deviceId");

        //reset session key
        SubscriberSession session = getSubscriberSession(subscriberId, deviceId);
        if (session != null) {
            if (_logger.isDebugEnabled())
                _logger.info(MessageFormat.format("Logging out subscriber {0,number,#} for device {1} and old session {2}", subscriberId, deviceId, session.getSessionKey()));
            session.setSessionKey(UUID.randomUUID().toString());
            _sessionByIdAndDevice.put(stringAndIntToCompoundKey(session.getDeviceId(), session.getSubscriberId()), session);
            _sessionBySessionKey.put(session.getSessionKey(), session);
            updateSubscriberSession(session, true);
        }
    }

    @Override
    @ServiceMethod
    public void updateFacebookPhotos(long subscriberId)
    throws NotAuthorizedException
    {
        if (subscriberId != 8 && subscriberId != 10 && subscriberId != 12 && subscriberId != 20) {
            throw new NotAuthorizedException("subscriber not authorized");
        }
        updateSubscriberPhotoUrls();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setSubscriberMintParentId(final long subscriberId, long mintParentId)
    {
        subscriberDaoMapper.setSubscriberMintParentId(subscriberId, mintParentId);
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Subscriber> getMintChildren(long subscriberId)
    {
        return subscriberDaoMapper.getMintChildren(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setSubscriberNickname(int contextId, final long subscriberId, String nickname)
    throws NicknameAlreadyUsedException, NicknameInvalidException
    {
        Subscriber s = getSubscriberByNickname(contextId, nickname);
        if (s != null) {
            _logger.warn("unable to set nickname to " + nickname + ", already used");
            throw new NicknameAlreadyUsedException("unable to set nickname to " + nickname + ", already used");
        }

        if (!isNicknameValid(nickname)) {
            // won't actually get here , isNicknameValid throws exception unless valid
            throw new NicknameInvalidException();
        }

        s = getSubscriberById(subscriberId);
        String oldNickname = s.getNickname();
        subscriberDaoMapper.setSubscriberNickname(subscriberId, nickname);
        subscriberDaoMapper.addSubscriberNicknameHistory(subscriberId, oldNickname, nickname);

        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<FacebookIdentityInfo> getFacebookIntegrationInfo(long subscriberId)
    {
        return subscriberDaoMapper.getFacebookIdentityInfo(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public FacebookIdentityInfo getFacebookIntegrationInfoForFacebookApp(long subscriberId, String facebookAppId) {
        return subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<SubscriberProfile> getProfileInfoForFacebookUsers(List<String> facebookIds, int contextId) {
        if (facebookIds == null || facebookIds.size() == 0) {
            return new ArrayList<SubscriberProfile>();
        }
        return xmlSubscriberDaoMapper.getIdentityInfoForFacebookUsers(facebookIds, contextId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public Long getSubscriberIdFromFacebookId(String facebookId, int contextId)
    {
        return subscriberDaoMapper.getSubscriberIdFromFacebookId(facebookId, contextId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public List<Subscriber> getSubscribersByFacebookId(String facebookId) {
        return subscriberDaoMapper.getSubscribersByFacebookId(facebookId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public List<FbSubscriber> getFacebookSubscribers(long subscriberId) throws FacebookAuthenticationNeededException
    {
        List<FacebookIdentityInfo> fbInfo = subscriberDaoMapper.getFacebookIdentityInfo(subscriberId);
        if (fbInfo != null && fbInfo.size() > 0) {
            List<FbSubscriber> subscribers = new ArrayList<FbSubscriber>(fbInfo.size());
            for (FacebookIdentityInfo info : fbInfo) {
                FbSubscriber fbSub = facebookService.getFacebookUser(info.getFacebookId());
                if (fbSub != null) subscribers.add(fbSub);
            }
            return subscribers;
        } else
            return null;
    }

    @Override
    @ServiceMethod
    public Subscriber getSubscriberById(long subscriberId)
    {
//        long start = System.currentTimeMillis();
        Subscriber sub = _subscriberById.get(subscriberId);
        if (sub == null) {
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            txDef.setReadOnly(false);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            try {
                sub = subscriberDaoMapper.getSubscriberById(subscriberId);
                if (sub != null) {
                    populateCacheWithSubscriber(sub);
                }
                transactionManager.commit(txStatus);
                txStatus = null;
            }
            finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }
//        long stop = System.currentTimeMillis();
//        _methodInvocationCounts.addAndGet(IDENTITY_METHOD.getSubscriberById.value(), 1);
//        _methodElapsedTime.addAndGet(IDENTITY_METHOD.getSubscriberById.value(), stop-start);
        return sub;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public Subscriber getSubscriberByPrimaryIdHash(String primaryIdHash)
    {
        return subscriberDaoMapper.getSubscriberByPrimaryIdHash(primaryIdHash);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Subscriber> getSubscribers(List<Long> subscriberIds)
    {
        if (subscriberIds == null || subscriberIds.size() == 0) {
            return new ArrayList<Subscriber>();
        }
        return xmlSubscriberDaoMapper.getSubscribers(subscriberIds);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Subscriber> getSubscribersByPhones(List<String> phones)
    {
        if (phones == null || phones.size() == 0) {
            return new ArrayList<Subscriber>();
        }
        return xmlSubscriberDaoMapper.getSubscribersByPhones(phones);
    }

    @Override
    @ServiceMethod
    public Subscriber getSubscriberByNickname(int contextId, String nickname)
    {
//        long start = System.currentTimeMillis();
        Subscriber result = _subscriberByNicknameAndContextId.get(stringAndIntToCompoundKey(nickname, contextId));
        if (result == null) {
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            txDef.setReadOnly(false);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            try {
                result = subscriberDaoMapper.getSubscriberByNickname(contextId, nickname);
                if (result != null) {
                    populateCacheWithSubscriber(result);
                }
                transactionManager.commit(txStatus);
                txStatus = null;
            }
            finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }
//        long stop = System.currentTimeMillis();
//        _methodInvocationCounts.addAndGet(IDENTITY_METHOD.getSubscriberByNickname.value(), 1);
//        _methodElapsedTime.addAndGet(IDENTITY_METHOD.getSubscriberByNickname.value(), stop-start);
        return result;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Subscriber> getSubscribersByEmail(String email)
    {
        if (email == null || email.trim().length() == 0) {
            return new ArrayList<Subscriber>();
        }
        return subscriberDaoMapper.getSubscribersByEmail(email);
    }

    @Override
    @ServiceMethod
    public Subscriber getSubscriberByEmail(int contextId, String email)
    {
        if (email == null || email.trim().length() == 0) {
            return null;
        }
//TODO: create a lookup map that preloads with EVERY email and keep it up to date to avoid this lookup on a cache miss
// in fact, don't call this at all but just use the map and call it from the signup method
//        long start = System.currentTimeMillis();
        _logger.debug(String.format("IdentityService::getSubscriberByEmail::%s searching HZ cluster", email));
        Subscriber result = _subscriberByEmailAndContextId.get(stringAndIntToCompoundKey(email, contextId));
        _logger.debug(String.format("IdentityService::getSubscriberByEmail::%s results returned", email));
        if (result == null) {
            _logger.debug(String.format("IdentityService::getSubscriberByEmail::%s no sub found, getting transaction", email));
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            txDef.setReadOnly(false);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            _logger.debug(String.format("IdentityService::getSubscriberByEmail::%s got transaction, calling DB", email));
            try {
                result = subscriberDaoMapper.getSubscriberByEmail(contextId, email);
                if (result != null) {
                    _logger.debug(String.format("IdentityService::getSubscriberByEmail::%s caching sub in DB", email));
                    populateCacheWithSubscriber(result);
                }
                transactionManager.commit(txStatus);
                txStatus = null;
            }
            finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }
//        long stop = System.currentTimeMillis();
//        _methodInvocationCounts.addAndGet(IDENTITY_METHOD.getSubscriberByEmail.value(), 1);
//        _methodElapsedTime.addAndGet(IDENTITY_METHOD.getSubscriberByEmail.value(), stop-start);
        return result;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public Subscriber getSubscriberAndUpdatedSessionByEmail(int contextId, String email, SubscriberSession session)
    throws SubscriberInactiveException, SubscriberRequiresUpdateException
    {
        Subscriber s = getSubscriberByEmail(contextId, email);
        authenticateRefactor(contextId, s, session, session.getDeviceId());
        return s;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public Subscriber getSubscriberAndUpdatedSessionById(long subscriberId, SubscriberSession session)
    throws SubscriberInactiveException, SubscriberRequiresUpdateException
    {
        Subscriber s = getSubscriberById(subscriberId);
        int contextId = session.getContextId() > 0 ? session.getContextId() : s.getContextId();
        _logger.debug(String.format("getSubscriberAndUpdatedSessionById: using contextId %d from %s", contextId, session.getContextId() > 0 ? "session object" : "subscriber object"));
        authenticateRefactor(contextId, s, session, session.getDeviceId());
        return s;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public Subscriber getSubscriberByPhone(String phone)
    {
        return subscriberDaoMapper.getSubscriberByPhone(phone);
    }

    @Override
    @ServiceMethod
    public SubscriberSession getSubscriberSession(long subscriberId, String deviceId)
    {
//        long start = System.currentTimeMillis();
        _logger.debug(String.format("IdentityService::getSubscriberSession::%d calling getSubscriberSession", subscriberId));
        String key = stringAndIntToCompoundKey(deviceId, subscriberId);
        SubscriberSession session = _sessionByIdAndDevice.get(key);
        if (session == null) {
            _logger.debug(String.format("IdentityService::getSubscriberSession::%d session not found, getting transaction", subscriberId));
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            _logger.debug(String.format("IdentityService::getSubscriberSession::%d got transaction, getting session from DB", subscriberId));
            try {
                session = subscriberDaoMapper.getSubscriberSession(subscriberId, deviceId);
                if (session != null) {
                    _logger.debug(String.format("IdentityService::getSubscriberSession::%d storing session in Hazelcast", subscriberId));
                    _sessionByIdAndDevice.put(key, session);
                    _sessionBySessionKey.put(session.getSessionKey(), session);
                }
                transactionManager.commit(txStatus);
                txStatus = null;
            }
            finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }
        else {
            _logger.debug(String.format("IdentityService::getSubscriberSession::%d session found", subscriberId));
        }
//        long stop = System.currentTimeMillis();
//        _methodInvocationCounts.addAndGet(IDENTITY_METHOD.getSubscriberSession.value(), 1);
//        _methodElapsedTime.addAndGet(IDENTITY_METHOD.getSubscriberSession.value(), stop-start);
        return session;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addSubscriberSession(SubscriberSession session){
        subscriberDaoMapper.addSubscriberSession(session);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updateSubscriber(Subscriber subscriber)
    throws InvalidSubscriberUpdateException, EmailAlreadyUsedException, InvalidEmailException, NicknameAlreadyUsedException, NicknameInvalidException
    {
        if (subscriber == null) {
            throw new InvalidSubscriberUpdateException("subscriber was null");
        }
        if (subscriber.getContextId() == 0) {
            throw new InvalidSubscriberUpdateException(String.format("Subscriber %s has an invalid contextId", subscriber.toString()));
        }
        Subscriber curSub = subscriberDaoMapper.getSubscriberById(subscriber.getSubscriberId());
        boolean emailRequired = false;
        boolean primaryIdChangeOk = false;
        URI primaryId;
        try {
            primaryId = new URI(curSub.getPrimaryId());
        } catch (URISyntaxException e) {
            String message = String.format("subscriber %d has an invalid primaryId %s", curSub.getSubscriberId(), curSub.getPrimaryId());
            _logger.error(message, e);
            throw new InvalidSubscriberUpdateException(message, e);
        }
        if (primaryId.getScheme().equalsIgnoreCase(EMAIL.getScheme())) {
            emailRequired = true;
        }
        if (curSub.getEmail() != null && curSub.getEmail().equalsIgnoreCase(subscriber.getEmail()) == false) {
            // the email address for this subscriber has changed
            // if the subscriber's primaryId uses the "email://" scheme, then update the primaryId with the new email address
            try {
                if (primaryId.getScheme().equalsIgnoreCase(EMAIL.getScheme())) {
                    // if the primary ID is an email scheme, then we can (and should) update the email in the primary id to the new email
                    subscriber.setPrimaryId(EMAIL, primaryId.getAuthority(), subscriber.getEmail());
                    subscriber.setEmailSha256Hash(HexUtils.stringToSha256HexString(subscriber.getPrimaryId(), true));
                    subscriber.setEmailHashPrefix(subscriber.getEmailSha256Hash().substring(0, 3));
                    primaryIdChangeOk = true;
                }
            } catch (URISyntaxException e1) {
                String message = String.format("subscriber %d has an invalid new primaryId %s://%s/%s", curSub.getSubscriberId(), primaryId.getScheme(), primaryId.getAuthority(), subscriber.getEmail());
                _logger.error(message, e1);
                throw new InvalidEmailException(message, e1);
            }

        }
        if (primaryIdChangeOk == false && subscriber.getPrimaryId().equalsIgnoreCase(curSub.getPrimaryId()) == false) {
            throw new InvalidSubscriberUpdateException(String.format("illegal attempt to change the primaryId of subscriber %d from %s to %s", curSub.getSubscriberId(), curSub.getPrimaryId(), subscriber.getPrimaryId()));
        }
        if (primaryIdChangeOk == false && subscriber.getEmailSha256Hash().equalsIgnoreCase(curSub.getEmailSha256Hash()) == false) {
            throw new InvalidSubscriberUpdateException(String.format("illegal attempt to change the email_sha256_hash of subscriber %d from %s to %s", curSub.getSubscriberId(), curSub.getEmailSha256Hash(), subscriber.getEmailSha256Hash()));
        }
        if (emailRequired && (subscriber.getEmail() == null || subscriber.getEmail().trim().length() == 0)) {
            throw new InvalidEmailException(String.format("subscriber %s has an invalid email", subscriber.toString()));
        }
        if (emailRequired) {
            //make sure the email is a valid email (pattern matching)
            Matcher matcher = EMAIL_PATTERN.matcher(subscriber.getEmail());
            if (!matcher.matches()) {
                throw new InvalidEmailException();
            }
        }
        if (subscriber.getNickname() == null || subscriber.getNickname().trim().length() == 0) {
            throw new InvalidSubscriberUpdateException(String.format("subscriber %s has an invalid nickname", subscriber.toString()));
        }
        Subscriber subMatchingEmail = subscriberDaoMapper.getSubscriberByEmail(subscriber.getContextId(), subscriber.getEmail());
        if (subMatchingEmail != null) {
            if (subMatchingEmail.getSubscriberId() != subscriber.getSubscriberId()) {
                throw new EmailAlreadyUsedException(String.format("attempt to update subscriber %s to a subscriber already using the same email (matching subscriber %s)", subscriber.toString(), subMatchingEmail.toString()));
            }
        }
        Subscriber subMatchingNickname = subscriberDaoMapper.getSubscriberByNickname(subscriber.getContextId(), subscriber.getNickname());
        if (subMatchingNickname != null && subMatchingNickname.getSubscriberId() != subscriber.getSubscriberId()) {
            throw new NicknameAlreadyUsedException(String.format("attempt to update subscriber %s to a subscriber using the same nickname (matching subscriber %s)", subscriber.toString(), subMatchingNickname.toString()));
        }

        if (!isNicknameValid(subscriber.getNickname())) {
            // won't actually get here, isNicknameValid throws exception if it's not valid
            throw new NicknameInvalidException();
        }

        final Long subId = subscriber.getSubscriberId();

        //see if the email address has changed
        Subscriber oldSub = getSubscriberById(subId);

        if (oldSub.getEmail() == null || subscriber.getEmail() == null || !oldSub.getEmail().equals(subscriber.getEmail())) {
            subscriberDaoMapper.setSubscriberEmailNotVerified(subId);
        }
        subscriberDaoMapper.updateSubscriber(subscriber);

        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setSubscriberEmail(int contextId, final long subscriberId, String newEmail)
    throws InvalidEmailException, EmailAlreadyUsedException, InvalidSubscriberUpdateException
    {
        //make sure the email is a valid email (pattern matching)
        Matcher matcher = EMAIL_PATTERN.matcher(newEmail);
        if (!matcher.matches()){
            _logger.debug("incoming email is invalid; ignoring request");
            throw new InvalidEmailException();
        }

        // With Facebook users coming in with no email address (because they signed up with a phone number)
        // this requirement no longer makes sense.  Leaving this here will prevent subscribers from
        // adding a valid email address

//        //make sure this doesn't match an existing facebook subscriber
//        List<FbSubscriber> fbSubs = null;
//        try {
//            fbSubs = getFacebookSubscribers(subscriberId);
//        } catch (FacebookAuthenticationNeededException e1) {
//        }
//        if (fbSubs != null)
//            throw new InvalidEmailException("Facebook subscribers cannot change their email addresses");

        //make sure no other accounts are using this email
        Subscriber s = getSubscriberByEmail(contextId, newEmail);
        if (s != null) {
            _logger.warn(String.format("subscriber exists with email address '%s'; ignoring request", newEmail));
            throw new EmailAlreadyUsedException();
        }

        //update the data
        s = getSubscriberById(subscriberId);
        s.setEmail(newEmail);
        try {
            updateSubscriber(s);
        } catch (NicknameAlreadyUsedException e) {
            // this should not be possible, as we are grabbing the subscriber and then changing just the email
            _logger.error("exception thrown where not expected", e);
        } catch (NicknameInvalidException e) {
            // this should not be possible, as we are grabbing the subscriber and then changing just the email
            _logger.error("exception thrown where not expected", e);
        }

        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setSubscriberPassword(PASSWORD_SCHEME pwScheme, final long subscriberId, String newPassword)
    {
        _logger.debug(MessageFormat.format("setSubscriberPassword a: subscriberId:{0}", subscriberId  + ""));
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                try{
                    clearSubscriberCache(subscriberId);
                }catch(Exception ex){
                    _logger.error(ex.getMessage(), ex);
                }
            }
        });

        switch (pwScheme)
        {
            case USE_MYSQL_PASSWORD:
                //change the password
                String encryptedPassword = encryptionService.scryptEncode(newPassword);
                subscriberDaoMapper.updateSubscriberPassword(subscriberId, encryptedPassword);
                break;

            case USE_PASSWORD_AS_IS:
                subscriberDaoMapper.updateSubscriberPassword(subscriberId, newPassword);
                break;
        }

        //reset all session keys
        List<SubscriberSession> sessions = subscriberDaoMapper.getSubscriberSessions(subscriberId);
        for (SubscriberSession session : sessions) {
            session.setSessionKey(UUID.randomUUID().toString());
            updateSubscriberSession(session, true);
        }

        _logger.debug(MessageFormat.format("setSubscriberPassword b: subscriberId:{0}", subscriberId  + ""));
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setRecruiter(int contextId, final long subscriberId, String recruiterNicknamee)
    throws NicknameInvalidException, RecruiterAlreadySetException
    {
        Subscriber s = getSubscriberById(subscriberId);

        Subscriber recruiter = getSubscriberByNickname(contextId, recruiterNicknamee);
        if (recruiter == null) {
            throw new NicknameInvalidException("recruiter nickname not found");
        }

        if (recruiter.getRole() == Subscriber.ROLE.CELEBRITY) {
            //fill up celebrity rings
            fillNextRing(subscriberId, recruiter.getSubscriberId());

            //optionally set the mint parent
            if (s.getMintParentSubscriberId() == null) {
                subscriberDaoMapper.setSubscriberMintParentId(subscriberId, recruiter.getSubscriberId());
            }

        } else {
            //not a celebrity; just fill the mint parent
            if (s.getMintParentSubscriberId() != null) {
                throw new RecruiterAlreadySetException();
            }
            subscriberDaoMapper.setSubscriberMintParentId(subscriberId, recruiter.getSubscriberId());
        }

        //clear the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public int setSubscriberAddress(SubscriberAddress address)
    {
        address.setCurrent(true);
        subscriberDaoMapper.inactivateSubscriberAddressesOfType(address.getSubscriberId(), address.getType());
        subscriberDaoMapper.addAddress(address);

        return address.getAddressId();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<SubscriberAddress> getSubscriberAddresses(long subscriberId)
    {
        return subscriberDaoMapper.getSubscriberAddresses(subscriberId);
    }

    private SubscriberSession getSubscriberSessionByDeviceSessionKey(String deviceId, String sessionKey, String notused_applicationId, String notused_applicationVersion) throws InvalidSessionException {
//        long start = System.currentTimeMillis();
        SubscriberSession session = _sessionBySessionKey.get(sessionKey);
        if (session == null) {
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            txDef.setReadOnly(false);
            TransactionStatus txStatus = transactionManager.getTransaction(txDef);
            try {
                Subscriber s = subscriberDaoMapper.getSubscriberBySessionKey(sessionKey);
                if (s == null) {
                    _logger.info(MessageFormat.format("subscriber not found for deviceId: {0}, sessionKey: {1}", deviceId, sessionKey));
                    throw new InvalidSessionException();
                }
                //if the session appId/appVersion have changed, update them
                if (deviceId == null)
                    throw new InvalidSessionException("no deviceId");
                session = subscriberDaoMapper.getSubscriberSession(s.getSubscriberId(), deviceId);
                if (session == null) {
                    _logger.warn(MessageFormat.format("session not found for deviceId: {0}, sessionKey: {1}", deviceId, sessionKey));
                    throw new InvalidSessionException();
                }
                if (session.getAppId() == null) {
                    _logger.warn(MessageFormat.format("session does not contain appId for deviceId: {0}, sessionKey: {1}", deviceId, sessionKey));
                    removeDeviceSessionsForThisDeviceAndTheseSubscribers(deviceId, Arrays.asList(s.getSubscriberId()), 0);
                    throw new InvalidSessionException();
                }
                if (session.getAppVersion() == null) {
                    _logger.warn(MessageFormat.format("session does not contain appVersion for deviceId: {0}, sessionKey: {1}", deviceId, sessionKey));
                    removeDeviceSessionsForThisDeviceAndTheseSubscribers(deviceId, Arrays.asList(s.getSubscriberId()), 0);
                    throw new InvalidSessionException();
                }
// GRANT: Do we really need to update a session in this method?  Isn't a check-in a good enough place to do that?
//                if (session != null && applicationId != null && applicationVersion != null) {
//                    if (! applicationId.equals(session.getAppId()) || ! applicationVersion.equals(session.getAppVersion())) {
//                        subscriberDaoMapper.updateSessionAppInfo(s.getSubscriberId(), deviceId, applicationId, applicationVersion);
//                    }
//                }
                _sessionByIdAndDevice.put(stringAndIntToCompoundKey(deviceId, s.getSubscriberId()), session);
                _sessionBySessionKey.put(session.getSessionKey(), session);
                transactionManager.commit(txStatus);
                txStatus = null;
            }
            finally {
                if (txStatus != null) {
                    transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }
        }
//        long stop = System.currentTimeMillis();
//        _methodInvocationCounts.addAndGet(IDENTITY_METHOD.getSubscriberSessionByDeviceSessionKey.value(), 1);
//        _methodElapsedTime.addAndGet(IDENTITY_METHOD.getSubscriberSessionByDeviceSessionKey.value(), stop-start);
        return session;
    }

    @Override
    @ServiceMethod
    public long getSubscriberIdByDeviceSessionKey(String deviceId, String sessionKey, String applicationId, String applicationVersion)
    throws InvalidSessionException
    {
        SubscriberSession session = getSubscriberSessionByDeviceSessionKey(deviceId, sessionKey, applicationId, applicationVersion);
        return session.getSubscriberId();
    }

    @Override
    @ServiceMethod
    public Subscriber getSubscriberByDeviceSessionKey(String deviceId, String sessionKey, String applicationId, String applicationVersion)
    throws InvalidSessionException
    {
        if (deviceId == null)
            throw new InvalidSessionException("no deviceId");

        SubscriberSession session = getSubscriberSessionByDeviceSessionKey(deviceId, sessionKey, applicationId, applicationVersion);
        Subscriber s = getSubscriberById(session.getSubscriberId());
        s.setSubscriberSession(session);
        return s;
    }

//    @Override
//    @ServiceMethod
//    @Transactional(propagation=NESTED,readOnly=false)
//    public boolean isNicknameUnique(String nickname)
//    {
//        return !subscriberDaoMapper.isNicknameUsed(nickname);
//    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<String> findPartialNicknameMatches(String partialNickname)
    {
        if (partialNickname == null || partialNickname.trim().length() == 0) {
            return null;
        }

        partialNickname += "%";
        return subscriberDaoMapper.findPartialNicknameMatches(partialNickname);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public boolean fillNextRing(long subscriberId, long ringSubscriberId)
    {
        //_logger.info(MessageFormat.format("###, fillNextRing, subscriberId: {0,number,#}, ringSubscriberId: {1,number,#}", subscriberId, ringSubscriberId));
        boolean updated = false;
        Subscriber subscriber = getSubscriberById(subscriberId);
        Subscriber ringOwner = getSubscriberById(ringSubscriberId);

        //_logger.info(MessageFormat.format("### got subscriber: {0}, got ringOwner: {1}", subscriber != null, ringOwner != null));
        if (subscriber != null && ringOwner != null) {

            //see if this celebrity is already in one of their rings
            //how can this happen?  the celebrity sent a tweet, the person signed up and got added to the celebrity ring,
            // and now later they are joining that same celebrities vipbox.
            Set<Long> alreadyFilledRingIds = new HashSet<>();
            if (subscriber.getRing1SubscriberId() != null)
                alreadyFilledRingIds.add(subscriber.getRing1SubscriberId());
            if (subscriber.getRing2SubscriberId() != null)
                alreadyFilledRingIds.add(subscriber.getRing2SubscriberId());
            if (subscriber.getRing3SubscriberId() != null)
                alreadyFilledRingIds.add(subscriber.getRing3SubscriberId());
            if (subscriber.getRing4SubscriberId() != null)
                alreadyFilledRingIds.add(subscriber.getRing4SubscriberId());

            if (alreadyFilledRingIds.contains(ringSubscriberId)) {
                //do nothing: this celebrity is already in one of their rings
                return false;
            }

            if (subscriber.getRing1SubscriberId() == null || subscriber.getRing1SubscriberId() == 0) {
                //_logger.info("### setting ring 1");
                subscriber.setRing1SubscriberId(ringSubscriberId);
                updated = true;

            } else if (subscriber.getRing2SubscriberId() == null || subscriber.getRing2SubscriberId() == 0) {
                //_logger.info("### setting ring 2");
                subscriber.setRing2SubscriberId(ringSubscriberId);
                updated = true;

            } else if (subscriber.getRing3SubscriberId() == null || subscriber.getRing3SubscriberId() == 0) {
                //_logger.info("### setting ring 3");
                subscriber.setRing3SubscriberId(ringSubscriberId);
                updated = true;

            } else if (subscriber.getRing4SubscriberId() == null || subscriber.getRing4SubscriberId() == 0) {
                //_logger.info("### setting ring 4");
                subscriber.setRing4SubscriberId(ringSubscriberId);
                updated = true;

            //} else {
            //    _logger.info(MessageFormat.format(
            //        "### all rings are taken!, r1: {0,number,#}, r2: {1,number,#}, r3: {2,number,#}, r4: {3,number,#}",
            //        subscriber.getRing1SubscriberId(), subscriber.getRing2SubscriberId(), subscriber.getRing3SubscriberId(), subscriber.getRing4SubscriberId()));
            }

            if (updated) {
                try {
                    updateSubscriber(subscriber);
                } catch (NicknameAlreadyUsedException e) {
                    // this should not be possible, as we are grabbing the subscriber and then changing just the rings
                    _logger.error("exception thrown where not expected", e);
                } catch (NicknameInvalidException e) {
                    // this should not be possible, as we are grabbing the subscriber and then changing just the rings
                    _logger.error("exception thrown where not expected", e);
                } catch (InvalidSubscriberUpdateException e) {
                    // this should not be possible, as we are grabbing the subscriber and then changing just the rings
                    _logger.error("exception thrown where not expected", e);
                } catch (EmailAlreadyUsedException e) {
                    // this should not be possible, as we are grabbing the subscriber and then changing just the rings
                    _logger.error("exception thrown where not expected", e);
                } catch (InvalidEmailException e) {
                    // this should not be possible, as we are grabbing the subscriber and then changing just the rings
                    _logger.error("exception thrown where not expected", e);
                }
            }
        }

        return updated;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Long> getActiveSubscriberIdsWithDevice()
    {
        return subscriberDaoMapper.getActiveSubscriberIdsWithDevice();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<SubscriberIdAndLanguageCode> getActiveSubscriberIdAndLanguageCodesWithDevice()
    {
        return subscriberDaoMapper.getActiveSubscriberIdAndLanguageCodesWithDevice();
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public int getNumberOfActiveDevicesForSubscriber(long subscriberId)
    {
        return subscriberDaoMapper.getNumberOfActiveDevicesForSubscriber(subscriberId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<SubscriberSessionLight> getSubscribersForSessionTokens(List<String> sessionTokens)
    {
        if (sessionTokens.size() > 0) {
            return xmlSubscriberDaoMapper.getSubscribersForSessionTokens(sessionTokens);
        }
        return null;
    }

    @Override
    @ServiceMethod
    public Integer getContextIdFromDeviceId(long subscriberId, String deviceId)
    {
        SubscriberSession session = getSubscriberSession(subscriberId, deviceId);
        if (session != null) {
            return session.getContextId();
        }
        else {
            _logger.warn(String.format("no contextId found for subscriber %d and device %s", subscriberId, deviceId));
            return null;
        }
    }
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<Subscriber> searchSubs(String searchTerm, int pageIndex, int pageSize) {
        Subscriber idSubscriber = null;
        if (pageIndex == 0)
            try {
                int subId = Integer.parseInt(searchTerm.trim());
                idSubscriber = subscriberDaoMapper.getSubscriberById(subId);
                if (idSubscriber == null)
                    return new ArrayList<Subscriber>();
            } catch (NumberFormatException e) {
                // Ignore - not an id
            }
        List<Subscriber> nickSubscribers = subscriberDaoMapper.searchSubscribersByEmailOrNickname("%"+searchTerm+"%", pageIndex*pageSize, pageSize);
        if (idSubscriber != null)
            nickSubscribers.add(0, idSubscriber);
        return nickSubscribers;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Subscriber> searchSubsByEmail(int contextId, String partialEmail)
    {
        return subscriberDaoMapper.searchSubsByEmail("%"+partialEmail+"%", contextId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void setSubscriberLanguageCode(final long subscriberId, String languageCode)
    {
        subscriberDaoMapper.setSubscriberLanguageCode(subscriberId, languageCode);
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void clearSubscriberCache(long subscriberId)
    {
        Subscriber sub = _subscriberById.get(subscriberId);
        if (sub != null) {
            notifyCachesOfSubscriberChange(subscriberId);

            List<SubscriberSession> sessions = subscriberDaoMapper.getSubscriberSessions(subscriberId);
            for (SubscriberSession session : sessions) {
                String key = stringAndIntToCompoundKey(session.getDeviceId(), session.getSubscriberId());
                notifyCachesOfSessionChange(key);
            }
        }
    }

    //it might seem like a lot, but it processes over 20k nicknames in around 200ms
    private boolean isNicknameValid(String nickname) throws NicknameInvalidException
    {
        //_logger.info("isNicknameValid: >>>" + nickname + "<<<");

        if (nickname == null) throw new NicknameInvalidException("nickname is null");
        String noWhitespaceNicknameToLower = nickname.replaceAll("\\s","").toLowerCase();
        if (noWhitespaceNicknameToLower.length() == 0) throw new NicknameInvalidException("nickname is null");

        for (int i=0; i<nickname.length(); i++) {
            if (!Character.isLetterOrDigit(nickname.charAt(i)) && !_validNonAlphaNumericCharacters.contains(nickname.charAt(i)))
                throw new NicknameInvalidException(String.format("character '%s' in nickname '%s' is not valid", nickname.substring(i, i+1), nickname));
            //else _logger.info("char >>>"+nickname.charAt(i)+"<<< is valid");
        }

        //make sure the list of invalid nicknames is populated
        if (_invalidNicknames == null) {
            _invalidNicknames = subscriberDaoMapper.getInvalidNicknames();
        }

        for (String invalidNickname : _invalidNicknames) {
            if (noWhitespaceNicknameToLower.contains(invalidNickname))
                throw new NicknameInvalidException(String.format("nickname '%s' contains invalid nickname substring '%s'", nickname, invalidNickname));
        }

        if (nickname.startsWith("null") || nickname.matches("^RMA[0123456789]+")) {
            _logger.warn("unable to setSubscriberNickname; invalid: " + nickname, new Exception());
            throw new NicknameInvalidException();
        }

        return true;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public long getTotalSubscriberCountAsOfDate(Date date)
    {
        return subscriberDaoMapper.getTotalSubscriberCountAsOfDate(date);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=false)
    public List<String> getUsernamesInDateRange(Date startDate, Date stopDate)
    {
        return subscriberDaoMapper.getUsernamesInDateRange(startDate, stopDate);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Long> getAllSubscribersForContext(int contextId)
    {
        return subscriberDaoMapper.getAllSubscribersForContext(contextId);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<Long> getSubscriberIdsForRole(ROLE role)
    {
        return subscriberDaoMapper.getSubscriberIdsForRole(role);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String setFacebookAccessToken(final long subscriberId, String accessToken, String facebookAppId)
    throws FacebookGeneralException, InvalidAccessTokenException, FacebookUserExistsException, FacebookAuthenticationNeededException
    {
        Subscriber s = getSubscriberById(subscriberId);
        String facebookId = facebookService.addFacebookUser(accessToken, true);
        subscriberDaoMapper.deleteFacebookMapping(facebookId, s.getContextId()); //just in case there's an old mapping
        subscriberDaoMapper.addIdentityMappingFacebook(subscriberId, facebookId, facebookAppId, s.getContextId());

        //set the facebook-isms data (gameplay.s_subscriber.facebook_user_flag, gameplay.s_subscriber.photo_url)
        String photoUrl = "http://graph.facebook.com/" + facebookId + "/picture";
        subscriberDaoMapper.setSubscriberFacebookInfo(subscriberId, true, photoUrl);

        // and then clear out the subscriber information from the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });

        return facebookId;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void removeFacebookAccessToken(final long subscriberId, String facebookAppId)
    throws FacebookLogoutProhibitedException, FacebookAuthTokenNotFoundException, URISyntaxException
    {
        FacebookIdentityInfo fbInfo = subscriberDaoMapper.getFacebookIdentityInfoForFacebookApp(subscriberId, facebookAppId);
        if (fbInfo != null) {
            Subscriber s = getSubscriberById(subscriberId);
            URI primaryRefId = new URI(s.getPrimaryId());
            if (primaryRefId.getScheme().equalsIgnoreCase(Subscriber.PRIMARY_ID_SCHEME.FACEBOOK.getScheme())) {
                throw new FacebookLogoutProhibitedException();
            }
            else {
                String facebookId = fbInfo.getFacebookId();
                facebookService.removeFacebookUser(fbInfo.getFacebookId());

                //remove the facebook mapping
                subscriberDaoMapper.deleteFacebookMapping(facebookId, s.getContextId()); //just in case there's an old mapping

                //clear the facebook-isms data (gameplay.s_subscriber.facebook_user_flag, gameplay.s_subscriber.photo_url)
                subscriberDaoMapper.setSubscriberFacebookInfo(subscriberId, false, null);

                // and then clear out the subscriber information from the cache
                TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                    @Override
                    public void run() {
                        notifyCachesOfSubscriberChange(subscriberId);
                    }
                });
            }
        }
        else {
            throw new FacebookAuthTokenNotFoundException();
        }
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void markPhoneAsVerified(long subscriberId)
    {
        subscriberDaoMapper.markPhoneAsVerified(subscriberId);

        // and then clear out the subscriber information from the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void updatePhone(long subscriberId, String phone)
    {
        subscriberDaoMapper.updatePhone(subscriberId, phone);

        // and then clear out the subscriber information from the cache
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                notifyCachesOfSubscriberChange(subscriberId);
            }
        });
    }

    /// FOREIGN HOST SUBSCRIBER support ///

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public List<Subscriber> getSubscribersByForeignHostId(String foreignHostSubscriberId) {
        return subscriberDaoMapper.getSubscribersByForeignHostId(foreignHostSubscriberId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public List<ForeignHostIdentityInfo> getForeignHostIdentityInfoList(long subscriberId) {
        return subscriberDaoMapper.getForeignHostIdentityInfoList(subscriberId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public ForeignHostIdentityInfo getForeignHostIdentityInfoForForeignHostApp(long subscriberId, String foreignHostAppId) {
        return subscriberDaoMapper.getForeignHostIdentityInfoForForeignHostApp(subscriberId, foreignHostAppId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public Long getSubscriberIdFromForeignHostId(String foreignHostSubscriberId, int contextId) {
        return subscriberDaoMapper.getSubscriberIdFromForeignHostId(foreignHostSubscriberId, contextId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public void deleteForeignHostMapping(String foreignHostSubscriberId, int contextId) {
        subscriberDaoMapper.deleteForeignHostMapping(foreignHostSubscriberId, contextId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public void updateForeignHostMappingForSubscriber(long subscriberId, String foreignHostSubscriberId) {
        subscriberDaoMapper.updateForeignHostMappingForSubscriber(subscriberId, foreignHostSubscriberId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public void deleteAllForeignHostMappingsForForeignHostId(String foreignHostSubscriberId) {
        subscriberDaoMapper.deleteAllForeignHostMappingsForForeignHostId(foreignHostSubscriberId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public int addIdentityMappingForeignHost(long subscriberId, String foreignHostSubscriberId, String foreignHostAppId, int contextId) {
        return subscriberDaoMapper.addIdentityMappingForeignHost(subscriberId, foreignHostSubscriberId, foreignHostAppId, contextId);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public List<ForeignHostIdentityInfo> getForeignHostSubscriberIds(){
        return subscriberDaoMapper.getForeignHostSubscriberIds();
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public void addSubscriberEmail(SubscriberEmail subscriberEmail)
    {
        subscriberDaoMapper.addSubscriberEmail(subscriberEmail);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED)
    public void verifySubscriberEmail(long subscriberId, String email)
    {
        subscriberDaoMapper.verifySubscriberEmail(subscriberId, email);
    }

    @Override
    @ServiceMethod()
    @Transactional(propagation=NESTED,readOnly=true)
    public List<SubscriberEmail> getSubscriberEmails(long subscriberId)
    {
        return subscriberDaoMapper.getSubscriberEmails(subscriberId);
    }
}
