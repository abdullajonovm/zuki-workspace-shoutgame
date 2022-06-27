package tv.shout.sync.service;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.trigger.domain.Trigger;
import com.meinc.webcollector.message.handler.IMessageTypeHandler;

import tv.shout.sync.domain.SyncMessage;

public interface ISyncService
extends IMessageTypeHandler
{
    public static final String SYNC_MESSAGE_TRIGGER_KEY = "SYNC_MESSAGE";
    public static final String TRIGGER_SERVICE_ROUTE = "tv.shout.sync.service.syncMessage";

    public static final String SERVICE_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "SyncService";
    public static final String SERVICE_INTERFACE = "ISyncService";
    public static final String SERVICE_VERSION = "1.0";

    void start();
    void stop();

    boolean processSyncMessageTriggerMessages(Trigger trigger);

    /**
     * Perform an asynchronous sync operation.
     *
     * @param subscriberId
     * @param since the last time the client performed a sync operation. If unknown, pass null.
     * @param contextualId if not null, will be the context in which to sync messages (such as a gameId). if null, will be ALL sync messages regardless of contextualId
     * @return the current high water mark for any messages that the server knows about.  If this is after the since parameter, it means
     *        something has changed on the server since the client asked. The server will drop a message on a queue to calculate the
     *        messages and push them to the WebDataStore.  The client should compare the result with the since param to know whether or
     *        not it needs to check the WebDataStore for new messages.  Note that since the call is asynchronous, the client may have to
     *        make one or more calls to the WebDataStore before the new data arrives, depending on server load.
     */
    Date sync(long subscriberId, Date since, String contextualId, String toWds);

    void addSyncMessage(SyncMessage syncMessage) throws JsonProcessingException;
    void addSyncMessageDirect(SyncMessage syncMessage);

    void generateSyncDocForSubscriber(long subscriberId, Date fromDate, String contextualId, String toWds);

    List<SyncMessage> getSyncMessages(long subscriberId, Date fromDate, String contextualId);
}
