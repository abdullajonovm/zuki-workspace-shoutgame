package tv.shout.snowyowl.engine.fixedroundsinglelife;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.jdbc.effect.TransactionSideEffectManager;
import com.meinc.launcher.serverprops.ServerPropertyHolder;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Match.MATCH_STATUS;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchPlayer.MATCH_PLAYER_DETERMINATION;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.engine.MMEFixedRoundCommon;
import tv.shout.snowyowl.engine.MMEProcess;
import tv.shout.snowyowl.engine.RMEFixedRoundCommon;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.snowyowl.service.SnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class MMEFixedRoundSingleLife
extends MMEFixedRoundCommon
{
    private static Logger _logger = Logger.getLogger(MMEFixedRoundSingleLife.class);

//    @Value("${mme.concurrent.threads:4}")
//    private int CONCURRENT_THREAD_POOL_SIZE;

    @Value("${mme.tiebreaker.delay.ms:0}")
    private long _tiebreakerDelayMs;

    @Resource(name="rmeFixedRoundSingleLife")
    private RMEFixedRoundCommon _roundManagementEngine;

    @Autowired
    private CurrentRankCalculator _currentRankCalculator;

    private Lock _saveCounterLock = new ReentrantLock();

    @PostConstruct
    public void onPostConstruct()
    {
        ServerPropertyHolder.addPropertyChangeListener(
            "mme\\.",
            (properties) -> {
                properties.forEach(change -> {
                    /*if ("mme.concurrent.threads".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("mme.concurrent.threads changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        CONCURRENT_THREAD_POOL_SIZE = Integer.parseInt(change.newValue);

                    } else*/ if ("mme.tiebreaker.delay.ms".equals(change.key)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug(MessageFormat.format("mme.tiebreaker.delay.ms changed from {0} to {1}", change.oldValue, change.newValue));
                        }
                        _tiebreakerDelayMs = Long.parseLong(change.newValue);
                    }
                });
            }
        );

        _mmeCommon.setPrefix("FixedRoundSingleLifeMME");
    }

    @Override
    public String getType()
    {
        return ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife;
    }

    @Override
    public MATCH_STATUS[] getSupportedMatchStatuses()
    {
        return new MATCH_STATUS[] {Match.MATCH_STATUS.NEW, Match.MATCH_STATUS.OPEN, Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION, Match.MATCH_STATUS.WAITING_FOR_TIEBREAKER_QUESTION};
    }

    @Override
    public RMEFixedRoundCommon getRME()
    {
        return _roundManagementEngine;
    }

    @Override
    public void scoreQuestion(Game game, Round round, Match match, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, boolean isQuestionTimedOut, List<Long> botsInGame)
    {
        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            String sIds = sqas.stream().map(sqa -> sqa.getSubscriberId() + "").collect(Collectors.joining(","));
            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                    "MME: scoring question. mqId: {0}, timedOut: {1}, sids: {2}}",
                    matchQuestion.getId(), isQuestionTimedOut, sIds));
        }

        boolean isTieBreakerQuestion = _dao.isTieBreakerQuestion(match.getGameId(), match.getId());
        final String correctAnswerId = getCorretAnswerId(matchQuestion.getQuestionId());

        //if the question timed out, find anyone who didn't answer - we know their determination (LOST_TIMEOUT)
        if (isQuestionTimedOut) {
            sqas.stream()
                .filter(sqa -> sqa.getSelectedAnswerId() == null)
                .forEach(sqa -> {
                    sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT);

                    if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: LOST_TIMEOUT (did not answer in time)",
                                sqa.getSubscriberId(), sqa.getId()));
                    }
                });
        }

        //get a list of players who answered correctly (sorted by answer time, fastest answer first)
        List<SubscriberQuestionAnswer> correctSqas = sqas.stream()
                .filter(a -> correctAnswerId.equals(a.getSelectedAnswerId()))
                .sorted((a1, a2) -> a1.getDurationMilliseconds().compareTo(a2.getDurationMilliseconds()))
                .collect(Collectors.toList());

        //get a list of players who answered incorrectly (but that didn't timeout)
        List<SubscriberQuestionAnswer> incorrectSqas = sqas.stream()
                .filter(a -> ! correctAnswerId.equals(a.getSelectedAnswerId()))
                .filter(a -> a.getDetermination() != SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT)
                .sorted((a1, a2) -> a1.getDurationMilliseconds().compareTo(a2.getDurationMilliseconds()))
                .collect(Collectors.toList());

        if (isTieBreakerQuestion) {
            scoreQuestionAsTieBreaker(
                game, round, match, matchQuestion, sqas, isQuestionTimedOut, botsInGame,
                correctAnswerId, correctSqas, incorrectSqas);
        } else {
            scoreQuestionAsRegular(
                game, round, match, matchQuestion, sqas, isQuestionTimedOut, botsInGame,
                correctAnswerId, correctSqas, incorrectSqas);
        }
    }

    private void scoreQuestionAsTieBreaker(
        Game game, Round round, Match match, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, boolean isQuestionTimedOut, List<Long> botsInGame,
        final String correctAnswerId, List<SubscriberQuestionAnswer> correctSqas, List<SubscriberQuestionAnswer> incorrectSqas)
    {
        //tiebreaker scoring rules:
        // if multiple got it correct: fastest correct score wins
        // else if only one got it correct: they win
        // else if nobody got it correct: faster answer wins
        // else (if everyone timed out): ??? (randomly pick one?)

        SubscriberQuestionAnswer winnerSqa;

        if (correctSqas.size() > 0) {
//_logger.info("marking fastest correct answer as winner");
            //fastest answer wins, everyone else loses
            winnerSqa = correctSqas.get(0);

        } else {
            //nobody got it correct. find everyone who answered at all, sorted by time
            if (incorrectSqas.size() > 0) {
//_logger.info("marking fastest incorrect answer as winner");
                //fastest answer wins, everyone else loses
                winnerSqa = incorrectSqas.get(0);

            } else {
//_logger.info("picking random subscriber as winner");
                //nobody got it right, nobody even answered
                //TODO: ???????????
                //until i hear back, i'm just picking one randomly - shawker 26 apr 2018
                winnerSqa = sqas.get(new Random().nextInt(sqas.size()));
            }
        }

        //mark anyone not the winner as a loser
        sqas.forEach(sqa -> {
            if (sqa.getSubscriberId() == winnerSqa.getSubscriberId()) {
                sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_CORRECT);
                sqa.setWon(true);

                if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_CORRECT);
                }

            } else {
                sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);

                if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
                }
            }
        });

        matchQuestion.setWonSubscriberId(winnerSqa.getSubscriberId());
        matchQuestion.setDetermination(Match.MATCH_DETERMINATION.WINNER);
        matchQuestion.setMatchQuestionStatus(Match.MATCH_STATUS.CLOSED);
        matchQuestion.setCompletedDate(new Date());
        _dao.updateMatchQuestion(matchQuestion);

        //update the tiebreaker info
        long winnerSubscriberId = (winnerSqa.getSubscriberId());
        _dao.addTieBreakerWinnerSubscriberId(match.getGameId(), match.getId(), winnerSubscriberId);

        //send out "question_result" sync messages to all players
        sqas.stream().forEach(sqa -> {
            _dao.updateSubscriberQuestionAnswer(sqa);
            Subscriber s = _identityService.getSubscriberById(sqa.getSubscriberId());
            enqueueSyncMessage(
                    _jsonMapper, _syncService, _logger,
                    sqa.getGameId(), ISnowyowlService.SYNC_MESSAGE_QUESTION_RESULT,
                    new FastMap<>("matchQuestion", matchQuestion, "subscriberQuestionAnswers", sqas, "correctAnswerId", correctAnswerId), s, _socketIoSocket, _triggerService);
        });

//_logger.info(MessageFormat.format("end of scoring tiebreaker question on match. match question has been marked as: #{0}", matchQuestion.getMatchQuestionStatus()));
    }

    private void scoreQuestionAsRegular(
        Game game, Round round, Match match, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, boolean isQuestionTimedOut, List<Long> botsInGame,
        final String correctAnswerId, List<SubscriberQuestionAnswer> correctSqas, List<SubscriberQuestionAnswer> incorrectSqas)
    {
        //find if at least one of the players got the answer correct
        if (correctSqas != null && correctSqas.size() > 0) {
            if (correctSqas.size() == 1) {
                //only 1 person got the answer correct - was it due to only they got it correct, or everyone else timed out?
                correctSqas.get(0).setWon(true);
                if (incorrectSqas != null && incorrectSqas.size() > 0) {
                    correctSqas.get(0).setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_CORRECT);

                    if (game.isProductionGame() && !botsInGame.contains(correctSqas.get(0).getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_CORRECT);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: WON_CORRECT",
                                correctSqas.get(0).getSubscriberId(), correctSqas.get(0).getId()));
                    }

                } else {
                    correctSqas.get(0).setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIMEOUT);

                    if (game.isProductionGame() && !botsInGame.contains(correctSqas.get(0).getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIMEOUT);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: WON_TIMEOUT",
                                correctSqas.get(0).getSubscriberId(), correctSqas.get(0).getId()));
                    }
                }

            } else {
                //multiple correct answers. must determine winner based on time (will be the first row since the sqas collection is ordered on time already)
                correctSqas.get(0).setWon(true);
                correctSqas.get(0).setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIME);

                if (game.isProductionGame() && !botsInGame.contains(correctSqas.get(0).getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(0).getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIME);
                }

                if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                    SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                            "MME: sId: {0,number,#}, sqaId: {1}, answer determination: WON_TIME",
                            correctSqas.get(0).getSubscriberId(), correctSqas.get(0).getId()));
                }

                //all other correct answers still lost due to time
                for (int i = 1; i < correctSqas.size(); i++) {
                    correctSqas.get(i).setWon(false);
                    correctSqas.get(i).setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIME);

                    if (game.isProductionGame() && !botsInGame.contains(correctSqas.get(i).getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(i).getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                        _subscriberStatsHandler.incrementSubscriberStat(correctSqas.get(i).getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIME);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: LOST_TIME",
                                correctSqas.get(i).getSubscriberId(), correctSqas.get(i).getId()));
                    }
                }
            }

            //since someone actually won, mark that fact in the MatchQuestion object
            matchQuestion.setWonSubscriberId(correctSqas.get(0).getSubscriberId());
            matchQuestion.setDetermination(Match.MATCH_DETERMINATION.WINNER);

            if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                        "MME: sId: {0,number,#}, mquestionId: {1}, match determination: WINNER",
                        matchQuestion.getWonSubscriberId(), matchQuestion.getId()));
            }

        } else {
            //nobody got the answer correct, therefore we know the determination of all players not already timed out (LOST_INCORRECT)
            sqas.stream()
                .filter(sqa -> sqa.getDetermination() == SubscriberQuestionAnswer.ANSWER_DETERMINATION.UNKNOWN)
                .forEach(sqa -> {
                    sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);

                    if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: LOST_INCORRECT",
                                sqa.getSubscriberId(), sqa.getId()));
                    }
                });

            //did all players timeout? if so, there's a special determination for that (LOST_ALL_TIMEOUT)
            long timedoutPlayerAnswers =  sqas.stream().filter(a -> a.getSelectedAnswerId() == null).count();
            if (timedoutPlayerAnswers == sqas.size()) {
                sqas.stream().forEach(sqa -> {
                    sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_ALL_TIMEOUT);

                    if (game.isProductionGame() && !botsInGame.contains(sqa.getSubscriberId())) {
                        _subscriberStatsHandler.incrementSubscriberStat(sqa.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_ALL_TIMEOUT);
                    }

                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                                "MME: sId: {0,number,#}, sqaId: {1}, answer determination: LOST_ALL_TIMEOUT",
                                sqa.getSubscriberId(), sqa.getId()));
                    }
                });
            }
        }

        //if nobody won, mark that on the MatchQuestion (if there was a winner, it will have already been set to WINNER)
        if (matchQuestion.getDetermination() == null || matchQuestion.getDetermination() == Match.MATCH_DETERMINATION.UNKNOWN) {
            matchQuestion.setDetermination(Match.MATCH_DETERMINATION.NO_WINNER);

            if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                        "MME: sId: n/a, mquestionId: {0}, match determination: NO_WINNER",
                        matchQuestion.getId()));
            }
        }
        matchQuestion.setMatchQuestionStatus(Match.MATCH_STATUS.CLOSED);
        matchQuestion.setCompletedDate(new Date());
        _dao.updateMatchQuestion(matchQuestion);

        //send out "question_result" sync messages to all players
        sqas.stream().forEach(sqa -> {
            _dao.updateSubscriberQuestionAnswer(sqa); //TODO: this got a sql lock wait timeout
//_logger.debug("sending 'question_result' sync message to: " + sqa.getSubscriberId());
            Subscriber s = _identityService.getSubscriberById(sqa.getSubscriberId());
            enqueueSyncMessage(
                    _jsonMapper, _syncService, _logger,
                    sqa.getGameId(), ISnowyowlService.SYNC_MESSAGE_QUESTION_RESULT,
                    new FastMap<>("matchQuestion", matchQuestion, "subscriberQuestionAnswers", sqas, "correctAnswerId", correctAnswerId), s, _socketIoSocket, _triggerService);
        });

//_logger.info(MessageFormat.format("end of scoring regular question on match. match question has been marked as: #{0}", matchQuestion.getMatchQuestionStatus()));
    }

    @Override
    public void processMatch(Round round, Match match, List<MatchQuestion> matchQuestions, Long twitchSubscriberId)
    {
//_logger.debug(MessageFormat.format(
//    "processMatch, roundId: {0}, matchId: {1}, # of questions asked: {2}",
//    round.getId(), match.getId(), matchQuestions.size()));

        List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());

//_logger.debug(MessageFormat.format(
//    "matchPlayers. p1: {0,number,#}, p2: {1,number,#}",
//    matchPlayers.get(0).getSubscriberId(), matchPlayers.get(1).getSubscriberId()));

        long s1Id = matchPlayers.get(0).getSubscriberId();
        long s2Id = matchPlayers.get(1).getSubscriberId();

        int s1CorrectCount = 0;
        int s2CorrectCount = 0;
        int questionsAsked = matchQuestions.size();
        int maxQuestionThatCanBeAsked = round.getMaximumActivityCount() != null ? round.getMaximumActivityCount() : Integer.MAX_VALUE;
        int correctQuestionsToWin = round.getMinimumActivityToWinCount();
//_logger.debug(MessageFormat.format(
//    "maxQuestionThatCanBeAsked: {0}, correctQuestionsToWin: {1}",
//    maxQuestionThatCanBeAsked, correctQuestionsToWin));

        //step 1: find who won the most questions
        for (MatchQuestion q : matchQuestions) {
            Long winnerSubscriberId = q.getWonSubscriberId();
            if (winnerSubscriberId != null) {
                if (winnerSubscriberId == s1Id) {
                    s1CorrectCount++;
                } else {
                    s2CorrectCount++;
                }
            }
        }
//_logger.debug(MessageFormat.format(
//    "s1Correct: {0}, s2Correct: {1}",
//    s1CorrectCount, s2CorrectCount));

        boolean isThereAWinner;
        Long winnerSubscriberId;

        //step 2: did someone correctly answer enough question to win OR did someone win more than someone else AND we've asked the max # of questions
        if (
                (s1CorrectCount >= correctQuestionsToWin || s2CorrectCount >= correctQuestionsToWin) ||
                (questionsAsked >= maxQuestionThatCanBeAsked && s1CorrectCount != s2CorrectCount)
           ) {
            //System.out.println("PLAYER X WON; MATCH IS OVER")
            isThereAWinner = true;
            if (s1CorrectCount >= correctQuestionsToWin || s2CorrectCount >= correctQuestionsToWin) {
                winnerSubscriberId = s1CorrectCount >= correctQuestionsToWin ? s1Id : s2Id;
            } else {
                winnerSubscriberId = s1CorrectCount > s2CorrectCount ? s1Id : s2Id;
            }
//_logger.debug(MessageFormat.format(
//    "THERE IS A WINNER; MATCH OVER. winnerSubscriberId: {0,number,#}",
//    winnerSubscriberId));

            finishProcessMatch(round, match, matchQuestions, isThereAWinner, winnerSubscriberId, twitchSubscriberId);
            return;

        } else {
            isThereAWinner = false;
            winnerSubscriberId = null;
        }

        //step 3: can more questions be asked
        if (questionsAsked < maxQuestionThatCanBeAsked) {
            match.setMatchStatus(Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION);
            match.setMatchStatusSetAt(new Date());
            Date sendNextQuestionAt = new Date(round.getDurationBetweenActivitiesSeconds() * 1_000L + System.currentTimeMillis());
            match.setSendNextQuestionAt(sendNextQuestionAt);
            _shoutContestService.updateMatch(match);

//_logger.debug("match state set to: WAITING_FOR_NEXT_QUESTION");
            return;
        }

        //step 4: is this a tie breaker
        boolean isTieBreaker = _dao.isTieBreakerQuestion(match.getGameId(), match.getId());
        if (!isTieBreaker) {
            match.setMatchStatus(Match.MATCH_STATUS.WAITING_FOR_TIEBREAKER_QUESTION);
            match.setMatchStatusSetAt(new Date());
            long tieBreakerComingInMs = round.getDurationBetweenActivitiesSeconds() * 1_000L + _tiebreakerDelayMs;
            Date sendNextQuestionAt = new Date(tieBreakerComingInMs + System.currentTimeMillis());
            match.setSendNextQuestionAt(sendNextQuestionAt);
            _shoutContestService.updateMatch(match);
//_logger.debug("match state set to: WAITING_FOR_TIEBREAKER_QUESTION");

            //send a socket.io message indicating that a tiebreaker question will be coming in X time
            if (_socketIoSocket != null) {
                Map<String, Object> tiebreakerMap = new HashMap<>();
                tiebreakerMap.put("tieBreakerComingInMs", tieBreakerComingInMs);

                List<Long> botIds = _botEngine.getBotsForGame(round.getGameId());
                for (MatchPlayer mp : matchPlayers) {
                    //don't bother sending socket.io message to a bot
                    if (botIds.contains(mp.getSubscriberId())) continue;

                    Subscriber subscriber = _identityService.getSubscriberById(mp.getSubscriberId());

                    Map<String, Object> msg = new HashMap<>();
                    msg.put("recipient", subscriber.getEmailSha256Hash());
                    msg.put("message", tiebreakerMap);

                    ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
                    String message;
                    try {
                        message = _jsonMapper.writeValueAsString(msg);
                        SocketIoLogger.log(_triggerService, subscriber.getSubscriberId(), "send_tiebreaker", message, "SENDING");
                        _socketIoSocket.emit("send_tiebreaker", message);
                        SocketIoLogger.log(_triggerService, subscriber.getSubscriberId(), "send_tiebreaker", message, "SENT");
                    } catch (JsonProcessingException e) {
                        _logger.warn("unable to emit socket.io message", e);
                    }
                }
            }

            return;

        } else {
            //there is a tie breaker therefore someone has to have won
            isThereAWinner = true;
            winnerSubscriberId = _dao.getTieBreakerWinnerSubscriberId(match.getGameId(), match.getId());
//_logger.debug(MessageFormat.format(
//    "THERE IS A WINNER; MATCH OVER DUE TO TIEBREAKER. winnerSubscriberId: {0}",
//    winnerSubscriberId));
        }

        //close the match
        finishProcessMatch(round, match, matchQuestions, isThereAWinner, winnerSubscriberId, twitchSubscriberId);
    }

    private void finishProcessMatch(Round round, Match match, List<MatchQuestion> matchQuestions, boolean isThereAWinner, Long winnerSubscriberId, Long twitchSubscriberId)
    {
        List<MatchPlayer> matchPlayers = _shoutContestService.getMatchPlayersForMatch(match.getId());

        boolean doesMatchContainTwitchSubscriber = false;
        long opId = 0;
        for (MatchPlayer mp : matchPlayers) {
            if (twitchSubscriberId != null && mp.getSubscriberId() == twitchSubscriberId) {
                doesMatchContainTwitchSubscriber = true;
                break;
            }
        }
        if (doesMatchContainTwitchSubscriber) {
            for (MatchPlayer mp : matchPlayers) {
                if (mp.getSubscriberId() != twitchSubscriberId) {
                    opId = mp.getSubscriberId();
                    break;
                }
            }
        }

        //finalize each MatchPlayer object and update the database
        for (MatchPlayer mp : matchPlayers) {
            MatchPlayer.MATCH_PLAYER_DETERMINATION determination = winnerSubscriberId != null && mp.getSubscriberId() == winnerSubscriberId ?
                    MatchPlayer.MATCH_PLAYER_DETERMINATION.WON : MatchPlayer.MATCH_PLAYER_DETERMINATION.LOST;

            //if the MatchPlayer lost, see if they should be saved. If so, save them.
            if (round.getRoundType() == ROUND_TYPE.BRACKET && determination == MATCH_PLAYER_DETERMINATION.LOST) {
                _saveCounterLock.lock();

                try {
//                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//                    TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
//                    try {
                        //are there any saves left? if so, save them
                        GameStats gameStats = _dao.getGameStats(mp.getGameId());
                        Integer remainingSavePlayerCount = (gameStats == null) ? null : gameStats.getRemainingSavePlayerCount();
                        if (remainingSavePlayerCount != null && remainingSavePlayerCount > 0) {
                            determination = MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED;
                            _dao.setGameStats(new GameStats(mp.getGameId()).withRemainingSavePlayerCount(--remainingSavePlayerCount));
                        }

//                        _transactionManager.commit(txStatus);
//                        txStatus = null;
//
//                    } finally {
//                        if (txStatus != null) {
//                            _transactionManager.rollback(txStatus);
//                            txStatus = null;
//                        }
//                    }
                } finally {
                    _saveCounterLock.unlock();
                }
            }

            //update the determination
            mp.setDetermination(determination);

//            if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
//                SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
//                    "MME: sId: {0,number,#}, mPlayer: {1}, matchPlayer determination: {2}",
//                    mp.getSubscriberId(), mp.getId(), determination));
//            }
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format(
                    "MME: sId: {0,number,#}, mPlayer: {1}, matchPlayer determination: {2}",
                    mp.getSubscriberId(), mp.getId(), determination));
            }

            _shoutContestService.updateMatchPlayer(mp);

            //apply a scoring algorithm
            updatePlayerRank(round, mp);
        }

        //send a twitch socket.io message
        if (doesMatchContainTwitchSubscriber) {
            MatchPlayer.MATCH_PLAYER_DETERMINATION twitchDetermination = null;
            MatchPlayer.MATCH_PLAYER_DETERMINATION twitchOpponentDetermination = null;

            for (MatchPlayer mp : matchPlayers) {
                if (mp.getSubscriberId() == twitchSubscriberId) {
                    twitchDetermination = mp.getDetermination();
                } else {
                    twitchOpponentDetermination = mp.getDetermination();
                }
            }

            Map<String, Object> twitchMap = new FastMap<>(
                "type", "TWITCH_MATCH_OVER",
                "gameid", round.getGameId(),
                "roundId", round.getId(),
                "matchId", match.getId(),
                "subscriberId", twitchSubscriberId,
                "opponentId", opId,
                "twitchSubscriberDetermination", twitchDetermination,
                "twitchOpponentDetermination", twitchOpponentDetermination
            );

            if (_socketIoSocket != null) {
                try {
                    _socketIoSocket.emit("send_twitch_message", _jsonMapper.writeValueAsString(twitchMap));
                } catch (JsonProcessingException e) {
                    _logger.error("unable to emit send_twitch_message", e);
                }
            }

            //if the opponent won, switch the twitch followed subscriber
            if (twitchDetermination != MatchPlayer.MATCH_PLAYER_DETERMINATION.WON && twitchDetermination != MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED) {
                _dao.setGameStats(new GameStats(round.getGameId()).withTwitchConsoleFollowedSubscriberId(opId));
            }
        }

        //update the Match so it moves through the engine pipeline
        match.setCompleteDate(new Date());
        match.setActualActivityCount(matchQuestions.size());
        match.setDetermination(isThereAWinner ? Match.MATCH_DETERMINATION.WINNER : Match.MATCH_DETERMINATION.NO_WINNER);
        match.setWonSubscriberId(winnerSubscriberId);

        if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
            SnowyowlService.SUB_EVENT_LOGGER.debug(MessageFormat.format(
                    "MME: matchId: {0}, determination: {1}, winner sId: {2,number,#}",
                    match.getId(), isThereAWinner ? Match.MATCH_DETERMINATION.WINNER : Match.MATCH_DETERMINATION.NO_WINNER, winnerSubscriberId));
        }

//_logger.info(MessageFormat.format("changing match {0} status from OPEN to PROCESSING", match.getId()));
        match.setMatchStatus(Match.MATCH_STATUS.PROCESSING);
        match.setMatchStatusSetAt(new Date());
        _shoutContestService.updateMatch(match);

        //send the match_result
        for (MatchPlayer mp : matchPlayers) {
            long subscriberId = mp.getSubscriberId();
            String roundId = round.getId();
            MatchPlayer.MATCH_PLAYER_DETERMINATION determination = mp.getDetermination();

            enqueueSyncMessage(
                _jsonMapper, _syncService, _logger,
                round.getGameId(), ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT,
                new FastMap<> (
                        "subscriberId", subscriberId,
                        "roundId", roundId,
                        "determination", determination,
                        "roundType", round.getRoundType()
                ), _identityService.getSubscriberById(subscriberId), _socketIoSocket, _triggerService);
        }

        //clear the local cache
        _matchQuestionCache.remove(match.getId());

        if (round.getRoundType() == Round.ROUND_TYPE.POOL) {
            //run the RME for every pool match that completes
            _lock.lock();
            try {
//_logger.info(MessageFormat.format("removing match {0} from the processing queue", match.getId()));
                _mmeCommon.getProcessingIds().remove(new MMEProcess(match.getId(), null));
                _mmeCommon.saveState();
            } finally {
                _lock.unlock();
            }

            //this needs to happen AFTER the current transaction commits since causing RME to run is on a different thread and it's a race condition
            // who happens first: does this transaction commit first, or does the RME thread start and do a db query first? If it's the later, then
            // it won't find this match in the processing state, and it will skip it
            TransactionSideEffectManager.runAfterThisTransactionCommit(() -> ((RMEFixedRoundSingleLife)_roundManagementEngine).run() );

        } else {
            //BRACKET ROUND

//            //decrement the match counter (if one exists for this round - which is will if this is a bracket round)
//            AtomicInteger matchCounter = _numMatchesInRound.get(match.getRoundId());
//            int numMatchesRemaining = -1;
//            if (matchCounter != null) {
//                numMatchesRemaining = matchCounter.decrementAndGet();
//            }
//_logger.info("num matches remaining in round: " + numMatchesRemaining);

            int remainingPlayerCount;

            _saveCounterLock.lock();
//            DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//            TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
            try {
                //decrement remaining player count
                remainingPlayerCount = _dao.getGameStats(match.getGameId()).getRemainingPlayers();

                for (MatchPlayer mp : matchPlayers) {
                    if (mp.getDetermination() != MatchPlayer.MATCH_PLAYER_DETERMINATION.WON && mp.getDetermination() != MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED) {
                        remainingPlayerCount--;
                    }
                }

                //update the db
                _dao.setGameStats(new GameStats(match.getGameId()).withRemainingPlayers(remainingPlayerCount));

//                _transactionManager.commit(txStatus);
//                txStatus = null;

                //send socket.io message
                if (_socketIoSocket != null) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("count", remainingPlayerCount);
_logger.info("sending updated player count to socket.io: " + remainingPlayerCount);

                    try {
                        ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
                        String message = _jsonMapper.writeValueAsString(msg);
                        SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENDING");
                        _socketIoSocket.emit("send_playercount", message);
                        SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENT");
                    } catch (JsonProcessingException e) {
                        _logger.error("unable to convert map to json", e);
                    }
                }

            } catch (Throwable t) {
                _logger.error("unable to get/set remaining players and/or emit send_playercount socket.io message", t);

            } finally {
//                if (txStatus != null) {
//                    _transactionManager.rollback(txStatus);
//                    txStatus = null;
//                }

                _saveCounterLock.unlock();
            }
        }
    }

    //the core of this algorithm comes from Matt's prototype
    private void updatePlayerRank(Round round, MatchPlayer matchPlayer)
    {
        //grab each of the SubscribrerQuestionAnswer objects for this MatchPlayer
        List<SubscriberQuestionAnswer> sqas = _dao.getSubscriberQuestionAnswersForMatch(matchPlayer.getMatchId(), matchPlayer.getSubscriberId());

        //get the match# (1,2,3,etc) - and increment by 1 (null=0). the engine later on will increment this correctly for the next go-round
        RoundPlayer roundPlayer = _shoutContestService.getRoundPlayer(matchPlayer.getRoundPlayerId());
        int roundCount = roundPlayer.getPlayedMatchCount() == null ? 1 : roundPlayer.getPlayedMatchCount()+1;

        double correctValue = matchPlayer.getDetermination() == MatchPlayer.MATCH_PLAYER_DETERMINATION.WON ? 1d : 0d;

        //correctness
        if (roundPlayer.getSkillAnswerCorrectPct() == null) {
            //first time
            roundPlayer.setSkillAnswerCorrectPct(correctValue);
        } else {
            roundPlayer.setSkillAnswerCorrectPct(
                correctValue + (roundCount-1d) * roundPlayer.getSkillAnswerCorrectPct() / roundCount
            );
        }

        //get the average answer times
        double answerAverageMs = sqas.stream()
                .mapToLong(sqa -> sqa.getDurationMilliseconds() == null ? round.getPlayerMaximumDurationSeconds()*1000 : sqa.getDurationMilliseconds())
                .average().getAsDouble();

//        //this shouldn't happen, but it does. this is an error! it will result in an imaginary number (NaN) in the calculation below which uses Math.pow(x,y)
//        if (answerAverageMs < 0) {
//            StringBuilder buf = new StringBuilder();
//            buf.append("GOT A NEGATIVE VALUE FOR answerAverageMs!\n");
//            buf.append("\n\tsubscriberId: ").append(matchPlayer.getSubscriberId());
//            buf.append("\n\tmatchId: ").append(matchPlayer.getMatchId());
//            buf.append("\n\roundPlayerMaxDurationMs: ").append(round.getPlayerMaximumDurationSeconds()*1000);
//            buf.append("\n\tsqas:");
//            sqas.stream().forEach(sqa -> {
//                buf.append("\n\t\tsqaId: ").append(sqa.getId());
//                buf.append("\n\t\tdurationMs: ").append(sqa.getDurationMilliseconds());
//                buf.append("\n\t\t---");
//            });
//            _logger.error(buf.toString());
//        }

        //skill
        if (roundPlayer.getSkillAverageAnswerMs() == null) {
            //first time
            roundPlayer.setSkillAverageAnswerMs((long)answerAverageMs);
        } else {

            roundPlayer.setSkillAverageAnswerMs( (long) (
                (double)roundPlayer.getSkillAverageAnswerMs() + (roundCount-1d) * answerAverageMs / roundCount
            ) );
        }

        //rank
        double correctnessFactor = 1.016167d + (0.09643648d - 1.016167d) / (1d + Math.pow(roundPlayer.getSkillAnswerCorrectPct() / 0.5252644d, 5.274839d));
        double speedFactor = 0.03738587d + (0.9554179d - 0.03738587d) / (1d + Math.pow((double)roundPlayer.getSkillAverageAnswerMs() / 6.390203d, 4.892211d));
        roundPlayer.setSkill(100d * correctnessFactor * speedFactor);

//        try {
            _shoutContestService.updateRoundPlayer(roundPlayer);
//        } catch (Throwable t) {
//            StringBuilder buf = new StringBuilder();
//            buf.append("GOT AN EXCEPTION WHILE UPDATE ROUND PLAYER's RANK.\n");
//            buf.append("\n\tskillAnswerCorrectPct: " + roundPlayer.getSkillAnswerCorrectPct());
//            buf.append("\n\tskillAverageAnswerMs: " + roundPlayer.getSkillAverageAnswerMs());
//            buf.append("\n\tcorrectnessFactor: " + correctnessFactor);
//            buf.append("\n\tspeedFactor: " + speedFactor);
//            buf.append("\trank: " + roundPlayer.getRank());
//            _logger.error(buf.toString());
//            throw t;
//        }
        _currentRankCalculator.clear(round.getGameId());
    }

}

