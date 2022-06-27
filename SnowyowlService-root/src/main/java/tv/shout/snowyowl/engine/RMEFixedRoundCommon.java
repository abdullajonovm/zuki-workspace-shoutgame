package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreService;
import com.meinc.webdatastore.service.WebDataStoreException;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.dao.DistributedVisorV2;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutTableRow;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public abstract class RMEFixedRoundCommon
extends BaseEngine
implements RME
{
    private static Logger _logger = Logger.getLogger(RMEFixedRoundCommon.class);

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private CurrentRankCalculator _currentRankCalculator;

    @Autowired
    private CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    private SubscriberStatsHandler _subscriberStatsHandler;

    @Resource(name="webDataStoreService")
    private IWebDataStoreService _wdsService;

    public abstract MQE getMQE();
    public abstract PayoutManager getPayoutManager();

    //threading
    //this engine is built around only 1 run at a time (not for concurrent runs)
    private ArrayBlockingQueue<Integer> _workQueue = new ArrayBlockingQueue<>(1);
    private RMERunner _runner;

    //must be called when service starts
    @Override
    public void start(Socket socketIoSocket)
    {
        if (_runner == null) {
            _runner = new RMERunner();
            _runner.setDaemon(true);
            _runner.start();
        }

        _socketIoSocket = socketIoSocket;

        //for good measure, always run once when starting, in case it got interrupted mid-processing during a server restart or crash
        run();
    }

    //must be called when service stops
    @Override
    public void stop()
    {
        if (_runner != null) {
            _runner.interrupt();
        }
    }

    //tell the MQE to run
    public void run()
    {
        try {
            //if something is already processing, "put" will block, but the engine should complete fairly quickly
            _workQueue.put(1); //value doesn't matter. just that something goes into the queue so the thread will run
        } catch (InterruptedException e) {
        }
    }

    private class RMERunner
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    _workQueue.take(); //block until input is available
                    process();
                }
            } catch (InterruptedException ignored) {}
        }
    }

    private void process()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("RME running...");
        }

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
        try {
            List<Game> games = _shoutContestService.getGamesByStatusAndEngine(ISnowyowlService.GAME_ENGINE, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY);
            for (Game game : games) {
//_logger.debug("RME processing game: " + game.getGameNames().get("en"));
                List<Round> rounds = _shoutContestService.getRoundsForGameForStatus(game.getId(), Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.INPLAY, Round.ROUND_STATUS.FULL);

                try {
                    for (Round round : rounds) {
//_logger.debug("RME processing round: " + round.getRoundNames().get("en") + ", type: " + round.getRoundType());

                        String lockName = "ROUND_" + round.getId().hashCode();
                        if (DistributedVisorV2.doesClusterLockExist(lockName)) {
                            //throw new IllegalStateException("RME attempting to process a locked round: " + lockName);
                            _logger.warn("RME attempting to process a locked round: " + lockName);
                            break;
                        }

                        if (round.getRoundType() == Round.ROUND_TYPE.POOL) {
                            processPoolRound(game, round/*, botSubscriberIds*/);
                        } else if (round.getRoundType() == Round.ROUND_TYPE.BRACKET) {
                            processBracketRound(game, round);
                        }
                    }
                } catch (Exception e) {
                    //just because one game craps out doesn't mean the others should stop processing
                    _logger.error("unable to process rounds for game. skipping.", e);
                }
            }

            _transactionManager.commit(txStatus);
            txStatus = null;

        } finally {
            if (txStatus != null) {
                _transactionManager.rollback(txStatus);
                txStatus = null;
            }
        }
    }

    private void processPoolRound(Game game, Round round/*, List<Integer> botSubscriberIds*/)
    {
        List<Match> matches = _shoutContestService.getMatchesByRoundAndStatus(round.getId(), Match.MATCH_STATUS.PROCESSING);
        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format("RME there are {0} matches in the PROCESSING state for round: {1}", matches.size(), round.getId()));
        }

        /*
         * here's a fun transaction race condition:
         * in transaction #1, the MatchManagementEngine changes all of the Matches in a round from OPEN to PROCESSING
         * immediately after, the RoundManagementEngine runs in transaction #2 and asks for all of the PROCESSING matches
         * but transaction #1 hasn't committed yet, and thus transaction #2 sees no rows and this logic gets skipped.
         * but in being skipped, it still marks the rounds as CLOSED, and thus the matches for that round are never processed.
         * SO, by returning if there are no matches, it will come back next time, and transaction #1 will be closed and all will be well.
         * BUT there is the problem of what if a round doesn't have any matches - it will never automatically close, thus the
         * extra check to make sure that a round actually has matches before the return statement.
         */
        if (matches.size() == 0) {
            int matchCountForRound = _shoutContestService.getMatchCountForRound(round.getId());
            if (matchCountForRound > 0) {
                return;
            }
        }

        List<Long> botSubscriberIds = _botEngine.getBotsForGame(game.getId());
        Set<Long> botSubscriberIdsSet = new HashSet<>(botSubscriberIds); //quicker lookup

//        Map<Integer, RoundPlayer> subscriberIdToRoundPlayerMap = new HashMap<>();
        for (Match match : matches) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("processing match: " + match.getId());
            }


            List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());
            for (MatchPlayer matchPlayer : matchPlayers) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("processing matchPlayer: " + matchPlayer.getId());
                }

//if this is put back, the GamePlayer record will also need to be expunged (or else other code changed to re-use an old GamePlayer is the player is a bot)
//                //if this is a bot, no need to do anything else. free it up so it can be used again
//                if (botSubscriberIds.contains(matchPlayer.getSubscriberId())) {
//                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//                    TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
//                    try {
//                        _botEngine.releaseBot(matchPlayer.getSubscriberId());
//
//                        _transactionManager.commit(txStatus);
//                        txStatus = null;
//
//                    } finally {
//                        if (txStatus != null) {
//                            _transactionManager.rollback(txStatus);
//                            txStatus = null;
//                        }
//                    }
//                    continue;
//                }

                RoundPlayer roundPlayer = _shoutContestService.getRoundPlayer(matchPlayer.getRoundPlayerId());
                GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), roundPlayer.getSubscriberId());
//                subscriberIdToRoundPlayerMap.put(roundPlayer.getSubscriberId(), roundPlayer);

                if (game.isProductionGame() && !botSubscriberIdsSet.contains(gamePlayer.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(gamePlayer.getSubscriberId(), SubscriberStats.STATS_TYPE.POOL_ROUNDS_PLAYED, 1);
                }

                switch (matchPlayer.getDetermination())
                {
                    case WON: {
                        roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.WON);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, POOL roundId: {1}, round determination: WON",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId()));
                        }

                        //there is always another round to play (pool play is always followed by bracket play)
                        int currentRoundSequence = round.getRoundSequence();
                        Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                        gamePlayer.setLastRoundId(round.getId());
                        gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                    }
                    break;

                    case LOST: {
                        roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.LOST);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, POOL roundId: {1}, round determination: LOST",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId()));
                        }

                        //there is always another round to play (pool play is always followed by bracket play)
                        int currentRoundSequence = round.getRoundSequence();
                        Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                        gamePlayer.setLastRoundId(round.getId());
                        gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                    }
                    break;

                    default:
                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, POOL roundId: {1}, unexpected match determination: {2}, THUS NO ROUND DETERMINATION!!!",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId(), matchPlayer.getDetermination()));
                        }
                }

                //update the player records
                _shoutContestService.updateRoundPlayer(roundPlayer);
                _shoutContestService.updateGamePlayer(gamePlayer);
                _currentRankCalculator.clear(game.getId());

                //if a bot, remove from the round and don't send a match_result
                if (botSubscriberIds.contains(gamePlayer.getSubscriberId())) {
                    //remove from the round so it doesn't take up space for a legit player that may want to join
                    _commonBusinessLogic.removeFromRound(game.getId(), round, roundPlayer);
//this is now sent as soon as the match is over
//                } else {
//                    //send out "match_result" sync message
//                    Subscriber s = _identityService.getSubscriberById(gamePlayer.getSubscriberId());
//                    enqueueSyncMessage(
//                            _jsonMapper, _syncService, _logger,
//                            roundPlayer.getGameId(), ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT,
//                            new FastMap<> ("roundPlayer", roundPlayer, "gamePlayer", gamePlayer), s, _socketIoSocket, _triggerService);
                }
            } //matchPlayer

            //close the match
            match.setMatchStatus(Match.MATCH_STATUS.CLOSED);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);

        } //match
    }

    private void processBracketRound(Game game, Round round)
    {
        List<Match> matches = _shoutContestService.getMatchesByRoundAndStatus(round.getId(), Match.MATCH_STATUS.PROCESSING);
        if (_logger.isDebugEnabled()) {
            _logger.debug(MessageFormat.format("RME there are {0} matches in the PROCESSING state for BRACKET round: {1}", matches.size(), round.getId()));
        }

        /*
         * here's a fun transaction race condition:
         * in transaction #1, the MatchManagementEngine changes all of the Matches in a round from OPEN to PROCESSING
         * immediately after, the RoundManagementEngine runs in transaction #2 and asks for all of the PROCESSING matches
         * but transaction #1 hasn't committed yet, and thus transaction #2 sees no rows and this logic gets skipped.
         * but in being skipped, it still marks the rounds as CLOSED, and thus the matches for that round are never processed.
         * SO, by returning if there are no matches, it will come back next time, and transaction #1 will be closed and all will be well.
         * BUT there is the problem of what if a round doesn't have any matches - it will never automatically close, thus the
         * extra check to make sure that a round actually has matches before the return statement.
         */
        if (matches.size() == 0) {
            int matchCountForRound = _shoutContestService.getMatchCountForRound(round.getId());
            if (matchCountForRound > 0) {
                return;
            }
        }

        List<Long> botSubscriberIds = _botEngine.getBotsForGame(game.getId());
        Set<Long> botSubscriberIdsSet = new HashSet<>(botSubscriberIds); //quicker lookup
        Set<Long> sponsorPlayerIdsForGame = new HashSet<>(_sponsorEngine.getSponsorsForGame(game.getId()));

        //grab all the data up front so it doesn't have to get re-retrieved over and over in the loops below
        Map<String, List<MatchPlayer>> matchIdToMatchPlayersMap = new HashMap<>();
        Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap = new HashMap<>();
        Map<Long, GamePlayer> subscriberIdToGamePlayerMap = new HashMap<>();
        int numPlayersWithLivesLeft = 0;

        for (Match match : matches) {
            List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());
            matchIdToMatchPlayersMap.put(match.getId(), matchPlayers);

            for (MatchPlayer matchPlayer : matchPlayers) {
                RoundPlayer roundPlayer = _shoutContestService.getRoundPlayer(matchPlayer.getRoundPlayerId());
                GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), roundPlayer.getSubscriberId());
                subscriberIdToRoundPlayerMap.put(roundPlayer.getSubscriberId(), roundPlayer);
                subscriberIdToGamePlayerMap.put(roundPlayer.getSubscriberId(), gamePlayer);

                if (gamePlayer.getCountdownToElimination() != null && matchPlayer.getDetermination() == MatchPlayer.MATCH_PLAYER_DETERMINATION.LOST) {
                    gamePlayer.setCountdownToElimination(gamePlayer.getCountdownToElimination() - 1);
                    _shoutContestService.updateGamePlayer(gamePlayer);
                }

//nope, consider everyone, even bots
//                //only consider actual players (not bots) that have a life left
                if (/*!botSubscriberIdsSet.contains(gamePlayer.getSubscriberId()) &&*/ gamePlayer.getCountdownToElimination() != null && gamePlayer.getCountdownToElimination() > 0) {
                    numPlayersWithLivesLeft++;
                }
            }
        }
        boolean lastRound = numPlayersWithLivesLeft <= 1;
//_logger.info(">>> numPlayersWithLivesLeft: " + numPlayersWithLivesLeft);
//_logger.info(">>> lastRound? " + lastRound);

        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                    "RME: BRACKET round: {0}, # of players with lives left: {1}",
                    round.getId(), numPlayersWithLivesLeft));
        }

        Round nextRoundInSequence = null;
        if (!lastRound) {
            //grab the next round (it will have already been created)
            int nextRoundSequence = round.getRoundSequence()+1;
            nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), nextRoundSequence);
        }

        for (Match match : matches) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("processing match: " + match.getId());
            }

            List<MatchPlayer> matchPlayers = matchIdToMatchPlayersMap.get(match.getId());
            for (MatchPlayer matchPlayer : matchPlayers) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("processing matchPlayer: " + matchPlayer.getId());
                }

                RoundPlayer roundPlayer = subscriberIdToRoundPlayerMap.get(matchPlayer.getSubscriberId());
                GamePlayer gamePlayer =  subscriberIdToGamePlayerMap.get(matchPlayer.getSubscriberId());

                if (game.isProductionGame() && !botSubscriberIdsSet.contains(gamePlayer.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(gamePlayer.getSubscriberId(), SubscriberStats.STATS_TYPE.BRACKET_ROUNDS_PLAYED, 1);
                }

                switch (matchPlayer.getDetermination())
                {
                    case WON: {
                        roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.WON);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, BRACKET roundId: {1}, round determination: WON",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId()));
                        }

                        if (lastRound) {
                            //gamePlayer.setPayoutAwardedAmount(round.getRoundPurse()); //this is incorrect; must wait until the payout calculator runs
                            //gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                            gamePlayer.setLastRoundId(round.getId()); //here, last should say previous
                            gamePlayer.setNextRoundId(null);

                        } else {
                            //there is another round to play
                            //int currentRoundSequence = round.getRoundSequence();
                            //Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                            gamePlayer.setLastRoundId(round.getId());
                            gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                        }
                    }
                    break;

                    case SAVED: {
                        roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, BRACKET roundId: {1}, round determination: SAVED",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId()));
                        }

                        if (lastRound) {
                            //gamePlayer.setPayoutAwardedAmount(round.getRoundPurse()); //this is incorrect; must wait until the payout calculator runs
                            //gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                            gamePlayer.setLastRoundId(round.getId()); //here, last should say previous
                            gamePlayer.setNextRoundId(null);

                        } else {
                            //there is another round to play
                            //int currentRoundSequence = round.getRoundSequence();
                            //Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                            gamePlayer.setLastRoundId(round.getId());
                            gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                        }
                    }
                    break;

                    case LOST: {
                        roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.LOST);

                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sId: {0,number,#}, BRACKET roundId: {1}, round determination: LOST",
                                    roundPlayer.getSubscriberId(), roundPlayer.getRoundId()));
                        }

                        if (lastRound) {
                            //gamePlayer.setPayoutAwardedAmount(0D); //this is incorrect; must wait until the payout calculator runs
                            //gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                            gamePlayer.setLastRoundId(round.getId());
                            gamePlayer.setNextRoundId(null);

                        } else {
                            //they don't necessarily go the next round; they may have been eliminated!!!
                            //int currentRoundSequence = round.getRoundSequence();
                            //Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                            gamePlayer.setLastRoundId(round.getId());
                            if (gamePlayer.getCountdownToElimination() == null || gamePlayer.getCountdownToElimination() > 0) {
                                gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                            } else {
                                //gamePlayer.setPayoutAwardedAmount(0D); //this is incorrect; must wait until the payout calculator runs
                                //gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                                gamePlayer.setNextRoundId(null);
                            }
                        }
                    }
                    break;

                    default:
                        //ignore
                }

                //update the player records
                _shoutContestService.updateRoundPlayer(roundPlayer);
                _shoutContestService.updateGamePlayer(gamePlayer);
                _currentRankCalculator.clear(game.getId());

//this is now sent as soon as the match is over
//                //send out "match_result" sync message
//                Subscriber s = _identityService.getSubscriberById(gamePlayer.getSubscriberId());
//                enqueueSyncMessage(
//                        _jsonMapper, _syncService, _logger,
//                        roundPlayer.getGameId(), ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT,
//                        new FastMap<> ("roundPlayer", roundPlayer, "gamePlayer", gamePlayer), s, _socketIoSocket, _triggerService);
            } //matchPlayer

            //close the match
            match.setMatchStatus(Match.MATCH_STATUS.CLOSED);
            match.setMatchStatusSetAt(new Date());
            _shoutContestService.updateMatch(match);

        } //match

        List<RoundPlayer> roundPlayersArray = new ArrayList<>(subscriberIdToRoundPlayerMap.values());
        roundPlayersArray.sort((rp1, rp2) -> rp2.getSkill().compareTo(rp1.getSkill()));
        for (int i = 0; i < roundPlayersArray.size(); i++) {
            RoundPlayer roundPlayer = roundPlayersArray.get(i);
            roundPlayer.setRank(i+1);
            _shoutContestService.updateRoundPlayer(roundPlayer);
        }

        //close the round
//_logger.info("RME closing round: " + round.getId() + ", " + round.getRoundNames().get("en"));
        round.setRoundStatus(Round.ROUND_STATUS.CLOSED);
        _shoutContestService.updateRoundStatus(round.getId(), lastRound, Round.ROUND_STATUS.CLOSED);

        //publish the subscriber stats documents
        if (game.isProductionGame()) {
            List<Long> playerIdsStillInGame = roundPlayersArray.stream()
                .filter(rp -> rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.WON || rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED)
                .filter(rp -> !botSubscriberIdsSet.contains(rp.getSubscriberId()) )
                .map(rp -> rp.getSubscriberId())
                .collect(Collectors.toList());
            _subscriberStatsHandler.publishSubscriberStatsDocuments(game.getId(), playerIdsStillInGame);
        }

        //if not the final round, move everyone en-masse to the next round and start it up
        if (!lastRound) {
            //get everyone who played this round
            List<Long> subscriberIds = _shoutContestService.getSubscriberIdsForRound(round.getId());

//            //score everyone and assign a ranking as if the game were over so that people can see where they currently are in the payout table
//            Map<Integer, List<RoundPlayer>> subscriberIdToRoundPlayers = new HashMap<>();
//            Map<Integer, Double> subscriberIdToScore = new HashMap<>();
//            List<Integer> rank = new ArrayList<>(); //so the lambda expression can access a changing variable without it having to be a member variable

            //grab the payout table rows
            List<PayoutTableRow> payoutTableRows = _dao.getPayoutTableRows(game.getId());

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
                if (botSubscriberIdsSet.contains(subscriberId)) {
                    continue;
                }

                String payoutTableRowUuid = null;

                RoundPlayer roundPlayer = subscriberIdToRoundPlayerMap.get(subscriberId);

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

                Subscriber subscriber = _identityService.getSubscriberById(subscriberId);

                //send as a socket.io message
                if (_socketIoSocket != null) {
                    Map<String, Object> curRankMap = new HashMap<>();
                    curRankMap.put("currentRank", payoutTableRowUuid);

                    Map<String, Object> msg = new HashMap<>();
                    msg.put("recipient", subscriber.getEmailSha256Hash());
                    msg.put("message", curRankMap);

                    ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
                    String message;
                    try {
                        message = _jsonMapper.writeValueAsString(msg);
                        SocketIoLogger.log(_triggerService, subscriberId, "send_current_rank", message, "SENDING");
                        _socketIoSocket.emit("send_current_rank", message);
                        SocketIoLogger.log(_triggerService, subscriberId, "send_current_rank", message, "SENT");
                    } catch (JsonProcessingException e) {
                        _logger.warn("unable to emit socket.io message", e);
                    }
                }

                //publish a wds doc
                WebDataStoreObject object = new WebDataStoreObject();
                object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
                //object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
                object.setPath("/" + subscriber.getEmailSha256Hash() + "/" + game.getId() + "_rank.json");
                Map<String, Object> data = new FastMap<>("currentRank", payoutTableRowUuid);
                try {
                    object.setData(_jsonMapper.writeValueAsBytes(data));

                    try {
                        _wdsService.createOrUpdateObjectSync(object, 0);
                    } catch (WebDataStoreException | InterruptedException e) {
                        _logger.error("unable to publish currentRank.json for player: " + subscriber.getSubscriberId(), e);
                    }

                } catch (JsonProcessingException e) {
                    _logger.error("unable to publish currentRank.json for player: " + subscriber.getSubscriberId(), e);
                }
            }

            int nonFilteredSubscriberCount = 0;
            for (long subscriberId : subscriberIds) {
                //filter out anyone who has been eliminated
                GamePlayer gamePlayer = subscriberIdToGamePlayerMap.get(subscriberId);
                if (gamePlayer == null) {
                    gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), subscriberId);
                    if (gamePlayer == null) {
                        throw new IllegalStateException(
                                MessageFormat.format("unable to locate GamePlayer for sId: {0,number,#}, gId: {1}", subscriberId, round.getGameId())
                        );
                    } else {
                        subscriberIdToGamePlayerMap.put(subscriberId, gamePlayer);
                    }
                }

                Subscriber s = _identityService.getSubscriberById(subscriberId);

//don't do this. let them play through -shawker 24apr 2018
//                //filter out any bots - they don't get to move forward
//                if (botSubscriberIdsSet.contains(gamePlayer.getSubscriberId())) {
//                    continue;
//                }

                //any players with no lives left are eliminated
                if (gamePlayer.getCountdownToElimination() == null || gamePlayer.getCountdownToElimination() <= 0) {
                    //send an eliminated message
                    enqueueSyncMessage(
                            _jsonMapper, _syncService, _logger,
                            game.getId(), ISnowyowlService.SYNC_MESSAGE_ELIMINATED, null, s, _socketIoSocket, _triggerService);
                    continue;
                }

                nonFilteredSubscriberCount++;

                //add the RoundPlayer record (move the rank forward from the previous round)
                RoundPlayer roundPlayer = new RoundPlayer(game.getId(), nextRoundInSequence.getId(), subscriberId);
                RoundPlayer oldRoundPlayer = subscriberIdToRoundPlayerMap.get(subscriberId);

                //every once in a while, oldRoundPlayer is null. not sure why this is or how it could be, but just deal with it
                if (oldRoundPlayer != null) {
                    roundPlayer.setRank(oldRoundPlayer.getRank());
                    roundPlayer.setSkillAnswerCorrectPct(oldRoundPlayer.getSkillAnswerCorrectPct());
                    roundPlayer.setSkillAverageAnswerMs(oldRoundPlayer.getSkillAverageAnswerMs());
                    roundPlayer.setSkill(oldRoundPlayer.getSkill());
                } else {
                    _logger.error(MessageFormat.format(
                            "oldRoundPlayer not found. unable to set previous rank! game: {0}, round: {1}, subscriber: {2}, ",
                            game.getId(), round.getId(), subscriberId));
                }

                _shoutContestService.addRoundPlayer(roundPlayer, nextRoundInSequence.getRoundType());
                _currentRankCalculator.clear(game.getId());

                //add to the match queue (make them eligible for pairing)
                _shoutContestService.enqueueMatchQueue(game.getId(), nextRoundInSequence.getId(), roundPlayer.getId(), subscriberId);

                //send the "joined_round" sync message
                enqueueSyncMessage(
                        _jsonMapper, _syncService, _logger,
                        game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND,
                        new FastMap<String, Object>(
                            "roundPlayer", roundPlayer,
                            "currentPlayerCount", nonFilteredSubscriberCount,
                            "roundType", "BRACKET"
                        ), s, _socketIoSocket, _triggerService);
            }

            //update the next round to have the same subscribers and set the status to READY so pairing can begin
            nextRoundInSequence.setCurrentPlayerCount(nonFilteredSubscriberCount);
            nextRoundInSequence.setRoundStatus(Round.ROUND_STATUS.OPEN);
            _shoutContestService.updateRoundStatusAndPlayerCount(nextRoundInSequence.getId(), nextRoundInSequence.getRoundStatus(), nextRoundInSequence.getCurrentPlayerCount());

            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                publishGameWithExtrasToWds(game.getId());

                _transactionManager.commit(txStatus);
                txStatus = null;
            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

            //publish doc with total number of players for this round
            publishBracketRoundCount(nextRoundInSequence.getGameId(), nextRoundInSequence.getId(), nextRoundInSequence.getCurrentPlayerCount());

            //update the gamestats db
            _dao.setGameStats(new GameStats(nextRoundInSequence.getGameId()).withRemainingPlayers(nextRoundInSequence.getCurrentPlayerCount()));

//not really necessary. player count is sent at the end of each match, so this would be a redundant call
//            //send a socket.io message
//            if (_socketIoSocket != null) {
//                Map<String, Object> msg = new HashMap<>();
//                msg.put("count", nextRoundInSequence.getCurrentPlayerCount());
//
//                try {
//                    ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
//                    String message = _jsonMapper.writeValueAsString(msg);
//                    SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENDING");
//                    _socketIoSocket.emit("send_playercount", message);
//                    SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENT");
//                } catch (JsonProcessingException e) {
//                    _logger.error("unable to convert map to json", e);
//                }
//            }

            //wait a bit before starting the next round
            new Thread() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(round.getDurationBetweenActivitiesSeconds() * 1_000L);
                    } catch (InterruptedException ignored) {}

                    //start the MQE so the pairing occurs
                    getMQE().run(null);
                }
            }.start();

        } else {
            if (_logger.isDebugEnabled()) {
                _logger.info(MessageFormat.format("RME setting game status to CLOSED for game: {0}, id: {1}", game.getGameNames().get("en"), game.getId()));
            }
            game.setGameStatus(Game.GAME_STATUS.CLOSED);
            _shoutContestService.updateGameStatus(game.getId(), Game.GAME_STATUS.CLOSED);

            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                //update the WDS docs
                List<String> gameIds = new ArrayList<>();
                gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.DEFAULT, ISnowyowlService.GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));
                gameIds.addAll(_shoutContestService.publishWebDataStoreGameListDoc(ISnowyowlService.GAME_ENGINE, Game.GAME_TYPE.TESTER, ISnowyowlService.TEST_GAMES_LIST_NAME, Game.GAME_STATUS.OPEN, Game.GAME_STATUS.INPLAY));

                for (String gameIdToPublish : gameIds) {
                    publishGameWithExtrasToWds(gameIdToPublish);
                }

                publishGameWithExtrasToWds(game.getId());

                _transactionManager.commit(txStatus);
                txStatus = null;
            } finally {
                if (txStatus != null) {
                    _transactionManager.rollback(txStatus);
                    txStatus = null;
                }
            }

            subscriberIdToRoundPlayerMap.values().stream().forEach(rp -> {
                GamePlayer gamePlayer = subscriberIdToGamePlayerMap.get(rp.getSubscriberId());
                gamePlayer.setRank(rp.getRank());
                _shoutContestService.updateGamePlayer(gamePlayer);
            });

            List<RoundPlayer> rpList = _shoutContestService.getMostRecentRoundPlayersForGame(game.getId(), Arrays.asList(Round.ROUND_TYPE.BRACKET));

            //sanity checking (these are all things i've seen occur before that shouldn't be possible)
            boolean passedSanityCheck = true;
            Set<Long> rpListAlreadyExaminedSubscriberIds = new HashSet<>();
            for (RoundPlayer rp : rpList) {
                //sanity check 1: for every entry in the rpList, there MUST be a matching GamePlayer in the subscriberIdToGamePlayerMap
                //(except for bot players)
                if (subscriberIdToGamePlayerMap.get(rp.getSubscriberId()) == null && !botSubscriberIds.contains(rp.getSubscriberId())) {
                    //it's perfectly legit for there to be no GamePlayer record in the map at this point. The map is populated up top
                    // from all the players in the final round, but if a player didn't make it to the final round, they won't have an
                    // object and thus must be manually added here.

                    //FUTURE: performance enhancement: rather than retrieve these one by one, do a multi select once the rpList is retrieved

                    GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), rp.getSubscriberId());
                    if (gamePlayer != null) {
                        subscriberIdToGamePlayerMap.put(gamePlayer.getSubscriberId(), gamePlayer);
                    } else {
                        passedSanityCheck = false;
                        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                    "RME: sanity check FAIL: NO GAMEPLAYER FOUND: sId: {0,number,#}, gameId: {1}, roundId: {2}",
                                    rp.getSubscriberId(), rp.getGameId(), rp.getRoundId()));
                        }
                        _logger.error(MessageFormat.format(
                            "RME: sanity check FAIL: NO GAMEPLAYER FOUND: sId: {0,number,#}, gameId: {1}, roundId: {2}",
                            rp.getSubscriberId(), rp.getGameId(), rp.getRoundId()));
                    }
                }

                //sanity check 2: there MAY NOT be any duplicate subscriberId's in the rpList (i.e. you can't play twice in the same game)
                if (rpListAlreadyExaminedSubscriberIds.contains(rp.getSubscriberId())) {
                    passedSanityCheck = false;
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "RME: sanity check FAIL: DUPLICATE ROUNDPLAYER FOUND: sId: {0,number,#}, gameId: {1}",
                                rp.getSubscriberId(), rp.getGameId()));
                    }
                    _logger.error(MessageFormat.format(
                        "RME: sanity check FAIL: DUPLICATE ROUNDPLAYER FOUND: sId: {0,number,#}, gameId: {1}",
                        rp.getSubscriberId(), rp.getGameId()));
                } else {
                    rpListAlreadyExaminedSubscriberIds.add(rp.getSubscriberId());
                }

                //sanity check 3: EVERY entry in the rpList MUST have a determination
                if (rp.getDetermination() == null || rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.UNKNOWN) {
                    passedSanityCheck = false;
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "RME: sanity check FAIL: ROUNDPLAYER HAS NO DETERMINATION: sId: {0,number,#}, gameId: {1}, roundPlayerId: {2}",
                                rp.getSubscriberId(), rp.getGameId(), rp.getId()));
                    }
                    _logger.error(MessageFormat.format(
                        "RME: sanity check FAIL: ROUNDPLAYER HAS NO DETERMINATION: sId: {0,number,#}, gameId: {1}, roundPlayerId: {2}",
                        rp.getSubscriberId(), rp.getGameId(), rp.getId()));
                }
            }

            if (passedSanityCheck) {
                //assign payouts based on rank
                try {
                    getPayoutManager().assignPayouts(
                        game,
                        _shoutContestService.getRoundsForGame(game.getId()),
                        subscriberIdToGamePlayerMap,
                        rpList,
                        botSubscriberIdsSet,
                        sponsorPlayerIdsForGame,
                        new HashSet<>(_identityService.getSubscriberIdsForRole(Subscriber.ROLE.TESTER)),
                        _dao.getGamePayout(game.getId())
                    );
                } catch (PayoutManagerException e) {
                    //this shouldn't happen - any errors should have been caught before this
                    _logger.error(MessageFormat.format("RME: UNABLE TO ASSIGN PAYOUTS: {0}\n\t{1}", e.getErrorTypeCode(), e.getErrorDetails()), e);
                }
            } else {
                _logger.error("RME: NOT ASSIGNING PAYOUTS. SANITY CHECK FAILED!!!");
            }

            //any GamePlayer objects that didn't get awarded (via the payout engine) need their determination set (otherwise it will stay as INPLAY even though the game is complete)
            subscriberIdToGamePlayerMap.forEach( (subscriberId,gamePlayer) -> {
                if (gamePlayer.getDetermination() == GamePlayer.GAME_PLAYER_DETERMINATION.INPLAY) {
                    gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.AWARDED);
                    gamePlayer.setPayoutAwardedAmount(0D);
                    _shoutContestService.updateGamePlayer(gamePlayer);
                }
            });

            //send out the "game_result" sync message to everyone
            subscriberIdToGamePlayerMap.forEach( (subscriberId, gamePlayer) -> {
                Subscriber s = _identityService.getSubscriberById(subscriberId);
                List<RoundPlayer> roundPlayers = _shoutContestService.getRoundPlayersForGame(gamePlayer.getGameId(), subscriberId);
                enqueueSyncMessage(
                        _jsonMapper, _syncService, _logger,
                        gamePlayer.getGameId(), ISnowyowlService.SYNC_MESSAGE_GAME_RESULT,
                        new FastMap<> ("roundPlayers", roundPlayers, "gamePlayer", gamePlayer),
                        s, _socketIoSocket, _triggerService);
            });

            //clear out the bots/subscriber question list
//_logger.info(MessageFormat.format("BotEngine (RoundManagementEngine, finishing game): releasing bots for game: {0}", game.getId()));
            _dao.releaseBotsForGame(game.getId());
            _dao.clearSubscriberQuestions(game.getId());

            //if there are any non-dequeued entries in the match_queue table for this game, they should be cleared out
            List<Long> subscriberIdsThatWereNeverMatchedForGame = _shoutContestService.getSubscriberIdsThatWereNeverMatchedForGame(game.getId());
            if (subscriberIdsThatWereNeverMatchedForGame.size() > 0) {
                if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                    SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                            "RME: these subscribers were in the match_queue table but were never matched: {0}",
                            subscriberIdsThatWereNeverMatchedForGame));
                }

                _shoutContestService.removeSubscribersThatWereNeverMatchedForGame(game.getId());
            }
        }
    }

    private void publishBracketRoundCount(String gameId, String roundId, int numPlayers)
    {
        WebDataStoreObject object = new WebDataStoreObject();
        object.addResponseHeader("Content-Type", "application/json;charset=utf-8");
        //object.setExpirationDate(new Date(new Date().getTime() + EXPIRES_1_HOUR_MS));
        object.setPath("/" + gameId + "/" + "numplayers_" + roundId + ".json");
        Map<String, Object> data = new FastMap<>("count", numPlayers);
        try {
            object.setData(_jsonMapper.writeValueAsBytes(data));

            try {
                _wdsService.createOrUpdateObjectSync(object, 0);
            } catch (WebDataStoreException | InterruptedException e) {
                _logger.error("unable to publish numplayers.json for game: " + gameId, e);
            }

        } catch (JsonProcessingException e) {
            _logger.error("unable to convert numPlayers to json", e);
        }
    }
}
