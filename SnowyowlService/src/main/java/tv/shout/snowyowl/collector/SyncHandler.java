package tv.shout.snowyowl.collector;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint;
import com.meinc.webcollector.service.IWebCollectorService.CollectorEndpoint.ConnectionType;

import tv.shout.collector.CollectorEndpointHandler;
import tv.shout.collector.PublishResponseError;
import tv.shout.sc.domain.Game;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;

public class SyncHandler
extends BaseSmMessageHandler
implements SyncMessageSender
{
//    private static org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(SyncHandler.class);

    // Valid form post param names
    private static final List<String> _validFormVars = Arrays.asList(
        "gameId", "fromDate"
    );

    @Autowired
    private ISyncService _syncService;

    @Override
    public String getHandlerMessageType()
    {
        return "SNOWL_SYNCAPI";
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

            new CollectorEndpointHandler(new CollectorEndpoint("/snowl/game/getSyncMessages", ConnectionType.ANY))
                .withValidRoles(new HashSet<>(Arrays.asList()))
                .withMessageHandlerFunction(
                    (message, logMessageTag) ->
                    getSyncMessages(message.getProperties(), message.getMessageId())),

        };

        for (CollectorEndpointHandler handler : handlers) {
            _collectorEndpointHandlerByPath.put(handler.getCollectorEndpoint().getPath(), handler);
        }

        return Arrays.stream(handlers)
                .map(CollectorEndpointHandler::getCollectorEndpoint)
                .toArray(CollectorEndpoint[]::new);
    }

    private Map<String, Object> getSyncMessages(Map<String, String> props, String messageId)
    throws PublishResponseError
    {
        String docType = "getSyncMessages";
        Map<String, List<SyncMessage>> messagesMap = new HashMap<>();

        Subscriber subscriber = getSubscriber(props, messageId, docType);
        Date fromDate = getDateParamFromProps(props, messageId, docType, "fromDate", true);

        String gameId = getParamFromProps(props, messageId, docType, "gameId", false);

        //if gameId is given, retrieve just for that game
        if (gameId != null) {
            List<SyncMessage> syncMessages = _syncService.getSyncMessages(subscriber.getSubscriberId(), fromDate, gameId);
            messagesMap.put(gameId, syncMessages);

        } else {
            //get all the OPEN/INPLAY games that the subscriber is part of and retrieve the sync messages for each of them
            List<String> gameIds = _shoutContestService.getSubscriberGameIdsByStatuses(subscriber.getContextId(), subscriber.getSubscriberId(), Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY);
            for (String gId : gameIds) {
                List<SyncMessage> syncMessages = _syncService.getSyncMessages(subscriber.getSubscriberId(), fromDate, gId);
                messagesMap.put(gId, syncMessages);
            }
        }

        Map<String, Object> result = new FastMap<>(
            "syncMessages", messagesMap);

        return result;
    }

}
