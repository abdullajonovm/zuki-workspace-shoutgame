package com.meinc.notification.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.transaction.annotation.Transactional;

import com.meinc.commons.postoffice.exception.PostOfficeException;
import com.meinc.commons.postoffice.service.EmailAddress;
import com.meinc.commons.postoffice.service.EmailPurpose;
import com.meinc.commons.postoffice.service.TemplateEmail;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.http.domain.NotAuthorizedException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnService;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.notification.dao.INotificationServiceDao;
import com.meinc.notification.domain.Notification;
import com.meinc.notification.domain.NotificationCallback;
import com.meinc.notification.domain.NotificationPref;
import com.meinc.notification.domain.NotificationTracker;
import com.meinc.notification.exception.InvalidMessageException;
import com.meinc.notification.exception.InvalidNotifcationException;
import com.meinc.notification.exception.InvalidPrefException;
import com.meinc.notification.exception.NotificationAlreadyHandledException;
import com.meinc.notification.exception.NotificationDeletedException;
import com.meinc.push.exception.PayloadInvalidException;
import com.meinc.push.exception.PayloadTooLargeException;
import com.meinc.push.exception.PushNetworkIoException;
import com.meinc.push.service.IPushService;

import clientproxy.epshttpconnectorservice.EpsHttpConnectorServiceClientProxy;
import clientproxy.postofficeservice.PostOfficeServiceClientProxy;
import tv.shout.util.DateUtil;

@Service(
    namespace = NotificationService.MEINC_NAMESPACE,
    name = NotificationService.SERVICE_NAME,
    interfaces = NotificationService.NOTIFICATION_INTERFACE,
    version = NotificationService.SERVICE_VERSION,
    exposeAs = INotificationService.class
)
public class NotificationService implements INotificationService {
    public static final String MEINC_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "NotificationService";
    public static final String NOTIFICATION_INTERFACE = "INotificationService";
    public static final String SERVICE_VERSION = "1.0";
    private static Logger _logger = Logger.getLogger(NotificationService.class);
    private INotificationServiceDao _dao;
    private IPushService _pushService;
    private Map<String, NotificationHandler> _requestHandlers;
    private Pattern _pathPattern;
    private ObjectMapper _mapper;
    private ReentrantLock _notificationThrottleLock = new ReentrantLock();
    private DistributedMap<Long, NotificationTracker> _notificationThrottle;
    private Map<String, Integer> _messagesPerTypeAndContext;
    private Map<String, Integer> _notificationTypeToPrefType;
    private Map<String, String> _notificationTypeSound;
    private Map<String, String> _notificationTypeCategory;
    private PostOfficeServiceClientProxy _postOffice = new PostOfficeServiceClientProxy();
    private IIdentityService _identityService;

    public NotificationService() {
        _requestHandlers = new HashMap<String, NotificationHandler>();
        _requestHandlers.put("/notification/list", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                String status = request.getFirstParameter("status");
                // if they use the special type of all, set status to null, which will pull back all of them
                if ("all".equalsIgnoreCase(status)) status = null;
                Date startDate = null;
                Date endDate = null;
                startDate = DateUtil.iso8601ToDate(request.getFirstParameter("startDate"));
                endDate = DateUtil.iso8601ToDate(request.getFirstParameter("endDate"));
                List<Notification> _list = getNotificationsForSubscriber(subscriberId, status, startDate, endDate);
                Map<String,Object> _ret = new HashMap<String, Object>();
                _ret.put("success", true);
                _ret.put("notifications", _list);
                String val = _mapper.writeValueAsString(_ret);
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/act", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                int notificationId = Integer.parseInt(request.getFirstParameter("notificationId"));
                String action = request.getFirstParameter("actionTaken");
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                try {
                    actionTaken(notificationId, subscriberId, action);
                    _ret.put("success", true);
                } catch (NotificationAlreadyHandledException e) {
                    _logger.info("notification already handled", e);
                    _ret.put("success", false);
                    _ret.put("alreadyHandled", true);
                } catch (NotificationDeletedException e) {
                    _logger.info("notification deleted", e);
                    _ret.put("success", false);
                    _ret.put("deleted", true);
                }
                String val = _mapper.writeValueAsString(_ret);
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/changeStatus", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                int notificationId = Integer.parseInt(request.getFirstParameter("notificationId"));
                String status = request.getFirstParameter("status");
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> ret = new HashMap<String, Object>();
                changeStatus(notificationId, subscriberId, status);
                ret.put("success", true);
                response.getWriter().println(_mapper.writeValueAsString(ret));
                return response;
            }
        });
        _requestHandlers.put("/notification/send", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                Notification note = new Notification();
                note.setSender(subscriberId);
                int contextId = Integer.parseInt(request.getFirstParameter("contextId"));
                if (contextId != -1) note.setContextId(contextId);
                note.setRecipient(Integer.parseInt(request.getFirstParameter("recipient")));
                note.setType(request.getFirstParameter("type"));
                note.setMessage(request.getFirstParameter("message"));
                note.setActionType(request.getFirstParameter("actionType"));
                note.setStatus(Notification.STATUS_NEW);
                note.setDescription(request.getFirstParameter("description"));
                note.setLastUpdatedBy(subscriberId);
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                try {
                    sendNotification(0, note, null, null, null, null, null);
                    _ret.put("success", true);
                    _ret.put("notificationId", note.getId());
                } catch (InvalidMessageException e) {
                    _ret.put("success", false);
                    _ret.put("invalidMessage", true);
                } catch (InvalidNotifcationException e) {
                    _ret.put("success", false);
                    _ret.put("invalidNotification", e.getMessage());
                }
                String val = _mapper.writeValueAsString(_ret);
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/get", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                int notificationId = Integer.parseInt(request.getFirstParameter("notificationId"));
                Notification note = _dao.getNotificationById(notificationId);
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                if (note == null || note.getSender() != subscriberId && note.getRecipient() != subscriberId) {
                    // don't actually return the notification if it is not either the sender or the recipient
                    _ret.put("success", false);
                    _ret.put("notSenderOrRecipient", true);
                }
                else {
                    _ret.put("success", true);
                    _ret.put("notification", note);
                }
                String val = _mapper.writeValueAsString(_ret);
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/senderDetails", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                String subscriberIdListStr = request.getFirstParameter("subscriberIds");
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> ret = new HashMap<String, Object>();
                if (subscriberIdListStr == null || subscriberIdListStr.trim().length() == 0) {
                    ret.put("success", false);
                    ret.put("invalidIdList", true);
                }
                else {
                    String[] strIdList = subscriberIdListStr.split(",");
                    List<Long> idList = new ArrayList<>(strIdList.length);
                    for (String noteId : strIdList) {
                        Long id = Long.parseLong(noteId);
                        idList.add(id);
                    }

                    //List<Map<String,Object>> nicknames = _mintService.getNicknamesForSubscriberIds(idList);
                    List<Subscriber> subscribers = _identityService.getSubscribers(idList);
                    List<Map<String,Object>> nicknames = new ArrayList<Map<String,Object>>();
                    for (Subscriber s : subscribers) {
                        Map<String, Object> sMap = new HashMap<String, Object>();
                        sMap.put("subscriberId", s.getSubscriberId());
                        sMap.put("nickname", s.getNickname());
                        nicknames.add(sMap);
                    }

                    ret.put("success", true);
                    ret.put("nicknames", nicknames);
                }
                String val = _mapper.writeValueAsString(ret);
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/prefs", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                List<NotificationPref> prefs = getPrefsForSubscriber(subscriberId);
                if (prefs != null) {
                    for (NotificationPref pref : prefs) {
                        pref.setPossibleValues(pref.getPossibleValuesString().split(","));
                    }
                }
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                _ret.put("success", true);
                _ret.put("prefs", prefs);
                String val = _mapper.writeValueAsString(_ret);
                response.getWriter().println(val);
                return response;
            }
        });
        _requestHandlers.put("/notification/updatePref", new NotificationHandler() {
            @Override
            public HttpResponse handleRequest(HttpRequest request, long subscriberId) throws IOException {
                int prefType = Integer.parseInt(request.getFirstParameter("prefType"));
                String value = request.getFirstParameter("value");
                if (value == null || value.trim().length() == 0) {
                    throw new IllegalArgumentException("missing parameter 'value'");
                }
                NotificationPref pref = _dao.getPrefForSubscriberByType(subscriberId, prefType);
                if (pref == null) {
                    pref = new NotificationPref();
                    pref.setPrefType(prefType);
                    pref.setSubscriberId(subscriberId);
                    pref.setCreated(new Date());
                }
                pref.setValue(value);
                pref.setLastUpdated(new Date());
                _dao.setPrefForSubscriber(pref);
                HttpResponse response = new HttpResponse();
                response.setContentType("application/json; charset=utf-8");
                Map<String,Object> _ret = new HashMap<String, Object>();
                _ret.put("success", true);
                String val = _mapper.writeValueAsString(_ret);
                response.getWriter().println(val);
                return response;
            }
        });

        _messagesPerTypeAndContext = new HashMap<String, Integer>();

        _notificationTypeToPrefType = new HashMap<String, Integer>();

        _notificationTypeSound = new HashMap<String, String>();

        _notificationTypeCategory = new HashMap<String, String>();

        _pathPattern = Pattern.compile("^.+?/eps(/notification/[^/]+)(.*)");
        _mapper = new ObjectMapper();
        _mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void setDao(INotificationServiceDao dao) {
        _dao = dao;
    }

    public INotificationServiceDao getDao() {
        return _dao;
    }

    public void setPushService(IPushService service)
    {
        _pushService = service;
    }

    public void setIdentityService(IIdentityService service)
    {
        _identityService = service;
    }

    @Override
    @ServiceMethod
    public void addNotificationType(String name, int prefType)
    {
        _notificationTypeToPrefType.put(name,  prefType);
    }

    @Override
    @ServiceMethod
    public void setNotificationLimit(String name, int limit)
    {
        _messagesPerTypeAndContext.put(name,  limit);
    }

    @Override
    @ServiceMethod
    public void setNotificationSound(String name, String sound)
    {
        _notificationTypeSound.put(name, sound);
    }

    @Override
    @ServiceMethod
    public void setNotificationCategory(String name, String category)
    {
        _notificationTypeCategory.put(name, category);
    }

    @Override
    @ServiceMethod
    @OnStart
    public void load() {
        _notificationThrottle = DistributedMap.getMap("notificationThrottle");
        _logger.debug("loading NotficationService");
        //register to handle get/post events for the /fb endpoint
        ServiceEndpoint myEndpoint = new ServiceEndpoint();
        myEndpoint.setNamespace(MEINC_NAMESPACE);
        myEndpoint.setServiceName(SERVICE_NAME);
        myEndpoint.setVersion(SERVICE_VERSION);

        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        boolean success = httpConnector.registerHttpCallback(myEndpoint, "doGet", "doPost", "/notification", "");
        if (success) {
            _logger.info("registered " + SERVICE_NAME + " to receive doGet/doPost for /notification endpoint");
        }
        else {
            _logger.error("unable to register " + SERVICE_NAME + " to receive doGet/doPost for /notification endpoint");
        }

        _logger.info("NotificationService loaded");
    }

    @Override
    @ServiceMethod
    @OnStop(depends=@OnService(proxy=EpsHttpConnectorServiceClientProxy.class))
    public void unload() {
        EpsHttpConnectorServiceClientProxy httpConnector = new EpsHttpConnectorServiceClientProxy();
        boolean success = httpConnector.unregisterHttpCallback("/notification");
        if (success) {
            _logger.debug("unregister for handling requests to '/notification'");
        }
    }

    @Override
    @ServiceMethod
    public boolean registerCallback(ServiceEndpoint endpoint,
            String methodName, String notificationType)
    {
        NotificationCallback callback = new NotificationCallback();
        callback.setEndpoint(endpoint);
        callback.setMethodName(methodName);
        callback.setType(notificationType);
        if (_dao.getCallbackForEndpoint(callback) == null) {
            _dao.addCallback(callback);
            return true;
        }
        return false;
    }

    @Override
    @ServiceMethod
    public boolean unregisterCallback(ServiceEndpoint endpoint,
            String notificationType)
    {
        NotificationCallback callback = new NotificationCallback();
        callback.setEndpoint(endpoint);
        callback.setType(notificationType);
        int rowsDeleted = _dao.removeCallback(callback);
        if (rowsDeleted > 0) {
            return true;
        }
        return false;
    }

    @Override
    @ServiceMethod
    public HttpResponse doGet(HttpRequest request) {
        return doPost(request);
    }

    @Override
    @ServiceMethod
    public HttpResponse doPost(HttpRequest request) {
        Matcher matcher = _pathPattern.matcher(request.getRequestURL());
        String path = "";
        if (matcher.matches()) {
            path = matcher.group(1);
        }
        _logger.info("request received for URI " + request.getRequestURL() + " (path=" + path + ")");
        NotificationHandler handler = _requestHandlers.get(path);
        if (handler != null) {
//            Map<String,String> headers = request.getHeaders();
//            _logger.info("headers:");
//            for (String key : headers.keySet()) {
//                _logger.info(key + ": " + headers.get(key));
//            }
            HttpResponse response = new HttpResponse();
            boolean shouldReturn = false;
            try {
                final String SESSION_KEY_HEADER = "X-REST-SESSION-KEY";
                final String DEVICE_ID_HEADER = "X-REST-DEVICE-ID";
                final String APPLICATION_ID_HEADER = "X-REST-APPLICATION-ID";
                final String APPLICATION_VERSION_HEADER = "X-REST-APPLICATION-VERSION";
                String sessionKey = request.getHeader(SESSION_KEY_HEADER);
                if (sessionKey == null)
                    throw new NotAuthorizedException();
                String deviceId = request.getHeader(DEVICE_ID_HEADER);
                String applicationId = request.getHeader(APPLICATION_ID_HEADER);
                String applicationVersion = request.getHeader(APPLICATION_VERSION_HEADER);
                long subscriberId = _identityService.getSubscriberIdByDeviceSessionKey(deviceId, sessionKey, applicationId, applicationVersion);
                if (subscriberId == 0) {
                    response.errorOut(400, "subscriber not authorized");
                    return response;
                }
                response = handler.handleRequest(request, subscriberId);
                if (response != null) shouldReturn = true;
            }
            catch (NumberFormatException e) {
                _logger.info("number format exception");
                response.errorOut(400, "invalid parameter type");
                shouldReturn = true;
            }
            catch (IllegalArgumentException e) {
                _logger.info("missing parameter", e);
                response.errorOut(400, "missing parameter");
                shouldReturn = true;
            }
            catch (IOException e) {
                _logger.error("error handling request", e);
                response = new HttpResponse();
                response.setError(500);
                response.setContentType("text/html");
                response.getWriter().print("<html><head><title>Internal Server Errror</title></head><body>Internal Server error</body></html>");
                return response;
            } catch (NotAuthorizedException e) {
                _logger.error("error getting subscriberId: " + e.getMessage());
                response.errorOut(401, "subscriber not authorized");
                shouldReturn = true;
            }
            catch (Throwable t) {
                _logger.error("error handling request: ", t);
                response.errorOut(500, "internal server error");
                shouldReturn = true;
            }
            if (shouldReturn) {
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

    private int getNumNotificationsPerType(String noteType) {
        Integer result = _messagesPerTypeAndContext.get(noteType);
        if (result == null) result = 0;
        return result;
    }

    private boolean hasValidMessage(Notification note, Map<String,String> messagesByDeliveryType, String deliveryType) {
        boolean messageValid = (note.getMessage() != null && note.getMessage().length() <= MAX_MESSAGE_LENGTH);
        if (note.getMessage() == null && messagesByDeliveryType != null) {
            if (deliveryType.equals(INotificationService.COMM_TYPE_APP_PUSH_AND_EMAIL) &&
                    messagesByDeliveryType.get(INotificationService.COMM_TYPE_APP_PUSH) != null
               )
            {
                String message = messagesByDeliveryType.get(INotificationService.COMM_TYPE_APP_PUSH);
                if (message.length() > MAX_MESSAGE_LENGTH) {
                    _logger.info("message '" + message + "' is too long (length is " + message.length() + ")");
                }
                else {
                    note.setMessage(message);
                    messageValid = true;
                }
            }
            else if (deliveryType.equals(INotificationService.COMM_TYPE_SMS_AND_EMAIL) &&
                    messagesByDeliveryType.get(INotificationService.COMM_TYPE_SMS) != null &&
                    messagesByDeliveryType.get(INotificationService.COMM_TYPE_SMS).length() <= MAX_MESSAGE_LENGTH)
            {
                note.setMessage(messagesByDeliveryType.get(INotificationService.COMM_TYPE_SMS));
                messageValid = true;
            }
            else {
                note.setMessage(messagesByDeliveryType.get(deliveryType));
                messageValid = (note.getMessage() != null && note.getMessage().length() <= MAX_MESSAGE_LENGTH);
            }
        }
        return messageValid;
    }

    private String getMessageByDeliveryType(Notification note, Map<String,String> messages, String deliveryType) {
        if (messages != null) {
            return messages.get(deliveryType);
        }
        if (note.getMessage() != null) {
            return note.getMessage();
        }
        return null;
    }

    @Override
    @ServiceMethod
    public void sendNotification(
        int accountId, Notification note, Map<String,String> messagesByDeliveryType, Set<String> appBundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws InvalidMessageException, InvalidNotifcationException
    {
//_logger.warn(MessageFormat.format(">>> sendNotification, accountId: {0,number,#}, type: {1}, recipient: {2,number,#}, appBundleIds: {3}", accountId, note.getType(), note.getRecipient(), appBundleIds));
        if (note.getType() == null || note.getRecipient() < 1) {
//_logger.warn(">>> no note type or no recipient");
            throw new InvalidNotifcationException("missing type or recipient");
        }
        int prefType = PREF_TYPE_GENERAL;
        Integer actualPrefType = _notificationTypeToPrefType.get(note.getType());
        if (actualPrefType != null) {
            prefType = actualPrefType;
        }
        NotificationPref pref = _dao.getPrefForSubscriberByType(note.getRecipient(), prefType);
        String deliveryType = "NONE";
        if (pref != null) deliveryType = pref.getValue();
        boolean messageValid = hasValidMessage(note, messagesByDeliveryType, deliveryType);
        if (!messageValid) {
//_logger.warn(">>> no valid message found");
            _logger.info("no valid message found for subscriber " + note.getRecipient() + " and delivery type " + deliveryType);
            throw new InvalidMessageException();
        }
        // notification throttling
        if (note.getContextId() != 0) {

            boolean shouldReturn = false;

            //FUTURE: don't special case this; make it a callback
            //special case: persist "player on leaderboard" so it survives server restarts
            if ("TYPE_PLAYER_ON_LEADERBOARD".equals(note.getType())) {
                if (_dao.hasSubscriberBeenNotifiedOfLeaderboardTop50ForEvent(note.getRecipient(), note.getContextId())) {
                    _logger.info("subscriber " + note.getRecipient() +
                            " has already reached the limit of 1" +
                            " notification(s) per context for the notification type " + note.getType() +
                            ", dropping notification"
                    );
                    shouldReturn = true;
                } else {
                    _logger.debug("storing notification for leaderboard top50, subscriber: " + note.getRecipient() + ", event: " + note.getContextId());
                    _dao.setSubscriberNotifiedOfLeaderboardTop50ForEvent(note.getRecipient(), note.getContextId());
                }

            //only allow a certain number of notifications for certain types of notifications
            } else {
                _notificationThrottleLock.lock();
                try {
                    NotificationTracker tracker = _notificationThrottle.get(note.getRecipient());
                    if (tracker == null) {
                        tracker = new NotificationTracker();
                        _notificationThrottle.put(note.getRecipient(), tracker);
                    }
                    int numNotifications = tracker.getNotificationsForContext(note.getType(), note.getContextId());
                    int numPerType = getNumNotificationsPerType(note.getType());
                    if (numPerType > 0 && numNotifications >= numPerType) {
                        _logger.info("subscriber " + note.getRecipient() +
                                " has already reached the limit of " + numPerType +
                                " notification(s) per context for notification type " + note.getType() +
                                ", dropping notification"
                        );
                        shouldReturn = true;
                    }
                    else if (numPerType > 0) {
                        tracker.incrementContext(note.getType(), note.getContextId());
                        _notificationThrottle.put(note.getRecipient(), tracker);
                    }
                }
                finally {
                    _notificationThrottleLock.unlock();
                }
            }

            if (shouldReturn) {
//_logger.warn(">>> doing early return; not sending notification");
            return;
            }
        }
//_logger.warn(">>> adding notification to db...");
        _dao.addNotification(note);
        /**
         * 1) Grab the recipient
         * 2) Look up the preferences the recipient has set for notifications
         * 3) Use the appropriate method (SMS, Push, Email) to send the notification
         */
        if (pref == null) {
//_logger.warn(">>> no notification prefs for recipient");
            _logger.info("no notification prefs for recipient " + note.getRecipient());
        }
        if ((deliveryType.equals(COMM_TYPE_APP_PUSH) || deliveryType.equals(COMM_TYPE_APP_PUSH_AND_EMAIL)) && appBundleIds != null) {
            note.setMessage(getMessageByDeliveryType(note, messagesByDeliveryType, INotificationService.COMM_TYPE_APP_PUSH));
            Map<String, Object> msgValues = new HashMap<String, Object>();
            Map<String, Object> aps = new HashMap<String, Object>();
            aps.put("alert", note.getMessage());

            String sound = _notificationTypeSound.get(note.getType());
            if (sound == null) sound = "default";
            aps.put("sound", sound);

            String category = _notificationTypeCategory.get(note.getType());
            if (category != null) {
                aps.put("category", category);
            }

            msgValues.put("aps", aps);
            msgValues.put("notificationId", note.getId());
            msgValues.put("type", "n");
            msgValues.put("notificationType", note.getType());
            msgValues.put("contextId", note.getContextId());

            if (note.getExtras() != null) {
                msgValues.put("extras", note.getExtras());
            }

            try {
//_logger.warn(">>> about to send push to recipient...");
                _pushService.pushNotificationToSubscriber(msgValues, note.getRecipient(), appBundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
//_logger.warn(">>> sent push to recipient");
            } catch (PushNetworkIoException e) {
                _logger.error("could not send notifiction " + note.getId() + " to subscriber " + note.getRecipient(), e);
            } catch (PayloadTooLargeException e) {
                adjustPushMessageForSize(msgValues);
                try {
                    _pushService.pushNotificationToSubscriber(msgValues, note.getRecipient(), appBundleIds, subIdToLanguageCodeMap, languageCodeToNotificationTitleMap, languageCodeToNotificationBodyMap);
                } catch (PushNetworkIoException e1) {
                    _logger.error("could not send notifiction " + note.getId() + " to subscriber " + note.getRecipient(), e);
                } catch (PayloadTooLargeException e1) {
                    _logger.error("could not send notifiction " + note.getId() + " to subscriber " + note.getRecipient(), e);
                } catch (PayloadInvalidException e1) {
                    _logger.error("could not send notifiction " + note.getId() + " to subscriber " + note.getRecipient(), e);
                }
            } catch (PayloadInvalidException e) {
                _logger.error("could not send notifiction " + note.getId() + " to subscriber " + note.getRecipient(), e);
            }
        }
        if (deliveryType.equals(COMM_TYPE_SMS) || deliveryType.equals(COMM_TYPE_SMS_AND_EMAIL)) {
            note.setMessage(getMessageByDeliveryType(note, messagesByDeliveryType, INotificationService.COMM_TYPE_SMS));
            Subscriber s = _identityService.getSubscriberById(note.getRecipient());
            if (s != null && s.getPhone() != null) {
                //old defunct txtwire stuff
            }
            else {
                if (s == null) {
                    _logger.warn("could not send text message to subscriber " + note.getRecipient() + ": no subscriber found");
                }
                else if (s.getPhone() == null) {
                    _logger.warn("could not send text message to subscriber " + note.getRecipient() + ": no phone number");
                }
            }
        }
        if (deliveryType.equals(COMM_TYPE_EMAIL) ||
                deliveryType.equals(COMM_TYPE_APP_PUSH_AND_EMAIL) ||
                deliveryType.equals(COMM_TYPE_SMS_AND_EMAIL))
        {
            note.setMessage(getMessageByDeliveryType(note, messagesByDeliveryType, INotificationService.COMM_TYPE_EMAIL));
            Subscriber s = _identityService.getSubscriberById(note.getRecipient());
            if (s != null) {
                String fromAddress = "info@shout.tv";
                String fromName = "Shout Gameplay";
                try {
                    String fullname = s.getFirstname() + " " + s.getLastname();
                    TemplateEmail email = new TemplateEmail(
                            "shout_rewards_email.html",
                            EmailPurpose.TRANSACTIONAL,
                            new EmailAddress(fromAddress, fromName),
                            new EmailAddress(s.getEmail(), fullname),
                            note.getMessage()
                    );
                    email.disableSection("header");
                    email.disableSection("unsubscribe");
                    String body = note.getDescription() + "<br><br>Open the SHOUT app on your mobile device to respond to this notification.";
                    email.addVariable("htmlBody", body);
//_logger.warn(">>> about to send email to recipient...");
                    _postOffice.sendTemplateEmail(email, s.getContextId(), s.getLanguageCode());
//_logger.warn(">>> sent email to recipient recipient");
                } catch (PostOfficeException e) {
                    _logger.warn("could not send email to '" + s.getEmail() + "': ", e);
                }
            }
            else {
                _logger.warn("could not send email to subscriber " + note.getRecipient() + ": no subscriber found");
            }
        }
    }

    @Override
    @ServiceMethod
    public void changeStatus(int notificationId, long idOfUpdater, String status) {
        Notification note = _dao.getNotificationById(notificationId);
        note.setLastUpdatedBy(idOfUpdater);
        note.setStatus(status);
        note.setLastUpdated(new Date());
        _dao.updateNotification(note);
        List<NotificationCallback> callbacks = _dao.getCallbacksForType(note.getType());
        if (callbacks != null) {
            for (NotificationCallback callback : callbacks) {
                ServiceMessage.send(callback.getEndpoint(), callback.getMethodName(), note, INotificationService.EVENT_STATUS_CHANGED);
            }
        }
    }

    @Override
    @ServiceMethod
    public void actionTaken(int notificationId, long idOfUpdater, String action) throws NotificationAlreadyHandledException, NotificationDeletedException {
        Notification note = _dao.getNotificationById(notificationId);
        if (note.getStatus().equals(Notification.STATUS_DELETED)) {
            throw new NotificationDeletedException("notification '" + notificationId + "' cannot be updated with action '" + action +"', as it has already been deleted");
        }
        if (note.getActionTaken() != null) {
            throw new NotificationAlreadyHandledException("notification '" + notificationId + "' cannot be updated with action '" + action +"', as it has already been handled with action '" + note.getActionTaken() + "'");
        }
        note.setStatus(Notification.STATUS_HANDLED);
        note.setLastUpdatedBy(idOfUpdater);
        note.setLastUpdated(new Date());
        note.setActionTaken(action);
        _dao.updateNotification(note);
        List<NotificationCallback> callbacks = _dao.getCallbacksForType(note.getType());
        if (callbacks != null) {
            for (NotificationCallback callback : callbacks) {
                ServiceMessage.send(callback.getEndpoint(), callback.getMethodName(), note, INotificationService.EVENT_ACTION_TAKEN);
            }
        }
    }

    @Override
    @ServiceMethod
    public List<Notification> getNotificationsForSubscriber(long subscriberId, String status, Date startDate, Date endDate) {
        return _dao.getNotificationsByRecipient(subscriberId, status, startDate, endDate);
    }

    @Override
    @ServiceMethod
    public List<NotificationPref> getPrefsForSubscriber(long subscriberId) {
        return _dao.getPrefsForSubscriber(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void setPrefs(List<NotificationPref> prefs) throws InvalidPrefException {
        if (prefs != null) {
            for (NotificationPref pref : prefs) {
                if (pref.getSubscriberId() == 0 || pref.getPrefType() == 0) {
                    throw new InvalidPrefException("missing subscriberId or prefType");
                }
                if (pref.getValue() == null || pref.getValue().length() == 0) {
                    throw new InvalidPrefException("no value set for pref: " + pref.getName());
                }
                _dao.setPrefForSubscriber(pref);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void adjustPushMessageForSize(Map<String,Object> msgValues) {
        try {
            String _pushStr = _mapper.writeValueAsString(msgValues);
            if (_pushStr.length() > 255) {
                Map<String,Object> aps = (Map<String, Object>) msgValues.get("aps");
                String message = (String)aps.get("alert");
                int overage = (_pushStr.length() - 255) + 3; // + 3 for the '...'
                String newMessage = message.substring(0, message.length() - overage) + "...";
                _logger.info("push message was too long, adjusting from '" + message + "' to '" + newMessage + "'");
                aps.put("alert", newMessage);
            }
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
