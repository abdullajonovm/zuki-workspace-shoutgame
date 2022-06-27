package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_STATUS;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.collector.PoolPlayMatchesInProgressException;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.common.PayoutTablePublisher;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.EngineCoordinator;
import tv.shout.snowyowl.engine.PayoutManagerException;
import tv.shout.snowyowl.engine.PayoutManagerFixedRoundCommon;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class MockCommonBusinessLogic
extends CommonBusinessLogic
implements PayoutTablePublisher
{
    private static Logger _logger = Logger.getLogger(MockCommonBusinessLogic.class);

    public MockCommonBusinessLogic(
            IShoutContestService shoutContestService, IDaoMapper dao, CurrentRankCalculator currentRankCalculator, EngineCoordinator engineCoordinator, IIdentityService identityService,
            GameStatsHandler gameStatsHandler)
    {
        _shoutContestService = shoutContestService;
        _dao = dao;
        _currentRankCalculator = currentRankCalculator;
        _engineCoordinator = engineCoordinator;
        _identityService = identityService;
        _gameStatsHandler = gameStatsHandler;
    }

    //can't be in the constructor or it causes a circular dependency since the bot engine also needs this class
    public void setBotEngine(BotEngine botEngine)
    {
        _botEngine = botEngine;
    }

    //can't be in the constructor or it causes a circular dependency since the match maker also needs this class
    public void setMatchMaker(MatchMaker matchMaker)
    {
        _matchMaker = matchMaker;
    }

    @Override
    public void joinGameAsBot(Game game, long botSubscriberId, String nextRoundIdForBot)
    {
        //add (or update) the GamePlayer record
        GamePlayer gamePlayer = new GamePlayer(game.getId(), botSubscriberId);

        //number of lives comes from two different sources, depending on the engineType
        switch (game.getEngineType())
        {
            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife:
                if (game.getBracketEliminationCount() != null) {
                    gamePlayer.setCountdownToElimination(game.getBracketEliminationCount());
                }
                break;

            case ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_MultiLife:
                gamePlayer.setCountdownToElimination(game.getStartingLivesCount());
                break;
        }

        _shoutContestService.addGamePlayer(gamePlayer, nextRoundIdForBot, Round.ROUND_STATUS.VISIBLE, Round.ROUND_STATUS.OPEN, Round.ROUND_STATUS.FULL);
    }

    @Override
    public Round setPlayerAvailabilityAsAi(Game game, Round round, long botSubscriberId, boolean lastCall, boolean makeAvailableForPairing)
    {
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(game.getId(), botSubscriberId);

        if (round == null) {
            String nextRoundId = gamePlayer.getNextRoundId();
            round = _shoutContestService.getRound(nextRoundId);
        }

        //increment the round's player count (and set to full if nobody else can fit in)
        round.setCurrentPlayerCount(round.getCurrentPlayerCount() + 1);
        if (round.getCurrentPlayerCount() >= round.getMaximumPlayerCount() && round.getRoundStatus() == Round.ROUND_STATUS.OPEN) {
            round.setRoundStatus(ROUND_STATUS.FULL);
        }

        //don't actually do this until the bots are finished filing in, otherwise it does it over and over and over
        if (lastCall) {
            _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());
        }

        RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), botSubscriberId);

        //add the RoundPlayer record
        _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

        return round;
    }

    @Override
    public long submitAnswer(SubscriberQuestionAnswer sqa, String selectedAnswerId)
    {
        long durationMilliseconds = System.currentTimeMillis() - sqa.getQuestionPresentedTimestamp().getTime();

        sqa.setSelectedAnswerId(selectedAnswerId);
        sqa.setDurationMilliseconds(durationMilliseconds);

        _dao.setAnswerOnSubscriberQuestionAnswer(sqa);

        return durationMilliseconds;
    }

    @Override
    public void removeFromRound(String gameId, Round round, RoundPlayer roundPlayer)
    {
        _shoutContestService.updateRoundPlayerDetermination(roundPlayer.getId(), RoundPlayer.ROUND_PLAYER_DETERMINATION.ABANDONED);
        if (_currentRankCalculator != null) _currentRankCalculator.clear(gameId);

        //do the same with the gameplayer object
        GamePlayer gamePlayer = _shoutContestService.getGamePlayer(gameId, roundPlayer.getSubscriberId());
        gamePlayer.setDetermination(GamePlayer.GAME_PLAYER_DETERMINATION.REMOVED);
        _shoutContestService.updateGamePlayer(gamePlayer);

        //decrement player count
        round.setCurrentPlayerCount(round.getCurrentPlayerCount() - 1);
        if (round.getRoundStatus() == ROUND_STATUS.FULL) {
            //doesn't necessarily go to open. might have been one over the max if a bot was playing
            if (round.getCurrentPlayerCount() < round.getMaximumPlayerCount()) {
                round.setRoundStatus(ROUND_STATUS.OPEN);
            }
        }
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());
    }

    @Override
    public List<Round> startBracketPlay(Game game, List<Round> rounds, IWebDataStoreService wdsService, long beginsInMs)
    throws PoolPlayMatchesInProgressException
    {
        //grab all of the pool play rounds and set them to FULL - this will prevent anyone else from joining, and allow any in progress to complete
_logger.info(MessageFormat.format("BBP (1/13): changing all POOL rounds to FULL for game: {0} [{1}] and waiting for in progress matches to complete", game.getId(), game.getGameName("en")));
        rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
            .forEach(r -> {
                _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.FULL);
            });

        //change the game state to INPLAY (no new players can enter)
_logger.info(MessageFormat.format("BBP (2/13): changing staus to INPLAY for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        _shoutContestService.updateGameStatus(game.getId(), Game.GAME_STATUS.INPLAY);

        //set all of the pool play rounds to closed
_logger.info(MessageFormat.format("BBP (3/13): changing all POOL rounds to CLOSED for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        rounds.stream()
            .filter(r -> r.getRoundType() == Round.ROUND_TYPE.POOL)
            .forEach(r -> {
                _shoutContestService.updateRoundStatus(r.getId(), r.isFinalRound(), Round.ROUND_STATUS.CLOSED);
            });

        return rounds;
    }

    @Override
    public void completeBracketPlayStart(Game game, List<Round> rounds, Round round, long beginsInMs, long then, IWebDataStoreService wdsService, Socket socketIoSocket)
    {
_logger.info(MessageFormat.format("BBP (4/13): releasing bots used during POOL play for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        //get the bots in the game, then release all the bots
        List<Long> botSubscriberIds = _botEngine.getBotsForGame(game.getId());
        _dao.releaseBotsForGame(game.getId());

        //also remove any GamePlayer records for those bots
        for (long botSubscriberId : botSubscriberIds) {
            _shoutContestService.removeGamePlayer(botSubscriberId, game.getId());
        }

        //get everyone who's in the game and move them into the first bracket round (except the bots. clear out all the bots)
        List<GamePlayer> gamePlayers = _shoutContestService.getGamePlayersForGame(game.getId());
        List<Long> bracketPlayerIds = new ArrayList<>();
        Map<Long, RoundPlayer> joinGameRoundPlayerMap = new HashMap<>();

        int actualPlayers = 0;
_logger.info(MessageFormat.format("BBP (5/13): moving POOL players to BRACKET round for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        for (GamePlayer gp : gamePlayers) {
            long subscriberId = gp.getSubscriberId();

            //filter out the bots
            if (botSubscriberIds.contains(subscriberId)) {
                continue;
            }

            //filter out free players
            if (gp.isFreeplay()) {
                continue;
            }

            actualPlayers++;
            bracketPlayerIds.add(subscriberId);

            //add the RoundPlayer record (move the rank forward from the previous round)
            RoundPlayer roundPlayer = new RoundPlayer(game.getId(), round.getId(), subscriberId);
            RoundPlayer oldRoundPlayer = _shoutContestService.getMostRecentRoundPlayer(game.getId(), subscriberId);
            if (oldRoundPlayer != null) {
                roundPlayer.setRank(oldRoundPlayer.getRank());
                roundPlayer.setSkillAnswerCorrectPct(oldRoundPlayer.getSkillAnswerCorrectPct());
                roundPlayer.setSkillAverageAnswerMs(oldRoundPlayer.getSkillAverageAnswerMs());
                roundPlayer.setSkill(oldRoundPlayer.getSkill());
            }

            _shoutContestService.addRoundPlayer(roundPlayer, round.getRoundType());

            //make subscriber available for pairing
            _matchMaker.addPlayerForMatching(roundPlayer);

            joinGameRoundPlayerMap.put(subscriberId, roundPlayer);
        }

_logger.info(MessageFormat.format("BBP (6/13): adding bots to game. actualPlayerCount (i.e. not bots and non freeplayers): {0}", actualPlayers));
        int numBotsAdded = 0;

        //if there aren't the right amount of players, add in bots as necessary (odd-man out)
        botSubscriberIds.clear();
        int matchPlayerCount = round.getMatchPlayerCount();
        if (actualPlayers < round.getMaximumPlayerCount() && actualPlayers % matchPlayerCount != 0) {
            int numBotsNeeded = matchPlayerCount - (actualPlayers % matchPlayerCount);
            numBotsAdded += numBotsNeeded;
            botSubscriberIds = _botEngine.addBotPlayers(game.getId(), round.getId(), numBotsNeeded);
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("added {0} bot[s] to BRACKET play due to odd man out", numBotsNeeded));
            }
        }

        //if the debug flag is set to fill up the game with bots, do so now
        //if (_percentageToFillBracketWithBots > 0D) {
        if (game.isFillWithBots()) {
            int totalPlayers = actualPlayers + botSubscriberIds.size();
            int numPlayersNeededToFillToDesiredAmount = round.getMaximumPlayerCount() - totalPlayers;

            //if specified, cap the maximum number of bots that can be added
            if (game.getMaxBotFillCount() != null && numPlayersNeededToFillToDesiredAmount > game.getMaxBotFillCount()) {
                numPlayersNeededToFillToDesiredAmount = game.getMaxBotFillCount();
            }

            if (numPlayersNeededToFillToDesiredAmount > 0) {
                numBotsAdded += numPlayersNeededToFillToDesiredAmount;
                botSubscriberIds.addAll(_botEngine.addBotPlayers(game.getId(), round.getId(), numPlayersNeededToFillToDesiredAmount));
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("added {0} bot[s] to BRACKET play due to game.fillWithBots and game.maxBotFillCount", numPlayersNeededToFillToDesiredAmount));
                }
            }
        }

        //update the next round to have the same subscribers and set the status to READY so pairing can begin
_logger.info(MessageFormat.format("BBP (7/13): updating bracket count to {0} for game: {1} [{2}]", actualPlayers + numBotsAdded, game.getId(), game.getGameName("en")));
        round.setCurrentPlayerCount(actualPlayers + numBotsAdded);
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

        //the mock version doesn't actually publish anything, but it does have the proper side effect of returning the player count map
        Map<String, Integer> playerCountMap = _shoutContestService.publishGamePlayerCountToWds(game.getId(), _botEngine.getBotsForGame(game.getId()));

        //per bx, 25 Oct 2018, create ALL bracket rounds up front, now that the player count is known
        int numBracketRoundsToCreate = _engineCoordinator.getNumberOfBracketRounds(game, round.getCurrentPlayerCount()) -1; //-1 because the first bracket round has already been created
        Round roundToBeCloned = round;
        for (int i=0; i<numBracketRoundsToCreate; i++) {
            Round newRound = roundToBeCloned.cloneForNextRound();
            _shoutContestService.addRound(newRound);
            roundToBeCloned = newRound;
        }

_logger.info(MessageFormat.format("BBP (8/13): no-op: republishing WDS doc for game: {0} [{1}]", game.getId(), game.getGameName("en")));

        //figure out the final payout table and publish it to the WDS
_logger.info(MessageFormat.format("BBP (9/13): publishing payout table for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        /*List<PayoutTableRow> rows =*/ try {
            publishPayoutTable(game, rounds, round.getCurrentPlayerCount(), rounds.get(0).getMaximumPlayerCount(), _engineCoordinator, wdsService, _dao, _logger);
        } catch (PayoutManagerException e1) {
            _logger.error("unable to publish payout table", e1);
            return;
        }

        //update the game stats table with the total number of players remaining in the game
        _gameStatsHandler.setGameStats(new GameStats(game.getId())
                .withRemainingPlayers(round.getCurrentPlayerCount())
                .withRemainingSavePlayerCount(PayoutManagerFixedRoundCommon.getNumberOfSaves(round.getCurrentPlayerCount()))
        );

_logger.info("sending joined_round sync message to all (real) players");
        for (long subscriberId : bracketPlayerIds) {
            //send the "joined_round" sync message
            Subscriber s = _identityService.getSubscriberById(subscriberId);
            enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND,
                    new FastMap<String, Object>(
                            "roundPlayer", joinGameRoundPlayerMap.get(subscriberId),
                            "payedPlayerCount", playerCountMap.get("payedPlayerCount"),
                            "freePlayerCount", playerCountMap.get("freePlayerCount"),
                            "roundType", "BRACKET"
                    ), s, socketIoSocket, _triggerService);
        }

        //notify everyone that bracket play is about to begin
_logger.info(MessageFormat.format("BBP (10/13): no-op: notifying players that BRACKET play is about to begin for game: {0} [{1}]", game.getId(), game.getGameName("en")));

        //it took some amount of time to process everything. take that time difference into account before starting the final countdown
        long now = System.currentTimeMillis();
        beginsInMs -= (now - then);

        //wait for the (remaining) prearranged amount of time before beginning so that people can have a chance to get ready
_logger.info(MessageFormat.format(
        "BBP (11/13): waiting {0} ms before starting BRACKET play for game: {1} [{2}]",
        beginsInMs > 0 ? beginsInMs : 0, game.getId(), game.getGameName("en")));

        //start the wait countdown, updating the countdown WDS doc every second (or so)
        while (beginsInMs > 0) {
            long aMs = System.currentTimeMillis();
            try {
                Thread.sleep(1_000L);
                long bMs = System.currentTimeMillis();

                beginsInMs -= (bMs - aMs);
                _logger.debug("bracket play begins in: " + beginsInMs);

            } catch (InterruptedException e) {
                _logger.warn("BRACKET play waiting thread was prematurely interrupted", e);
            }
        }

_logger.info(MessageFormat.format("BBP (12/13): setting first BRACKET round to OPEN for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        round.setRoundStatus(Round.ROUND_STATUS.OPEN);
        _shoutContestService.updateRoundStatusAndPlayerCount(round.getId(), round.getRoundStatus(), round.getCurrentPlayerCount());

        //start bracket pairing
_logger.info(MessageFormat.format("BBP (13/13): beginning BRACKET play for game: {0} [{1}]", game.getId(), game.getGameName("en")));
        _matchMaker.createBracketMatches(game, round, true);
    }

    @Override
    public void enqueueSyncMessage(
        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
        String contextualId, String messageType, Map<String, Object> payload,
        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
    {
        _logger.debug(MessageFormat.format(
                "enqueueing sync message. recipient: {0,number,#}, messageType: {1}",
                subscriber.getSubscriberId(), messageType));
    }

    @Override
    public void publishJsonWdsDoc(Logger logger, IWebDataStoreService wdsService, Date expireDate, String path, Object data)
    {
        logger.debug("MOCK published doc: " + path);
    }
}
