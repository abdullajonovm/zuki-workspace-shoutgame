package tv.shout.snowyowl.reactiveengine.fixedround;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import com.meinc.identity.domain.Subscriber;

import io.socket.client.Socket;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.simplemessagebus.MessageProcessor;
import tv.shout.snowyowl.collector.BaseSmMessageHandler;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.util.FastMap;

public abstract class BaseHandler
implements MessageProcessor
{
    protected Logger _busLogger = Logger.getLogger("messagebus");

    @Autowired
    protected MessageBus _messageBus;

    @Autowired
    private PlatformTransactionManager _transactionManager;

    @Autowired
    protected IDaoMapper _dao;

    protected Socket _socket;

    @Autowired
    protected GameStatsHandler _gameStatsHandler;

    @Override
    public void init(Object config)
    {
        _socket = (Socket) config;
    }

    protected Long getTwitchSubscriberId(String gameId)
    {
        //see if there is a twitch subscriber being followed
        Long twitchSubscriberId = null;

        GameStats gameStats = (GameStats) wrapInTransaction((x) -> {
            return _gameStatsHandler.getGameStats(gameId);
        }, null);

        if (gameStats != null) {
            twitchSubscriberId = gameStats.getTwitchConsoleFollowedSubscriberId();
        }

        return twitchSubscriberId;
    }

    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        return BaseSmMessageHandler.wrapInTransaction(_transactionManager, transactionalMethod, params);
    }

    // --------------------------------
    // util methods for logging
    // --------------------------------

    protected Map<String, Object> getSqaFromPayload(Map<String, Object> payload, String key)
    {
        SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) payload.get(key);
        Map<String, Object> sqaMap = sqa == null ? new HashMap<>() : new FastMap<>(
            "id", sqa.getId(),
            "gameId", sqa.getGameId(),
            "roundId", sqa.getRoundId(),
            "matchId", sqa.getMatchId(),
            "questionId", sqa.getQuestionId(),
            "matchQuestionId", sqa.getMatchQuestionId(),
            "subscriberId", sqa.getSubscriberId(),
            "selectedAnswerId", sqa.getSelectedAnswerId()
        );

        return sqaMap;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getSqasFromPayload(Map<String, Object> payload, String key)
    {
        List<SubscriberQuestionAnswer> sqas = (List<SubscriberQuestionAnswer>) payload.get(key);
        Map<String, Object> sqaMap = new FastMap<>(
            "gameId", sqas.get(0).getGameId(),
            "roundId", sqas.get(0).getRoundId(),
            "matchId", sqas.get(0).getMatchId(),
            "questionId", sqas.get(0).getQuestionId(),
            "matchQuestionId", sqas.get(0).getMatchQuestionId(),

            "id1", sqas.get(0).getId(),
            "subscriberId1", sqas.get(0).getSubscriberId(),
            "selectedAnswerId1", sqas.get(0).getSelectedAnswerId(),

            "id2", sqas.get(1).getId(),
            "subscriberId2", sqas.get(1).getSubscriberId(),
            "selectedAnswerId2", sqas.get(1).getSelectedAnswerId()
        );

        return sqaMap;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getMatchPlayersFromPayload(Map<String, Object> payload, String key)
    {
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) payload.get(key);

        Map<String, Object> matchPlayersMap = new FastMap<>(
            "id1", matchPlayers.get(0).getId(),
            "subscriberId1", matchPlayers.get(0).getSubscriberId(),
            "id2", matchPlayers.get(1).getId(),
            "subscriberId2", matchPlayers.get(1).getSubscriberId()
        );

        return matchPlayersMap;
    }

    @SuppressWarnings("unchecked")
    protected List<Long> getSubscribersFromPayload(Map<String, Object> payload, String key)
    {
        Map<Long, Subscriber> subscribers = (Map<Long, Subscriber>) payload.get(key);

        List<Long> subscriberIds = new ArrayList<>();
        for (long subscriberId : subscribers.keySet()) {
            subscriberIds.add(subscriberId);
        }
        return subscriberIds;
    }
}
