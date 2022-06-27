package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.service.IIdentityService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

/**
 * This handler takes a message of type: POOL_PLAY_ROUND
 * <p/>
 * <h2>Payload - a Map<String, Object> containing:
 * <pre>
 * {
 *     "game": {...Game...},
 *     "round": {...Round...},
 *     "subscriberId": [long],
 *     "roundPlayerId": [string],
 *     "pairingWaitTimeMs": [long]
 * }
 * <pre>
 */
public class HandlerBeginPoolPlay
extends BaseHandler
implements SyncMessageSender
{
    private static final String PLAY_POOL_ROUND = "PLAY_POOL_ROUND";
    private static final String BEGIN_POOL_BOT_PLAY_AT = "BEGIN_POOL_BOT_PLAY_AT";

    private static Logger _logger = Logger.getLogger(HandlerBeginPoolPlay.class);

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    @Resource(name="matchMaker")
    protected MatchMaker _matchMaker;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IIdentityService _identityService;

    private Lock _lock = new ReentrantLock();
    private Map<String, Set<Long>> _playersWaitingToBePaired = new HashMap<>();

    public static Message createPlayPoolRoundMessage(Game game, Round round, long subscriberId, String roundPlayerId, long pairingWaitTimeMs)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "round", round,
            "subscriberId", subscriberId,
            "roundPlayerId", roundPlayerId,
            "pairingWaitTimeMs", pairingWaitTimeMs
        );

        return new Message(HandlerBeginPoolPlay.PLAY_POOL_ROUND, payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    //MessageProcessor
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case PLAY_POOL_ROUND:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId(),
                        "subscriberId", payload.get("subscriberId"),
                        "roundPlayerId", payload.get("roundPlayerId"),
                        "pairingWaitTimeMs", payload.get("pairingWaitTimeMs")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerBeginPoolPlay received PLAY_POOL_ROUND\n{0}", JsonUtil.print(map)));
                }
                handlePlayPoolRound(message);
                break;

            case BEGIN_POOL_BOT_PLAY_AT:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId(),
                        "subscriberId", payload.get("subscriberId"),
                        "autoPairAt", payload.get("autoPairAt")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerBeginPoolPlay received BEGIN_POOL_BOT_PLAY_AT\n{0}", JsonUtil.print(map)));
                }
                handleBeginPoolBotPlayAt(message);
                break;

            //else ignore
        }
    }

    private void handlePlayPoolRound(Message message)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        long subscriberId = (Long) payload.get("subscriberId");
        String roundPlayerId = (String) payload.get("roundPlayerId");
        long pairingWaitTimeMs =  (Long) payload.get("pairingWaitTimeMs");
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format(
    "handlePlayPoolRound. game: {0}, round: {1}, sId: {2,number,#}, rpId: {3}, pairingWaitTimeMs: {4}",
    game.getGameName("en"), round.getRoundSequence(), subscriberId, roundPlayerId, pairingWaitTimeMs ));

        Long opponentId = null;

        //make player available for matching
        _matchMaker.addPlayerForMatching(game.getId(), subscriberId, roundPlayerId);

        //see if another player is already waiting to be paired
        _lock.lock();
        try {
            opponentId = getAnotherPlayerWaitingToPair(game.getId());
            if (opponentId != null) {
                removePlayerFromPairWaiting(game.getId(), opponentId);
            }
        } finally {
            _lock.unlock();
        }

        if (opponentId == null) {
            if (game.isPairImmediately()) {
if (_logger.isDebugEnabled()) _logger.debug("no opponent available, pair immediate, creating match with a bot...");
                opponentId = spinUpBot(game.getId(), round.getId());
                _matchMaker.createPoolMatch(game, round, subscriberId, opponentId);

            } else {
                //wait for a bot (unless another player comes along first)
                Map<String, Object> newPayload = new FastMap<>(
                    "game", game,
                    "round", round,
                    "subscriberId", subscriberId,
                    "autoPairAt", System.currentTimeMillis() + pairingWaitTimeMs
                );
if (_logger.isDebugEnabled()) _logger.debug("no opponent available, adding to wait queue...");
                DelayedMessage msg = new DelayedMessage(BEGIN_POOL_BOT_PLAY_AT, newPayload, pairingWaitTimeMs);
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerBeginPoolPlay sending delayed message: " + msg.type);
                }
                _messageBus.sendDelayedMessage(msg);
                markPlayerAsWaiting(game.getId(), subscriberId);
            }

        } else {
if (_logger.isDebugEnabled()) _logger.debug("opponent is available. creating match with another player...");
            //there is an opponent; move on
            _matchMaker.createPoolMatch(game, round, subscriberId, opponentId);
        }
    }

    private void handleBeginPoolBotPlayAt(Message message)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        long subscriberId = (Long) payload.get("subscriberId");
        long autoPairAt = (Long) payload.get("autoPairAt");
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format(
    "handleBeginPoolBotPlayAt. game: {0}, round: {1}, subscriberId: {2,number,#}, autoPairAt: {3,time,yyyy-MM-dd hh:mm:ss.SSS}, now: {4,time,yyyy-MM-dd hh:mm:ss.SSS}",
    game.getGameName("en"), round.getRoundSequence(), subscriberId, autoPairAt, new Date() ));

        _lock.lock();
        try {
            boolean isSubscriberStillWaiting = isSubscriberStillWaiting(game.getId(), subscriberId);
            if (isSubscriberStillWaiting) {
                if (autoPairAt <= System.currentTimeMillis()) {
if (_logger.isDebugEnabled()) _logger.debug("creating match with a bot...");
                    //it's time to pair with a bot

                    //spin up a bot
                    long opponentId = spinUpBot(game.getId(), round.getId());

                    //remove player from waiting
                    removePlayerFromPairWaiting(game.getId(), subscriberId);

                    //move on
                    _matchMaker.createPoolMatch(game, round, subscriberId, opponentId);

                } else {
if (_logger.isDebugEnabled()) _logger.debug("message arrived too early. not yet time to pair with bot. adding back to queue...");
                    //not yet time; put the message back on the queue
                    Map<String, Object> newPayload = new FastMap<>(
                        "game", game,
                        "round", round,
                        "subscriberId", subscriberId,
                        "autoPairAt", autoPairAt
                    );
                    DelayedMessage msg = new DelayedMessage(BEGIN_POOL_BOT_PLAY_AT, newPayload, autoPairAt - System.currentTimeMillis());
                    if (_busLogger.isDebugEnabled()) {
                        _busLogger.debug("HandlerBeginPoolPlay sending delayed message: BEGIN_POOL_BOT_PLAY_AT");
                    }
                    _messageBus.sendDelayedMessage(msg);
                }
            }
else if (_logger.isDebugEnabled()) _logger.debug("subscriber is NOT still waiting. ignoring message");
        } finally {
            _lock.unlock();
        }
    }

    private long spinUpBot(String gameId, String roundId)
    {
        long opponentId = (Long) wrapInTransaction(
            (x) -> {
                List<Long> botSubscriberIds = _botEngine.addBotPlayers(gameId, roundId, 1);
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("spun up bot: {0,number,#}", botSubscriberIds.get(0) ));
                return botSubscriberIds.get(0);
            }, null);

        return opponentId;
    }

    private void markPlayerAsWaiting(String gameId, long subscriberId)
    {
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("marking player as waiting. gameId: {0}, sId: {1,number,#}", gameId, subscriberId));
        Set<Long> waitingPlayers = _playersWaitingToBePaired.get(gameId);
        if (waitingPlayers == null) {
            waitingPlayers = new HashSet<Long>();
            _playersWaitingToBePaired.put(gameId, waitingPlayers);
        }

        waitingPlayers.add(subscriberId);
    }

    public boolean isSubscriberStillWaiting(String gameId, long subscriberId)
    {
        Set<Long> waitingPlayers = _playersWaitingToBePaired.get(gameId);
        if (waitingPlayers == null) {
//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("player is not still waiting. gameId: {0}, sId: {1,number,#}", gameId, subscriberId));
            return false;
        }

//if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("player is {0} still waiting. gameId: {1}, sId: {2,number,#}", waitingPlayers.contains(subscriberId) ? "" : "not", gameId, subscriberId));
        return waitingPlayers.contains(subscriberId);
    }

    private void removePlayerFromPairWaiting(String gameId, long subscriberId)
    {
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("marking player as no longer waiting. gameId: {0}, sId: {1,number,#}", gameId, subscriberId));
        Set<Long> waitingPlayers = _playersWaitingToBePaired.get(gameId);
        if (waitingPlayers == null) return;

        waitingPlayers.remove(subscriberId);
    }

    private Long getAnotherPlayerWaitingToPair(String gameId)
    {
        Set<Long> waitingPlayers = _playersWaitingToBePaired.get(gameId);
        if (waitingPlayers == null || waitingPlayers.size() == 0) {
if (_logger.isDebugEnabled()) _logger.debug("getAnotherPlayerWaitingToPair: no player available");
            return null;
        }

        long opponentId = waitingPlayers.iterator().next();
        waitingPlayers.remove(opponentId);
if (_logger.isDebugEnabled()) _logger.debug(MessageFormat.format("getAnotherPlayerWaitingToPair: {0,number,#}", opponentId));
        return opponentId;
    }

}
