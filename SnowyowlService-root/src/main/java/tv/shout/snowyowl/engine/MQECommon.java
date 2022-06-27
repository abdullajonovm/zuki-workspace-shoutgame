package tv.shout.snowyowl.engine;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.distdata.visor.DistributedVisor;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchQueue;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.common.FileHandler;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class MQECommon
implements FileHandler, SyncMessageSender, GamePublisher
{
    private static Logger _logger = Logger.getLogger(MQECommon.class);
    private static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    private static long WAIT_LOCK_RETRY = 250L;
    private static long WAIT_TIME_FOR_PAIRING_TIMEOUT_CHECK = 1_000L;

    private static long WAIT_TIME_TIMEOUT_DEFAULT = 10_000L;
    private static long WAIT_TIME_TIMEOUT_IMMEDIATE = 1_000L;

    @Value("${sm.engine.statedir}")
    protected String _stateDir;

    @Autowired
    private IDaoMapper _dao;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    protected ISyncService _syncService;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    @Autowired
    protected BotEngine _botEngine;

    //keep track of how long a subscriber has been waiting to get paired (key: subscriberId, val: timestamp)
    private Map<Long, Long> _subscriberMatchQueueWaitingMap = new ConcurrentHashMap<>();

    public void subscriberCancelledQueuing(long subscriberId)
    {
        _subscriberMatchQueueWaitingMap.remove(subscriberId);
    }

    public Map<Long, Long> getSubscriberMatchQueueWaitingMap()
    {
        return _subscriberMatchQueueWaitingMap;
    }

    public List<Game> getGamesForProcessing(String engineType)
    {
        List<Game> games = _shoutContestService.getGamesByStatusAndEngine(ISnowyowlService.GAME_ENGINE, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY);
        return games.stream()
            //.peek(game -> _logger.info("\tgame engine type: " + game.getEngineType()))
            .filter(game -> game.getEngineType().equals(engineType))
            .collect(Collectors.toList());
    }

    public void processGame(Game game, Consumer<Void> runMethod, String filePrefix, Socket socketIoSocket, ITriggerService triggerService, MME mme)
    {
        //process each open/full round of the game (should only ever be at most 1 at a time for each game)
        List<Round> rounds =  _shoutContestService.getRoundsForGameForStatus(game.getId(), Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL);
        if (rounds.size() > 0) {
            //get a list of all the bots in this game
            List<Long> botSubscriberIds;
            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                botSubscriberIds = _botEngine.getBotsForGame(game.getId());

                _transactionManager.commit(txStatus);
                txStatus = null;
            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

            rounds.stream().forEach(round -> {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("processing round: " + round.getRoundNames().get("en") + ", status: " + round.getRoundStatus());
                }
                processRound(game, round, botSubscriberIds, runMethod, filePrefix, socketIoSocket, triggerService, mme);
            });
        }
    }

    private void processRound(
        Game game, Round round, List<Long> botSubscriberIds, Consumer<Void> runMethod, String filePrefix, Socket socketIoSocket, ITriggerService triggerService, MME mme)
    {
//String roundName = round.getRoundNames().get("en");
        String lockName = "ROUND_" + round.getId().hashCode();
        List<List<MatchQueue>> allMatchQueues;

//_logger.info(MessageFormat.format("processing round. ROUND {0}, type: {1}, roundId: {2}", roundName, round.getRoundType(), round.getId()));
        switch (round.getRoundType())
        {
            case POOL: {
                if (!DistributedVisor.tryClusterLock(lockName)) {
                    new Thread() {@Override public void run() {
                        try { Thread.sleep(WAIT_LOCK_RETRY); } catch (InterruptedException ignored) { }
                        runMethod.accept(null);
                    }}.start();
                    return;
                }
                try {
//_logger.info(">>> PROCESSROUND, " + lockName + " acquired by: MQE");
                    allMatchQueues = _shoutContestService.getMatchQueues(
                        game.getId(), round.getId(), round.getMatchPlayerCount(), IShoutContestService.MATCH_QUEUE_ALGORITHM.RANDOM);
_logger.info(MessageFormat.format("MQE, # of match queues: {0}, gameId: {1}, roundId: {2}", allMatchQueues.size(), game.getId(), round.getId()));
                } finally {
                    //unlock round
                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                    TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                    try {
                        DistributedVisor.releaseClusterLock(lockName);

                        _transactionManager.commit(txStatus);
                        txStatus = null;

                    } finally {
                        if (txStatus != null) {
                            _transactionManager.rollback(txStatus);
                            txStatus = null;
                        }
                    }

//_logger.info(">>> PROCESSROUND, " + lockName + " released by: MQE");
                }

                if (allMatchQueues.size() == 0) {
                    processNonMatches(game, round, botSubscriberIds, runMethod, filePrefix, socketIoSocket, triggerService, mme);
                } else {
                    //there is at least one pairing to be made. go ahead and process it/them
                    allMatchQueues.stream().forEach(mqs -> {
_logger.info(MessageFormat.format(
    "MQE: processPairing, roundType: {0}, subscriberIds: {1}",
    round.getRoundType(), mqs.stream().map(mq -> mq.getSubscriberId()+"").collect(Collectors.joining(",")))
);
                        processPairing(game, round, mqs, botSubscriberIds, filePrefix, socketIoSocket, triggerService, mme, null);
                    });

                    //just to be safe that nobody was left out, process non matches as well (might be none)
                    processNonMatches(game, round, botSubscriberIds, runMethod, filePrefix, socketIoSocket, triggerService, mme);
                }
            }
            break;

            case BRACKET: {
                if (!DistributedVisor.tryClusterLock(lockName)) {
                    new Thread() {@Override public void run() {
                        try { Thread.sleep(WAIT_LOCK_RETRY); } catch (InterruptedException ignored) { }
                        runMethod.accept(null);
                    }}.start();
                    return;
                }
                try {
//_logger.info(">>> PROCESSROUND, " + lockName + " acquired by: MQE");
                    allMatchQueues = _shoutContestService.getMatchQueues(
                        game.getId(), round.getId(), round.getMatchPlayerCount(), IShoutContestService.MATCH_QUEUE_ALGORITHM.BY_OPPOSITE_SKILL);
                } finally {
                    //unlock round
                    DistributedVisor.releaseClusterLock(lockName);
//_logger.info(">>> PROCESSROUND, " + lockName + " released by: MQE");
                }

                //see if there is a twitch subscriber being followed
                Long twitchSubscriberId = null;

                GameStats gameStats = null;
                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                try {
                    gameStats = _dao.getGameStats(game.getId());

                    _transactionManager.commit(txStatus);
                    txStatus = null;

                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                    }
                }

                if (gameStats != null) {
                    twitchSubscriberId = gameStats.getTwitchConsoleFollowedSubscriberId();
                }

                //process each pairing
_logger.info(MessageFormat.format("MQE: BEGIN processPairing, roundType: BRACKET, # of pairings: {0}", allMatchQueues.size()));
                for (List<MatchQueue> mqs : allMatchQueues) {
                    processPairing(game, round, mqs, botSubscriberIds, filePrefix, socketIoSocket, triggerService, mme, twitchSubscriberId);
                }
_logger.info(MessageFormat.format("MQE: END processPairing, roundType: BRACKET, # of pairings: {0}", allMatchQueues.size()));

                //it's possible there could be an odd-man out issue. if so, a bot must be spun up
                List<MatchQueue> matchQueues = _shoutContestService.getOpenMatchQueues(game.getId(), round.getId(), round.getMatchPlayerCount());
                if (matchQueues.size() > 0) {
                //if (game.isAllowBots()) {
                    //spin up bots
                    int numBotsNeeded = round.getMatchPlayerCount() - 1;
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(MessageFormat.format("odd man out issue in bracket play. adding {0} bot[s]", numBotsNeeded));
                    }

                    //add the bot(s) - create a db transaction since the bot engine doesn't have one
                    txStatus = _transactionManager.getTransaction(txDef);
                    try {
                        _logger.info(MessageFormat.format("adding {0} bot[s] to bracket round: {1}, {2}", numBotsNeeded, round.getId(), round.getRoundNames().get("en")));
                        botSubscriberIds.addAll(_botEngine.addBotPlayers(game.getId(), round.getId(), numBotsNeeded));

                        _transactionManager.commit(txStatus);
                        txStatus = null;
                    } finally {
                        if (txStatus != null) {
                            _transactionManager.rollback(txStatus);
                            txStatus = null;
                        }
                    }

                    //try to pair again now that the correct number of bots are available
                    processRound(game, round, botSubscriberIds, runMethod, filePrefix, socketIoSocket, triggerService, mme);

                    return;

                //} else {
                //
                }

                //change round status to INPLAY
_logger.debug("MQE: changing BRACKET round status to INPLAY");
                round.setRoundStatus(Round.ROUND_STATUS.INPLAY);
                _shoutContestService.updateRoundStatus(round.getId(), round.isFinalRound(), Round.ROUND_STATUS.INPLAY);

                txStatus = _transactionManager.getTransaction(txDef);
                try {
                    publishGameWithExtrasToWds(game.getId(), _dao, _shoutContestService);

                    _transactionManager.commit(txStatus);
                    txStatus = null;
                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                    }
                }

                //start the MME
_logger.debug("MQE: calling MME run with roundId: " + round.getId());
                mme.run(round.getId(), round.getGameId(), true);
_logger.debug("MQE: done calling MME run with roundId: " + round.getId());
            }
            break;

            default:
                throw new IllegalStateException("unexpected round type, unable to pair: " + round.getRoundType());
        }
    }

    private void processPairing(
        Game game, Round round, List<MatchQueue> matchQueues, List<Long> botSubscriberIds, String filePrefix, Socket socketIoSocket, ITriggerService triggerService, MME mme,
        Long twitchSubscriberId)
    {
//_logger.info("1");
    Match match;
try {
        //make a match and add all the players to it
        match = new Match(game.getId(), round.getId(), ISnowyowlService.GAME_ENGINE, game.getEngineType(), round.getMinimumActivityToWinCount(), round.getMaximumActivityCount());
        _shoutContestService.addMatch(match);
} catch (Throwable t) {
    _logger.error("uncaught exception", t);
    throw t;
}
//_logger.info("2");

        boolean doesMatchContainTwitchSubscriber = false;

        List<MatchPlayer> players = new ArrayList<MatchPlayer>(matchQueues.size());
        for (MatchQueue mq : matchQueues) {
            players.add(_shoutContestService.addMatchPlayer(game.getId(), round.getId(), match.getId(), mq.getRoundPlayerId(), mq.getSubscriberId()));

            if (twitchSubscriberId != null && twitchSubscriberId == mq.getSubscriberId()) {
                doesMatchContainTwitchSubscriber = true;
            }

            //also remove from the waiting map so if they come in on another round they don't get immediately paired
            getSubscriberMatchQueueWaitingMap().remove(mq.getSubscriberId());
        }
//_logger.info("3");
        saveState(filePrefix);
//_logger.info("4");

        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            StringBuilder buf = new StringBuilder();
            buf.append("MQE: created match for: ");
            buf.append(players.stream().map(p -> p.getSubscriberId() + "").collect(Collectors.joining(",")));
            SnowyowlService.SUB_EVENT_LOGGER.debug(buf.toString());
        }
//_logger.info("5");

        for (MatchPlayer mp : players) {
            if (!botSubscriberIds.contains(mp.getSubscriberId())) {
                //send a "user_matched" sync message for each player that was just matched
                Subscriber s = _identityService.getSubscriberById(mp.getSubscriberId());
                enqueueSyncMessage(
                        _jsonMapper, _syncService, _logger,
                        game.getId(), ISnowyowlService.SYNC_MESSAGE_USER_MATCHED,
                        new FastMap<>("players", players), s, socketIoSocket, triggerService);
            }
        }

        //fire off a twitch socket.io message
        if (doesMatchContainTwitchSubscriber) {
            long opId = 0;
            for (MatchPlayer mp : players) {
                if (mp.getSubscriberId() != twitchSubscriberId) {
                    opId = mp.getSubscriberId();
                    break;
                }
            }

            Map<String, Object> twitchMap = new FastMap<>(
                "type", "TWITCH_PAIRED",
                "gameid", game.getId(),
                "roundId", round.getId(),
                "matchId", match.getId(),
                "subscriberId", twitchSubscriberId,
                "opponentId", opId
            );

            if (socketIoSocket != null) {
                try {
                    socketIoSocket.emit("send_twitch_message", _jsonMapper.writeValueAsString(twitchMap));
                } catch (JsonProcessingException e) {
                    _logger.error("unable to emit send_twitch_message", e);
                }
            }
        }

//_logger.info("6");

        //if this is a POOL round, kick off the MME
        if (round.getRoundType() == Round.ROUND_TYPE.POOL) {
_logger.info("MQE: starting the MME with this matchId: " + match.getId());
            mme.run(match.getId(), match.getGameId(), false);
        }
//_logger.info("7");
    }

    private void processNonMatches(
        Game game, Round round, List<Long> botSubscriberIds, Consumer<Void> runMethod, String filePrefix, Socket socketIoSocket, ITriggerService triggerService, MME mme)
    {
        //get anyone not paired. expected to be 0 or 1
        List<MatchQueue> matchQueues = _shoutContestService.getOpenMatchQueues(game.getId(), round.getId(), round.getMatchPlayerCount());
        if (matchQueues.size() == 0) {
//_logger.info(MessageFormat.format("the queue is empty for game: {0}, round: {1}", game.getId(), round.getId()));
            return;
        }

        if (matchQueues.size() > 1) {
            throw new IllegalStateException("unexpected # of open matches when processing non matches. expected 1, got: " + matchQueues.size());
        }

//_logger.info(MessageFormat.format("", ));
        matchQueues.stream().forEach(mq -> {
//_logger.info(MessageFormat.format("processing MatchQueue, sId: {0,number,#}", mq.getSubscriberId()));
            //has the subscriber for this match queue been waiting for too long?
            Long waitingSince = getSubscriberMatchQueueWaitingMap().get(mq.getSubscriberId());
            if (waitingSince == null) {
                if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                    SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                        "MQE: sub {0,number,#} has not been waiting at all. adding to the map and going to sleep for: {0} ms",
                        mq.getSubscriberId(), WAIT_TIME_FOR_PAIRING_TIMEOUT_CHECK));
                }

                //hasn't been waiting too long since they just started
                getSubscriberMatchQueueWaitingMap().put(mq.getSubscriberId(), new Date().getTime());
                saveState(filePrefix);

                //go to sleep and process it again
                new Thread() {
                    @Override
                    public void run()
                    {
                        try {
                            Thread.sleep(WAIT_TIME_FOR_PAIRING_TIMEOUT_CHECK);
                        } catch (InterruptedException ignored) {
                            _logger.error("subsriber initial pairing wait thread was interrupted");
                        }

                        runMethod.accept(null);
                    }
                }.start();

            } else {
                //how long to wait depends on the game flag: pairImmediately
                long timeoutMs = game.isPairImmediately() ? WAIT_TIME_TIMEOUT_IMMEDIATE : WAIT_TIME_TIMEOUT_DEFAULT;

                if (waitingSince + timeoutMs < new Date().getTime()) {
                    //remove from the waiting queue
                    getSubscriberMatchQueueWaitingMap().remove(mq.getSubscriberId());
                    saveState(filePrefix);

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                            "MQE: subscriber {0,number,#} has been waiting long enough",
                            mq.getSubscriberId()));
                    }

                    //subscriber has been waiting long enough
                    if (game.isAllowBots()) {
                        //spin up bots
                        int numBotsNeeded = round.getMatchPlayerCount() - 1;
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("player not matched in allotted time. adding {0} bots", numBotsNeeded));
                        }

                        //add the bot(s) - create a db transaction since the bot engine doesn't have one
                        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                        try {
_logger.info(MessageFormat.format("MQE: adding {0} bot[s] to round: {1}, {2}", numBotsNeeded, round.getId(), round.getRoundNames().get("en")));
                            botSubscriberIds.addAll(_botEngine.addBotPlayers(game.getId(), round.getId(), numBotsNeeded));

                            _transactionManager.commit(txStatus);
                            txStatus = null;
                        } finally {
                            if (txStatus != null) {
                                _transactionManager.rollback(txStatus);
                                txStatus = null;
                            }
                        }

                        //try to pair again now that the correct number of bots are available
                        processRound(game, round, botSubscriberIds, runMethod, filePrefix, socketIoSocket, triggerService, mme);

                    } else {
                        //oh well, give up and tell them they'll get a notification if/when they are paired
                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MQE: subscriber {0,number,#} has been waiting long enough, but game doesn't allow bots",
                                mq.getSubscriberId()));
                        }

                        Subscriber s = _identityService.getSubscriberById(mq.getSubscriberId());

                        enqueueSyncMessage(
                                _jsonMapper, _syncService, _logger,
                                game.getId(), ISnowyowlService.SYNC_MESSAGE_WILL_NOTIFY_ON_PAIRED, null, s, socketIoSocket, triggerService);
                    }

                } else {
                    //not waiting too long yet. go to sleep and process it again
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                            "MQE: subscriber {0,number,#} has NOT been waiting long enough. going to sleep for: {1} ms",
                            mq.getSubscriberId(), WAIT_TIME_FOR_PAIRING_TIMEOUT_CHECK));
                    }

                    new Thread() {
                        @Override
                        public void run()
                        {
                            try {
                                Thread.sleep(WAIT_TIME_FOR_PAIRING_TIMEOUT_CHECK);
                            } catch (InterruptedException ignored) {
                                _logger.error("subsriber subsequent pairing wait thread was interrupted");
                            }

                            runMethod.accept(null);
                        }
                    }.start();
                }
            }
        });
    }

    public void saveState(String prefix)
    {
        try {
            File mapFile = new File(_stateDir, prefix + "_subscriberMatchQueueWaitingMap.dat");
            writeToFile(mapFile, _subscriberMatchQueueWaitingMap);
        } catch (IOException e) {
            _logger.error("unable to save state", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadState(String prefix, Consumer<Void> runMethod)
    {
        try {
            File mapFile = new File(_stateDir, prefix + "_subscriberMatchQueueWaitingMap.dat");
            if (!mapFile.exists()) return;

            _subscriberMatchQueueWaitingMap = (Map<Long, Long>) readFromFile(mapFile);
            if (_subscriberMatchQueueWaitingMap.size() > 0) {
                runMethod.accept(null);
            }

        } catch (IOException | ClassNotFoundException e) {
            _logger.error("unable to load state", e);
        }
    }

}
