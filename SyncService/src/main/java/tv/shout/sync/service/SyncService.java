package tv.shout.sync.service;

import static com.meinc.webcollector.message.CollectorMessage.PARM_TO_WDS;
import static com.meinc.webdatastore.domain.WebDataStoreObject.TransferEncoding.BASE64;
import static com.meinc.webdatastore.service.IWebDataStoreService.OPFLAG_PRECACHE;
import static java.util.Calendar.DAY_OF_YEAR;
import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.trigger.domain.Trigger;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;
import com.meinc.webdatastore.util.WdsTools;

import tv.shout.sync.collector.ICollectorMessageHandler;
import tv.shout.sync.collector.SyncMessageHandler;
import tv.shout.sync.dao.ISyncServiceDao;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.util.DateUtil;
import tv.shout.util.JsonUtil;

@Service(
    namespace=       ISyncService.SERVICE_NAMESPACE,
    name=            ISyncService.SERVICE_NAME,
    interfaces=      ISyncService.SERVICE_INTERFACE,
    version=         ISyncService.SERVICE_VERSION,
    exposeAs=        ISyncService.class
)
public class SyncService
implements ISyncService
{
    private static Logger _logger = Logger.getLogger(SyncService.class);

    private final List<String> _validTriggerKeys = Arrays.asList(
        ISyncService.SYNC_MESSAGE_TRIGGER_KEY
    );

    private ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
    private DistributedMap<Long, Date> _subscriberSyncHighWaterMarkMap;

    @Value("${sync.collector.security.key}")
    private String collectorSecurityKey;

    @Autowired
    private ICollectorMessageHandler[] allMessageHandlers;

    @Autowired
    private ISyncServiceDao _dao;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private IWebCollectorService _webCollector;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private IWebDataStoreService _webDataStoreService;

    //hold each of the collector handlers (key=PATH)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlersByPath = new HashMap<String, IMessageTypeHandler>();
    //hold each of the collector handlers (key=MESSAGE_TYPE)
    private Map<String, IMessageTypeHandler> _collectorMessageHandlerByType = new HashMap<String, IMessageTypeHandler>();

    @Override
    @ServiceMethod
    @OnStart
    public void start()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService starting...");
        }

        ServiceEndpoint syncServiceEndpoint = new ServiceEndpoint(ISyncService.SERVICE_NAMESPACE, ISyncService.SERVICE_NAME, ISyncService.SERVICE_VERSION);
        ServiceEndpoint triggerServiceEndpoint = new ServiceEndpoint("meinc-service", "TriggerService", "1.0");

        _subscriberSyncHighWaterMarkMap = DistributedMap.getMap("subscriberSyncHighWaterMarkMap");

        //wait for the trigger service and then register for callbacks
        ServiceMessage.waitForServiceRegistration(triggerServiceEndpoint);
        _triggerService.registerCallback(syncServiceEndpoint, "processSyncMessageTriggerMessages", ISyncService.TRIGGER_SERVICE_ROUTE);

        //register the collector handlers
        registerMessageHandlers(allMessageHandlers);

        _logger.info("SyncService started");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void stop()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopping...");
        }

        //unregister all of the collector handlers
        try {
            /*for (String requestPath : _collectorMessageHandlersByPath.keySet()) {
                _webCollectorService.unregisterMessageTypeHandler(_collectorMessageHandlersByPath.get(requestPath).getHandlerMessageType());
            }*/
            _collectorMessageHandlersByPath.forEach(
                    (k,v) -> _webCollector.unregisterMessageTypeHandler(_collectorMessageHandlersByPath.get(k).getHandlerMessageType()) );

        } catch (Exception e) {
            //oh well; ignore it.
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopped");
        }
    }

    private void registerMessageHandlers(IMessageTypeHandler ... handlers)
    {
        ServiceEndpoint serviceEndpoint = new ServiceEndpoint();
        serviceEndpoint.setNamespace(SERVICE_NAMESPACE);
        serviceEndpoint.setServiceName(SERVICE_NAME);
        serviceEndpoint.setVersion(SERVICE_VERSION);

        /*for (IMessageTypeHandler handler : handlers) {
            _collectorMessageHandlerByType.put(handler.getHandlerMessageType(), handler);

            for (int i=0; i < handler.getCollectorEndpoints().length; i++) {
                _collectorMessageHandlersByPath.put(handler.getCollectorEndpoints()[i].getPath(), handler);
                _webCollectorService.registerMessageTypeHandler(handler.getCollectorEndpoints()[i], handler.getHandlerMessageType(), serviceEndpoint);
            }
        }*/
        Arrays.asList(handlers).forEach(
            (handler) -> {
                _collectorMessageHandlerByType.put(handler.getHandlerMessageType(), handler);

                for (int i=0; i < handler.getCollectorEndpoints().length; i++) {
                    _collectorMessageHandlersByPath.put(handler.getCollectorEndpoints()[i].getPath(), handler);
                    _webCollector.registerMessageTypeHandler(handler.getCollectorEndpoints()[i], handler.getHandlerMessageType(), serviceEndpoint);
                }
            }
        );
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public boolean processSyncMessageTriggerMessages(Trigger trigger)
    {
        //ensure the message is intended for the SyncService
        if (trigger == null || trigger.getPayload() == null || !isMyTrigger(trigger.getKey())) {
            return true;
        }

        //deserialize to a SyncMessage
        TypeReference<SyncMessage> messageTypeRef = new TypeReference<SyncMessage>() {};
        SyncMessage message;
        try {
            message = _jsonMapper.readValue((String)trigger.getPayload(), messageTypeRef);
        } catch (IOException e) {
            _logger.error("unable to deserialize syncMessage from payload: " + trigger.getPayload(), e);
            return true;
        }

        //persist
        _dao.insertSyncMessage(message);

        //store the high water mark in local cache
        _subscriberSyncHighWaterMarkMap.put(message.getSubscriberId(), message.getCreateDate());

        return false;
    }

    @Override
    @ServiceMethod
    public Date sync(long subscriberId, Date since, String contextualId, String toWds)
    {
//_logger.info(">>> sync, subscriberId: " + subscriberId + ", since: " + since + ", contextualId: " + contextualId);
        //if not provided, grab "everything" from forever (TODO: only go back so far)
        boolean sinceNull = false;
        Date clientDate;
        if (since == null) {
            clientDate = new Date(0);
            sinceNull = true;
        } else {
            clientDate = since;
        }
//_logger.info(">>> sync, clientDate: " + clientDate);
//_logger.info(">>> sync, sinceNull: " + sinceNull);

        //grab the high water mark (if not found, assume it was from one week ago to prevent overly large datasets from being returned
        boolean highWaterMarkNull = false;
        Date highWaterMark = _subscriberSyncHighWaterMarkMap.get(subscriberId);
        if (highWaterMark == null) {
            highWaterMark = new Date(0); //TODO: only go back so far
            highWaterMarkNull = true;
        }
//_logger.info(">>> sync, highWaterMark: " + highWaterMark);
//_logger.info(">>> highWaterMarkNull: " + highWaterMarkNull);

        //see if there is anything to sync. if not, do nothing, otherwise drop a sync message onto the processing queue
        //FUTURE: to make this more granular, rather than have one global high water mark, have each individual type of
        // object have it's own high water mark.  That way the specifics of what needs to be generated can be more fine
        // grained rather than all or nothing generation.
        if (clientDate.before(highWaterMark) || (sinceNull == true && highWaterMarkNull == true)) {
//_logger.info(">>> sync, adding a collector message to cause a private sync");
//            _threadPool.execute(new SyncData(subscriberId, eventId, ccId));

            //
            // Create new Collector Message and add to WebCollector queue
            //
            CollectorMessage message = new CollectorMessage();
            message.setMessageType(SyncMessageHandler.COLLECTOR_MESSAGE_TYPE_SYNC_MESSAGE_HANDLER);
            Map<String, String> props = new HashMap<String, String>();
            props.put("subscriberId", subscriberId + "");
            props.put("securityKey", collectorSecurityKey);
            props.put("fromDate", DateUtil.dateToIso8601(clientDate));
            props.put("contextualId", contextualId);
            props.put("__requestPath", SyncMessageHandler.COLLECTOR_SYNC_MESSAGE_PATH);
            props.put(PARM_TO_WDS, toWds);
            message.setProperties(props);
            _webCollector.addMessageToBuffer(message);

        } else {
//_logger.info(">>> sync, nothing to do; NOT creating a private sync collector message. but still touching the document so the client knows something happened");
            //nothing new to report
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("nothing to sync for subscriber {0,number,#} since: {1}", subscriberId, clientDate));
            }

            Subscriber s = _identityService.getSubscriberById(subscriberId);
            writeSyncDoc(s, contextualId, null, clientDate, toWds);
        }

        return highWaterMark;
    }

    @Override
    @ServiceMethod
    public void addSyncMessage(SyncMessage syncMessage)
    throws JsonProcessingException
    {
//_logger.info(">>> adding sync message to trigger: " + syncMessage.getMessageType() + ", sId: " + syncMessage.getSubscriberId());
        _triggerService.enqueue(
                ISyncService.SYNC_MESSAGE_TRIGGER_KEY,
                _jsonMapper.writeValueAsString(syncMessage),
                ISyncService.TRIGGER_SERVICE_ROUTE,
                ISyncService.SERVICE_NAME,
                null,
                0);

//        //persist
//        _dao.insertSyncMessage(syncMessage);
//
//        //store the high water mark in local cache
//        _subscriberSyncHighWaterMarkMap.put(syncMessage.getSubscriberId(), syncMessage.getCreateDate());
    }

    //rather than go through the indirection of the trigger service, just directly store the sync message.
    // the trigger service appears to stop working after a while and messages get lost until the server
    // restarts. this is not acceptable.
    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void addSyncMessageDirect(SyncMessage syncMessage)
    {
        //persist
        _dao.insertSyncMessage(syncMessage);

        //store the high water mark in local cache
        _subscriberSyncHighWaterMarkMap.put(syncMessage.getSubscriberId(), syncMessage.getCreateDate());
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public void generateSyncDocForSubscriber(long subscriberId, Date fromDate, String contextualId, String toWds)
    {
        Subscriber s = _identityService.getSubscriberById(subscriberId);
//_logger.info(">>> sId: " + subscriberId + ", hash: " + s.getEmailSha256Hash());

        // Get all the sync messages since the fromDate
        List<SyncMessage> syncMessages = this.getSyncMessages(subscriberId, fromDate, contextualId);

        // Write messsages out (encrypted) to the subscriber's private location
        this.writeSyncDoc(s, contextualId, syncMessages, fromDate, toWds);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED,readOnly=true)
    public List<SyncMessage> getSyncMessages(long subscriberId, Date fromDate, String contextualId)
    {
        List<SyncMessage> syncMessages;
        if (contextualId != null) {
            syncMessages = _dao.getSyncMessagesWithContext(contextualId, subscriberId, fromDate);
        } else {
            syncMessages = _dao.getSyncMessages(subscriberId, fromDate);
        }
        return syncMessages;
    }

    private void writeSyncDoc(Subscriber s, String contextualId, List<SyncMessage> syncMessages, Date fromDate, String toWds)
    {
        Date lastModifiedDate = new Date();
        Calendar expiresCal = Calendar.getInstance();
        expiresCal.add(DAY_OF_YEAR, 1);

        WebDataStoreObject object = new WebDataStoreObject();
        object.setExpirationDate(expiresCal.getTime());
        object.setCallbackPassthrough("SYNC:" + s.getSubscriberId());
        object.setServiceCallbackEndpoint(new ServiceEndpoint()); //FUTURE: revisit if we ever 'go big' and have many servers

        if (contextualId != null) {
            object.setPath(s.getEmailSha256Hash() + "/" + contextualId + "/syncDoc.json");
        } else {
            object.setPath(s.getEmailSha256Hash() + "/syncDoc.json");
        }

        object.setToWds(toWds);
        object.setPartitionDividend(WdsTools.getSubscriberPartitionDividend(s.getEmailSha256Hash()));

        if (s.getEncryptKey() == null || s.getEncryptKey().trim().isEmpty())
            throw new IllegalStateException("Subscriber " + s.getSubscriberId() + " is missing encryptKey - SKIPPING SYNC PUBLISH!");
        object.setEncryptWithKey(s.getEncryptKey());

        if (syncMessages != null && syncMessages.size() > 0) {

            byte[] document;
            try {
                document = _jsonMapper.writeValueAsBytes(syncMessages);
            } catch (JsonProcessingException e) {
                _logger.error("unable to convert sync messages to json", e);
                touchSyncDoc(object, lastModifiedDate, fromDate);
                return;
            }

            object.setData(document);
            writeDocument(lastModifiedDate, object);

        } else {
            touchSyncDoc(object, lastModifiedDate, fromDate);
        }
    }

    private void writeDocument(Date lastModifiedDate, WebDataStoreObject object)
    {
        object.addResponseHeader("Content-Type", "text/plain");
        object.setLastModifiedDate(lastModifiedDate);
        object.setLastTouchedDate(null);
        object.setTransferEncoding(BASE64);
        //_webDataStoreService.createOrUpdateObjectAsync(object, OPFLAG_PRECACHE);
        try {
//_logger.info(">>> updating doc");
            _webDataStoreService.createOrUpdateObjectSync(object, OPFLAG_PRECACHE);
        } catch (WebDataStoreException | InterruptedException e) {
            _logger.error("Failed to create sync doc", e);
        }
    }

    private void touchSyncDoc(WebDataStoreObject object, Date lastModifiedDate, Date fromDate)
    {
        object.setLastTouchedDate(lastModifiedDate);
        //object.addTransientProperty("sessionKey", subscriberSession.getSessionKey());
        //object.addTransientProperty("syncSinceDate", fromDate);
        //object.addTransientProperty("lastModifiedDate", lastModifiedDate);

        //_webDataStoreService.updateObjectAsync(object, OPFLAG_PRECACHE);

        try {
            _webDataStoreService.updateObjectSync(object, OPFLAG_PRECACHE);
//_logger.info(">>> touched doc");

        } catch (WebDataStoreException | InterruptedException e) {
            //_logger.error("Failed to touch sync doc", e);
            //object doesn't exist; create an empty doc
//_logger.info(">>> creating empty doc");
            object.setData("[]".getBytes());
            writeDocument(lastModifiedDate, object);
        }

    }

    private boolean isMyTrigger(String triggerKey)
    {
        return _validTriggerKeys.contains(triggerKey);
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public String getHandlerMessageType()
    {
        throw new UnsupportedOperationException();
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public CollectorEndpoint[] getCollectorEndpoints()
    {
        throw new UnsupportedOperationException();
    }

    /** INTERNAL METHOD. No public access. */
    @Override
    @ServiceMethod
    public CollectorMessageResult createMessage(String requestPath, Map<String, String> requestHeaders, Map<String, String> requestParameters)
    throws BadRequestException
    {
        IMessageTypeHandler handler = _collectorMessageHandlersByPath.get(requestPath);
        if (handler != null) {
            return handler.createMessage(requestPath, requestHeaders, requestParameters);
        } else {
            _logger.warn("received createMessage for unregistered path type: " + requestPath);
            throw new BadRequestException();
        }
    }

    /** INTERNAL METHOD. No public access. */
    //pass the Message off for handling. (all messages in the list will be of the same type)
    @Override
    @ServiceMethod
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException
    {
        IMessageTypeHandler handler = _collectorMessageHandlerByType.get(messages.get(0).getMessageType());
        if (handler != null) {
            handler.handleMessages(messages, messageBuffer);
        } else {
            _logger.warn("received createMessage for unregistered type: " + messages.get(0).getMessageType());
            throw new BadRequestException();
        }
    }

    @Override
    @ServiceMethod
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        throw new BadRequestException();
    }
}
