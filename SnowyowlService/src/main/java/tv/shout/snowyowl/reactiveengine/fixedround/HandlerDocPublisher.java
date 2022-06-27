package tv.shout.snowyowl.reactiveengine.fixedround;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.common.WdsPublisher;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.engine.MMECommon;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerDocPublisher
extends BaseHandler
implements WdsPublisher, GamePublisher
{
    private static Logger _logger = Logger.getLogger(HandlerDocPublisher.class);

    private static final String DOC_SUBSCRIBER_GAME_RANK = "DOC_SUBSCRIBER_GAME_RANK";
    private static final String DOC_GAME_WITH_EXTRAS = "DOC_GAME_WITH_EXTRAS";
    private static final String DOC_BRACKET_ROUND_COUNT = "DOC_BRACKET_ROUND_COUNT";
    private static final String DOC_SUBSCRIBER_STATS = "DOC_SUBSCRIBER_STATS";
    private static final String DOC_REPUBLISH_OPEN_INPLAY_GAMES = "DOC_REPUBLISH_OPEN_INPLAY_GAMES";
    private static final String DOC_BRACKET_OUTSTANDING_MATCH_COUNT = "DOC_BRACKET_OUTSTANDING_MATCH_COUNT";

    @Autowired
    protected SubscriberStatsHandler _subscriberStatsHandler;

    @Autowired
    protected MMECommon _mmeCommon;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    private ITriggerService _triggerService;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    @Autowired
    protected IShoutContestService _shoutContestService;

    public static Message getSubscriberGameRankMessage(
        Game game, List<Long> subscriberIds, Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap, List<Long> botIds)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "subscriberIds", subscriberIds,
            "subscriberIdToRoundPlayerMap", subscriberIdToRoundPlayerMap,
            "botIds", botIds
        );

        return new Message(DOC_SUBSCRIBER_GAME_RANK, payload);
    }

    /**
     *
     * @param gameId
     * @param pm optional; pass if you have it, otherwise it will be retrieved
     * @param gp optional; pass if you have it, otherwise it will be retrieved
     * @param gameStats optional; pass if you have it, otherwise it will be retrieved
     * @return .
     */
    public static Message getGameWithExtrasMessage(String gameId, PayoutModel pm, GamePayout gp, GameStats gameStats)
    {
        Map<String, Object> payload = new FastMap<>(
            "gameId", gameId,
            "pm", pm,
            "gp", gp,
            "gameStats", gameStats
        );

        return new Message(DOC_GAME_WITH_EXTRAS, payload);
    }

    public static Message getBracketRoundCountMessage(String gameId, String roundId, int numPlayers)
    {
        Map<String, Object> payload = new FastMap<>(
            "gameId", gameId,
            "roundId", roundId,
            "numPlayers", numPlayers
        );

        return new Message(DOC_BRACKET_ROUND_COUNT, payload);
    }

    public static Message getSubscriberStatsMessage(String gameId, List<Long> playerIdsStillInGame)
    {
        Map<String, Object> payload = new FastMap<>(
            "gameId", gameId,
            "playerIdsStillInGame", playerIdsStillInGame
        );

        return new Message(DOC_SUBSCRIBER_STATS, payload);
    }

    public static Message getRepublishOpenInplayGames()
    {
        Map<String, Object> payload = new FastMap<>(
        );

        return new Message(DOC_REPUBLISH_OPEN_INPLAY_GAMES, payload);
    }

    public static Message getBracketOutstandingMatchCountMessage(Round round, int matchesNotYetProcessingForRound)
    {
        Map<String, Object> payload = new FastMap<>(
            "round", round,
            "matchesNotYetProcessingForRound", matchesNotYetProcessingForRound
        );

        return new Message(DOC_BRACKET_OUTSTANDING_MATCH_COUNT, payload);
    }

    @Override
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case DOC_SUBSCRIBER_GAME_RANK:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_SUBSCRIBER_GAME_RANK");
                }
                handleSubscriberGameRank(message);
                break;

            case DOC_GAME_WITH_EXTRAS:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_GAME_WITH_EXTRAS");
                }
                handleGameWithExtras(message);
                break;

            case DOC_BRACKET_ROUND_COUNT:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_BRACKET_ROUND_COUNT");
                }
                handleBracketRoundCount(message);
                break;

            case DOC_SUBSCRIBER_STATS:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_SUBSCRIBER_STATS");
                }
                handleSubscriberStats(message);
                break;

            case DOC_REPUBLISH_OPEN_INPLAY_GAMES:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_REPUBLISH_OPEN_INPLAY_GAMES");
                }
                handleRepublishOpenInplayGames(message);
                break;

            case DOC_BRACKET_OUTSTANDING_MATCH_COUNT:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerDocPublisher received: DOC_BRACKET_OUTSTANDING_MATCH_COUNT");
                }
                handleBracketOutstandingMatchCount(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleBracketOutstandingMatchCount(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Round round = (Round) payload.get("round");
        int matchesNotYetProcessingForRound = (Integer) payload.get("matchesNotYetProcessingForRound");

        /*int totalMatchesForRound = */_mmeCommon.publishBracketOutstandingMatchCount(round, matchesNotYetProcessingForRound, _socket, _triggerService);
    }

    private void handleRepublishOpenInplayGames(Message message)
    {
        List<String> gameIds = new ArrayList<>();

        wrapInTransaction((x) -> {

            gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(
                    ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.DEFAULT, ISnowyowlService.GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

            gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(
                    ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.TESTER, ISnowyowlService.TEST_GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

            return null;
        }, null);

        for (String gameIdToPublish : gameIds) {
            if (_busLogger.isDebugEnabled()) {
                _busLogger.debug("HandlerDocPublisher sending message: DOC_GAME_WITH_EXTRAS");
            }
            _messageBus.sendMessage(getGameWithExtrasMessage(gameIdToPublish, null, null, null));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSubscriberStats(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        String gameId = (String) payload.get("gameId");
        List<Long> playerIdsStillInGame = (List<Long>) payload.get("playerIdsStillInGame");

        _subscriberStatsHandler.publishSubscriberStatsDocuments(gameId, playerIdsStillInGame);
    }

    @SuppressWarnings("unchecked")
    private void handleBracketRoundCount(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        String gameId = (String) payload.get("gameId");
        String roundId = (String) payload.get("roundId");
        int numPlayers = (Integer) payload.get("numPlayers");

        //publish the doc
        String path = "/" + gameId + "/" + "numplayers_" + roundId + ".json";
        Map<String, Object> data = new FastMap<>("count", numPlayers);
        publishJsonWdsDoc(_logger, _wdsService, null, path, data);
    }

    @SuppressWarnings("unchecked")
    private void handleGameWithExtras(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        String gameId = (String) payload.get("gameId");
        PayoutModel pm = (PayoutModel) payload.get("pm");
        GamePayout gp = (GamePayout) payload.get("gp");
        GameStats gameStats = (GameStats) payload.get("gameStats");

        wrapInTransaction((x) -> {
            publishGameWithExtrasToWds(gameId, pm, gp, _gameStatsHandler, gameStats, _dao, _shoutContestService);
            return null;
        }, null);
    }

    @SuppressWarnings("unchecked")
    private void handleSubscriberGameRank(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        List<Long> subscriberIds = (List<Long>) payload.get("subscriberIds");
        Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap = (Map<Long, RoundPlayer>) payload.get("subscriberIdToRoundPlayerMap");
        List<Long> botIds = (List<Long>) payload.get("botIds");

        //grab all the data
        Object[] obj = (Object[]) wrapInTransaction((x) -> {
            List<PayoutTableRow> payoutTableRows = _dao.getPayoutTableRows(game.getId());
            return new Object[] {payoutTableRows};
        }, null);

        List<PayoutTableRow> payoutTableRows = (List<PayoutTableRow>) obj[0];

        //see how many people won and are moving on
        int numPeopleStillPlaying = 0;
        for (long subscriberId : subscriberIds) {
            RoundPlayer roundPlayer = subscriberIdToRoundPlayerMap.get(subscriberId);
            if (roundPlayer == null) {
                _logger.error("NO roundPlayer for subscriberId: " + subscriberId, new Exception());
            }
            if (roundPlayer.getDetermination() != null &&
                    (roundPlayer.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.WON || roundPlayer.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED)) {
                numPeopleStillPlaying++;
            }
        }

        //find the current lowest value that everyone still in the game will be at or above and see if there is a row in the payout table for it
        String currentMinPayoutRowId = null;
        for (PayoutTableRow payoutTableRow : payoutTableRows) {
            if (numPeopleStillPlaying >= payoutTableRow.getRankTo()) { //even though stored as a double, it was put in as an int so the cast is safe
                currentMinPayoutRowId = payoutTableRow.getRowId();
                break;
            }
        }

        //for each subscriber (that isn't a bot and that played in this round), publish a doc showing their current payout AND send a socket io message with the same information
        for (long subscriberId : subscriberIds) {
            //filter out any bots
            if (botIds.contains(subscriberId)) {
                continue;
            }

            RoundPlayer roundPlayer = subscriberIdToRoundPlayerMap.get(subscriberId);
            String payoutTableRowUuid = null;

            //if the player hasn't been eliminated, they will get AT LEAST the min current payout
            if (roundPlayer.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.WON || roundPlayer.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED) {
                payoutTableRowUuid = currentMinPayoutRowId;

            } else {
                //find out where they are in the table. they were eliminated and the position/payout won't change.they will make AT LEAST this much
                for (PayoutTableRow payoutTableRow : payoutTableRows) {
                    if (roundPlayer.getRank().intValue() <= payoutTableRow.getRankTo()) { //even though stored as a double, it was put in as an int so the cast is safe
                        payoutTableRowUuid = payoutTableRow.getRowId();
                        break;
                    }
                }
            }

            //send the socket.io message
            Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
            sendCurrentRankSocketIoMessage(subscriber, payoutTableRowUuid);

            //publish the doc
            String path = "/" + subscriber.getEmailSha256Hash() + "/" + game.getId() + "_rank.json";
            Map<String, Object> data = new FastMap<>("currentRank", payoutTableRowUuid);
            publishJsonWdsDoc(_logger, _wdsService, null, path, data);
        }
    }

    protected void sendCurrentRankSocketIoMessage(Subscriber subscriber, String payoutTableRowUuid)
    {
        //send as a socket.io message
        if (_socket != null) {
            Map<String, Object> curRankMap = new HashMap<>();
            curRankMap.put("currentRank", payoutTableRowUuid);

            Map<String, Object> msg = new HashMap<>();
            msg.put("recipient", subscriber.getEmailSha256Hash());
            msg.put("message", curRankMap);

            ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
            String message;
            try {
                message = _jsonMapper.writeValueAsString(msg);
                SocketIoLogger.log(_triggerService, subscriber.getSubscriberId(), "send_current_rank", message, "SENDING");
                _socket.emit("send_current_rank", message);
                SocketIoLogger.log(_triggerService, subscriber.getSubscriberId(), "send_current_rank", message, "SENT");
            } catch (JsonProcessingException e) {
                _logger.warn("unable to emit socket.io message", e);
            }
        }
    }

}
