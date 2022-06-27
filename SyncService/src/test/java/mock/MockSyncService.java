package mock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.trigger.domain.Trigger;
import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.IWebCollectorMessageBuffer;
import com.meinc.webcollector.message.handler.BadRequestException;
import com.meinc.webcollector.message.handler.CollectorMessageResult;
import com.meinc.webcollector.message.handler.SyncRequest.HttpRequest;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;

import tv.shout.sync.dao.ISyncServiceDao;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.JsonUtil;

public class MockSyncService
implements ISyncService
{
    private static Logger _logger = Logger.getLogger(MockSyncService.class);

    private final List<String> _validTriggerKeys = Arrays.asList(
            ISyncService.SYNC_MESSAGE_TRIGGER_KEY
        );

    private ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
    private Map<Long, Date> _subscriberSyncHighWaterMarkMap = new HashMap<>();

    private ISyncServiceDao _dao = new MockSyncDao();

    @Override
    public void start()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService starting...");
        }

        _logger.info("SyncService started");
    }

    @Override
    public void stop()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopping...");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopped");
        }
    }

    @Override
    public boolean processSyncMessageTriggerMessages(Trigger trigger)
    {
        //ensure the message is intended for the SyncService
        if (trigger == null || trigger.getPayload() == null || !isMyTrigger(trigger.getKey()))
            return true;

        //deserialize to a SyncMessage
        TypeReference<SyncMessage> messageTypeRef = new TypeReference<SyncMessage>() {};
        SyncMessage message;
        try {
            message = _jsonMapper.readValue((String)trigger.getPayload(), messageTypeRef);
        } catch (IOException e) {
            _logger.error("unable to deserialize syncMessage from payload: " + trigger.getPayload(), e);
            return true;
        }

        _dao.insertSyncMessage(message);

        _subscriberSyncHighWaterMarkMap.put(message.getSubscriberId(), message.getCreateDate());

        return false;
    }

    @Override
    public void addSyncMessage(SyncMessage syncMessage) throws JsonProcessingException
    {
        _dao.insertSyncMessage(syncMessage);

        _subscriberSyncHighWaterMarkMap.put(syncMessage.getSubscriberId(), syncMessage.getCreateDate());
    }

    @Override
    public void addSyncMessageDirect(SyncMessage syncMessage)
    {
        _dao.insertSyncMessage(syncMessage);

        _subscriberSyncHighWaterMarkMap.put(syncMessage.getSubscriberId(), syncMessage.getCreateDate());
    }

    @Override
    public Date sync(long subscriberId, Date since, String contextualId, String toWds)
    {
        //if not provided, grab "everything" from forever
        boolean sinceNull = false;
        Date clientDate;
        if (since == null) {
            clientDate = new Date(0);
            sinceNull = true;
        } else {
            clientDate = since;
        }

        //grab the high water mark (if not found, assume it was from one week ago to prevent overly large datasets from being returned
        boolean highWaterMarkNull = false;
        Date highWaterMark = _subscriberSyncHighWaterMarkMap.get(subscriberId);
        if (highWaterMark == null) {
            highWaterMark = new Date(0);
            highWaterMarkNull = true;
        }

        //see if there is anything to sync. if not, do nothing, otherwise drop a sync message onto the processing queue
        //FUTURE: to make this more granular, rather than have one global high water mark, have each individual type of
        // object have it's own high water mark.  That way the specifics of what needs to be generated can be more fine
        // grained rather than all or nothing generation.
        if (clientDate.before(highWaterMark) || (sinceNull == true && highWaterMarkNull == true)) {
            generateSyncDocForSubscriber(subscriberId, clientDate, contextualId, "foo");

        } else {
            //nothing new to report
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("nothing to sync for subscriber {0,number,#} since: {1}", subscriberId, clientDate));
            }
        }

        return highWaterMark;
    }

    @Override
    public void generateSyncDocForSubscriber(long subscriberId, Date fromDate, String contextualId, String toWds)
    {
        List<SyncMessage> syncMessages;
        if (contextualId != null) {
            syncMessages = _dao.getSyncMessagesWithContext(contextualId, subscriberId, fromDate);
        } else {
            syncMessages = _dao.getSyncMessages(subscriberId, fromDate);
        }

        if (syncMessages != null && syncMessages.size() > 0) {
            try {
                //String doc = _jsonMapper.writeValueAsString(syncMessages);
                String doc = _jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncMessages);

                _logger.info("writing syncdoc for: "+ subscriberId);
                try (PrintStream out = new PrintStream(new FileOutputStream(new File(System.getProperty("user.home") + "/syncdocs", "sync_" + subscriberId + ".json")))) {
                    out.print(doc);
                }

            } catch (JsonProcessingException e) {
                _logger.error("unable to generate sync doc", e);
            } catch (IOException e) {
                _logger.error("unable to store sync doc", e);
            }
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
        throw new UnsupportedOperationException();
    }

    /** INTERNAL METHOD. No public access. */
    //pass the Message off for handling. (all messages in the list will be of the same type)
    @Override
    @ServiceMethod
    public void handleMessages(List<CollectorMessage> messages, IWebCollectorMessageBuffer messageBuffer)
    throws BadRequestException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    @ServiceMethod
    public HttpResponse handleSyncRequest(HttpRequest request)
    throws BadRequestException {
        throw new BadRequestException();
    }

    @Override
    public List<SyncMessage> getSyncMessages(long subscriberId, Date fromDate, String contextualId) {
        // TODO Auto-generated method stub
        return null;
    }

}
