package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerScoreBracketFixedLife
extends BaseHandlerScoreBracket
{
    private static Logger _logger = Logger.getLogger(HandlerScoreBracketFixedLife.class);

    @Autowired
    protected SubscriberStatsHandler _subscriberStatsHandler;

    @Autowired
    protected CurrentRankCalculator _currentRankCalculator;

    @Autowired
    protected MatchMaker _matchMaker;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private ITriggerService _triggerService;

    @SuppressWarnings("unchecked")
    @Override
    //MessageProcessor
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case SCORE_BRACKET_ROUND:
//this was in when i thought a different bracket score handler might be needed. i don't think it is. this handler should cover both cases just fine.
//                String engineType = ((Game)((Map<String, Object>)message.payload).get("game")).getEngineType();
//                if (!engineType.equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife)) return;

                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId()
                    );

                    _busLogger.debug(MessageFormat.format("HandlerScoreBracketFixedLife received SCORE_BRACKET_ROUND\n{0}", JsonUtil.print(map)));
                }
                handleScoreBracketRound(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleScoreBracketRound(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        List<Long> botIds = (List<Long>) payload.get("botIds");
        List<Long> sponsorIds = (List<Long>) payload.get("sponsorIds");

        //grab all the data up front so it doesn't have to get re-retrieved over and over in the loops below
        wrapInTransaction((x) -> {

            Map<String, List<MatchPlayer>> matchIdToMatchPlayersMap = new HashMap<>();
            Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap = new HashMap<>();
            Map<Long, GamePlayer> subscriberIdToGamePlayerMap = new HashMap<>();

            List<Match> matches = _shoutContestService.getMatchesByRoundAndStatus(round.getId(), Match.MATCH_STATUS.PROCESSING);
            int numPlayersWithLivesLeft = 0;

            for (Match match : matches) {
                List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());
                matchIdToMatchPlayersMap.put(match.getId(), matchPlayers);

                for (MatchPlayer matchPlayer : matchPlayers) {
                    RoundPlayer roundPlayer = _shoutContestService.getRoundPlayer(matchPlayer.getRoundPlayerId());
                    GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), roundPlayer.getSubscriberId());
                    subscriberIdToRoundPlayerMap.put(roundPlayer.getSubscriberId(), roundPlayer);
                    subscriberIdToGamePlayerMap.put(roundPlayer.getSubscriberId(), gamePlayer);

//TODO: this only applies for fixedlife, NOT multilife
                    if (gamePlayer.getCountdownToElimination() != null && matchPlayer.getDetermination() == MatchPlayer.MATCH_PLAYER_DETERMINATION.LOST) {
                        gamePlayer.setCountdownToElimination(gamePlayer.getCountdownToElimination() - 1);
                        _shoutContestService.updateGamePlayer(gamePlayer);
                    }

                    if (gamePlayer.getCountdownToElimination() != null && gamePlayer.getCountdownToElimination() > 0) {
                        numPlayersWithLivesLeft++;
                    }
                }
            }

            boolean lastRound = numPlayersWithLivesLeft <= 1;
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("isLastRound: {0}", lastRound));
}

            Round nextRoundInSequence = null;
            if (!lastRound) {
                //grab the next round (it will have already been created)
                int nextRoundSequence = round.getRoundSequence()+1;
                nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), nextRoundSequence);
            }

            //update each Match and the MatchPlayer's for each match
            for (Match match : matches) {
                processMatch(
                    game, round, match,
                    matchIdToMatchPlayersMap, subscriberIdToRoundPlayerMap, subscriberIdToGamePlayerMap,
                    botIds, lastRound, nextRoundInSequence);
            }

            //update each RoundPlayer
            List<RoundPlayer> roundPlayersArray = new ArrayList<>(subscriberIdToRoundPlayerMap.values());
            roundPlayersArray.sort((rp1, rp2) -> rp2.getSkill().compareTo(rp1.getSkill()));
            for (int i = 0; i < roundPlayersArray.size(); i++) {
                RoundPlayer roundPlayer = roundPlayersArray.get(i);
                roundPlayer.setRank(i+1);
                _shoutContestService.updateRoundPlayer(roundPlayer);
            }

            //close the round
if (_logger.isDebugEnabled()) {
    _logger.debug("closing bracket round: " + round.getId() + ", " + round.getRoundNames().get("en"));
}
            round.setRoundStatus(Round.ROUND_STATUS.CLOSED);
            _shoutContestService.updateRoundStatus(round.getId(), lastRound, Round.ROUND_STATUS.CLOSED);

            //publish the subscriber stats documents
            if (game.isProductionGame()) {
                List<Long> playerIdsStillInGame = roundPlayersArray.stream()
                    .filter(rp -> rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.WON || rp.getDetermination() == RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED)
                    .filter(rp -> !botIds.contains(rp.getSubscriberId()) )
                    .map(rp -> rp.getSubscriberId())
                    .collect(Collectors.toList());
                Message msg = HandlerDocPublisher.getSubscriberStatsMessage(game.getId(), playerIdsStillInGame);
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg.type);
                }
                _messageBus.sendMessage(msg);
            }

            if (!lastRound) {
                processNotLastRound(game, round, subscriberIdToRoundPlayerMap, subscriberIdToGamePlayerMap, nextRoundInSequence, botIds, sponsorIds);
            } else {
                Message msg = HandlerCloseGame.getCloseGameMessage(game, subscriberIdToRoundPlayerMap, subscriberIdToGamePlayerMap, botIds, sponsorIds);
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg.type);
                }
                _messageBus.sendMessage(msg);
            }

            return null;
        }, null);
    }

    //wrapped in a transaction
    private void processMatch(
        Game game, Round round, Match match,
        Map<String, List<MatchPlayer>> matchIdToMatchPlayersMap, Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap, Map<Long, GamePlayer> subscriberIdToGamePlayerMap,
        List<Long> botIds, boolean lastRound, Round nextRoundInSequence)
    {
if (_logger.isDebugEnabled()) {
    _logger.debug("processing match: " + match.getId());
}

        List<MatchPlayer> matchPlayers = matchIdToMatchPlayersMap.get(match.getId());
        for (MatchPlayer matchPlayer : matchPlayers) {
//if (_logger.isDebugEnabled()) {
//    _logger.debug("\tprocessing matchPlayer: " + matchPlayer.getId());
//}

            RoundPlayer roundPlayer = subscriberIdToRoundPlayerMap.get(matchPlayer.getSubscriberId());
            GamePlayer gamePlayer =  subscriberIdToGamePlayerMap.get(matchPlayer.getSubscriberId());

            if (game.isProductionGame() && !botIds.contains(gamePlayer.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(gamePlayer.getSubscriberId(), SubscriberStats.STATS_TYPE.BRACKET_ROUNDS_PLAYED, 1);
            }

            switch (matchPlayer.getDetermination())
            {
                case WON: {
                    roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.WON);

                    if (lastRound) {
                        gamePlayer.setLastRoundId(round.getId()); //here, last should say previous
                        gamePlayer.setNextRoundId(null);

                    } else {
                        //there is another round to play
                        gamePlayer.setLastRoundId(round.getId());
                        gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                    }
                }
                break;

                case SAVED: {
                    roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.SAVED);

                    if (lastRound) {
                        gamePlayer.setLastRoundId(round.getId()); //here, last should say previous
                        gamePlayer.setNextRoundId(null);

                    } else {
                        //there is another round to play
                        gamePlayer.setLastRoundId(round.getId());
                        gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                    }
                }
                break;

                case LOST: {
                    roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.LOST);

                    if (lastRound) {
                        gamePlayer.setLastRoundId(round.getId());
                        gamePlayer.setNextRoundId(null);

                    } else {
                        //they don't necessarily go the next round; they may have been eliminated!!!
                        gamePlayer.setLastRoundId(round.getId());
                        if (gamePlayer.getCountdownToElimination() == null || gamePlayer.getCountdownToElimination() > 0) {
                            gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                        } else {
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
        } //matchPlayer

        //close the match
        match.setMatchStatus(Match.MATCH_STATUS.CLOSED);
        match.setMatchStatusSetAt(new Date());
        _shoutContestService.updateMatch(match);
    }

    //wrapped in a transaction
    private void processNotLastRound(
        Game game, Round round, Map<Long, RoundPlayer> subscriberIdToRoundPlayerMap, Map<Long, GamePlayer> subscriberIdToGamePlayerMap, Round nextRoundInSequence,
        List<Long> botIds, List<Long> sponsorIds)
    {
        //get everyone who played this round
        List<Long> subscriberIds = _shoutContestService.getSubscriberIdsForRound(round.getId());
_logger.info(MessageFormat.format(">>> round: {0}, subscribers in that round: {1}", round.getRoundNames().get("en"), subscriberIds));
_logger.info(MessageFormat.format(">>> number of RoundPlayers in lookup map: {0}", subscriberIdToRoundPlayerMap.size()));

        //make sure that everyone who played this round also has an entry in subscriberIdToRoundPlayerMap
        for (long sId : subscriberIds) {
            if (subscriberIdToRoundPlayerMap.get(sId) == null) {
                RoundPlayer rp = _shoutContestService.getRoundPlayer2(round.getId(), sId);
                if (rp == null) {
                    _logger.error(MessageFormat.format("NO RoundPlayer found for sId: {0,number,#}, roundId: {1}", sId, round.getId() ), new Exception());
                } else {
_logger.info(MessageFormat.format(">>> adding subscriber {0,number,#} to roundPlayer lookup map", sId));
                    subscriberIdToRoundPlayerMap.put(sId, rp);
                }
            }
        }

        //publish the subscriber ranks for this game
        Message msg = HandlerDocPublisher.getSubscriberGameRankMessage(game, subscriberIds, subscriberIdToRoundPlayerMap, botIds);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg.type);
        }
        _messageBus.sendMessage(msg);

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

            //any players with no lives left are eliminated
            if (gamePlayer.getCountdownToElimination() == null || gamePlayer.getCountdownToElimination() <= 0) {
                if (!botIds.contains(gamePlayer.getSubscriberId()) && !sponsorIds.contains(gamePlayer.getSubscriberId()) )
                //send an eliminated message
                enqueueSyncMessage(
                        JsonUtil.getObjectMapper(), _syncService, _logger,
                        game.getId(), ISnowyowlService.SYNC_MESSAGE_ELIMINATED, null, s, _socket, _triggerService);
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

_logger.info(MessageFormat.format(">>> HandlerScoreBracketFixedLife::processNotLastRound, addRoundPlayer. roundId: {0}, subscriberId: {1}", roundPlayer.getRoundId(), roundPlayer.getSubscriberId()));
            _shoutContestService.addRoundPlayer(roundPlayer, nextRoundInSequence.getRoundType());
            _currentRankCalculator.clear(game.getId());

            //make them available for pairing
            _matchMaker.addPlayerForMatching(roundPlayer);

            //send the "joined_round" sync message
            enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_JOINED_ROUND,
                    new FastMap<String, Object>(
                        "roundPlayer", roundPlayer,
                        "currentPlayerCount", nonFilteredSubscriberCount,
                        "roundType", "BRACKET"
                    ), s, _socket, _triggerService);
        }

        //update the next round to have the same subscribers and set the status to READY so pairing can begin
        nextRoundInSequence.setCurrentPlayerCount(nonFilteredSubscriberCount);
        nextRoundInSequence.setRoundStatus(Round.ROUND_STATUS.OPEN);
        _shoutContestService.updateRoundStatusAndPlayerCount(nextRoundInSequence.getId(), nextRoundInSequence.getRoundStatus(), nextRoundInSequence.getCurrentPlayerCount());

        //publish the updated game w/extras
        Message msg4 = HandlerDocPublisher.getGameWithExtrasMessage(game.getId(), null, null, null);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg4.type);
        }
        _messageBus.sendMessage(msg4);

        //publish doc with total number of players for this round
        Message msg3 = HandlerDocPublisher.getBracketRoundCountMessage(nextRoundInSequence.getGameId(), nextRoundInSequence.getId(), nextRoundInSequence.getCurrentPlayerCount());
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg3.type);
        }
        _messageBus.sendMessage(msg3);

        //update the gamestats db
        _gameStatsHandler.setGameStats(new GameStats(nextRoundInSequence.getGameId()).withRemainingPlayers(nextRoundInSequence.getCurrentPlayerCount()));

        //kick off a message for the next round
        DelayedMessage msg2 = HandlerBeginBracketPlay.getNextBracketPlayBeginAtMessage(game, nextRoundInSequence, round.getDurationBetweenActivitiesSeconds() * 1_000L);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerScoreBracketFixedLife sending message: " + msg2.type);
        }
        _messageBus.sendDelayedMessage(msg2);
    }

}
