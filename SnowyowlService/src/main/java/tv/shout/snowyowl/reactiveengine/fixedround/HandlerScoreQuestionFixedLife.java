package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerScoreQuestionFixedLife
extends BaseHandlerScoreQuestion
{
    private static Logger _logger = Logger.getLogger(HandlerScoreQuestionFixedLife.class);

    @Override
    //MessageProcessor
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case SCORE_QUESTION:
                String engineType = (String) ((Map<String, Object>)message.payload).get("gameEngineType");
                if (!engineType.equals(ISnowyowlService.GAME_ENGINE_TYPE_FixedRound_SingleLife)) return;

                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "gameEngineType", payload.get("gameEngineType"),
                        "matchPlayers", getMatchPlayersFromPayload(payload, "matchPlayers"),
                        "sqas", getSqasFromPayload(payload, "sqas")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerScoreQuestionFixedLife received SCORE_QUESTION\n{0}", JsonUtil.print(map)));
                }
                handleScoreQuestion(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleScoreQuestion(Message message)
    {
        Map<String, Object> data = getDataFromMessagePayload(message);
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) data.get("matchPlayers");
        List<SubscriberQuestionAnswer> sqas = (List<SubscriberQuestionAnswer>) data.get("sqas");
        Game game = (Game) data.get("game");
        Round round = (Round) data.get("round");
        Match match = (Match) data.get("match");
        String correctAnswerId = (String) data.get("correctAnswerId");
        List<Long> botsInGame = (List<Long>) data.get("botsInGame");
        List<Long> sponsorsInGame = (List<Long>) data.get("sponsorsInGame");
        List<MatchQuestion> matchQuestions = (List<MatchQuestion>) data.get("matchQuestions");
        Subscriber s1 = (Subscriber) data.get("s1");
        Subscriber s2 = (Subscriber) data.get("s2");
        boolean isTieBreakerQuestion = (Boolean) data.get("isTieBreakerQuestion");

        Map<Long, Subscriber> subscribers = new FastMap<>(s1.getSubscriberId(), s1, s2.getSubscriberId(), s2);

        MatchQuestion matchQuestion = matchQuestions.stream()
                .filter(mq -> mq.getQuestionId().equals(sqas.get(0).getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("unable to locate match question"));

        switch (match.getMatchStatus())
        {
            case OPEN:
            case WAITING_FOR_NEXT_QUESTION:
                scoreQuestionAsRegular(game, match, matchQuestion, matchPlayers, sqas, s1, s2, correctAnswerId, botsInGame, sponsorsInGame);
                break;

            case WAITING_FOR_TIEBREAKER_QUESTION:
                scoreQuestionAsTieBreaker(game, match, matchQuestion, matchPlayers, sqas, s1, s2, correctAnswerId, botsInGame, sponsorsInGame);
                break;

            default:
                //it's possible the match was cancelled (as part of a game cancel)
                _logger.warn(MessageFormat.format(
                    "found match in unexpected state while scoring question. ignoring. matchId: {0}, state: {1}",
                    match.getId(), match.getMatchStatus()));
                return;
        }

        processMatch(game, round, match, matchPlayers, matchQuestions, getTwitchSubscriberId(game.getId()), isTieBreakerQuestion, botsInGame, sponsorsInGame, subscribers);
    }

    private void scoreQuestionAsRegular(
            Game game, Match match, MatchQuestion matchQuestion, List<MatchPlayer> matchPlayers, List<SubscriberQuestionAnswer> sqas,
            Subscriber s1, Subscriber s2, String correctAnswerId, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
//if (_logger.isDebugEnabled()) {
//    _logger.debug(MessageFormat.format(
//            "\n\tsId: {0,number,#}, correct: {1}, timedOut: {2}\n\tsId: {3,number,#}, correct: {4}, timedOut: {5}",
//            s1.getSubscriberId(), sqa1Correct, sqa1TimedOut, s2.getSubscriberId(), sqa2Correct, sqa2TimedOut));
//}

        scoreQuestionAsRegularAndSetDeterminationAndSendSubscriberStatsAndCloseMatch(game, matchQuestion, sqas, correctAnswerId, botsInGame, sponsorsInGame);

        List<GamePlayer> gamePlayers = updateSqasAndUpdateMatchAndGetGamePlayers(game, matchQuestion, sqas);

        sendQuestionResult(game, matchQuestion, sqas, s1, s2, correctAnswerId, gamePlayers, botsInGame, sponsorsInGame);
    }

    //tiebreaker scoring rules:
    // if multiple got it correct: fastest correct score wins
    // else if only one got it correct: they win
    // else if nobody got it correct: faster answer wins
    // else (if everyone timed out || all were incorrect but tied for time): randomly pick one
    private void scoreQuestionAsTieBreaker(
            Game game, Match match, MatchQuestion matchQuestion, List<MatchPlayer> matchPlayers, List<SubscriberQuestionAnswer> sqas,
            Subscriber s1, Subscriber s2, String correctAnswerId, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
if (_logger.isDebugEnabled()) {
    _logger.debug("scoring tiebreaker question...");
}
        //shuffle the sqas so that when we pick sqas[0] as the winner in the case of a tie, it will be randomized
        Collections.shuffle(sqas);

        SubscriberQuestionAnswer sqa1 = sqas.get(0);
        SubscriberQuestionAnswer sqa2 = sqas.get(1);

        boolean sqa1TimedOut = sqa1.getDetermination() != null && sqa1.getDetermination() == SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT;
        boolean sqa2TimedOut = sqa2.getDetermination() != null && sqa2.getDetermination() == SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT;
        boolean sqa1Correct = !sqa1TimedOut && sqa1.getSelectedAnswerId().equals(correctAnswerId);
        boolean sqa2Correct = !sqa2TimedOut && sqa2.getSelectedAnswerId().equals(correctAnswerId);
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
            "\n\tsId: {0,number,#}, correct: {1}, timedOut: {2}\n\tsId: {3,number,#}, correct: {4}, timedOut: {5}",
            s1.getSubscriberId(), sqa1Correct, sqa1TimedOut, s2.getSubscriberId(), sqa2Correct, sqa2TimedOut));
}

        long winnerSubscriberId;

        if (sqa1TimedOut && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_ALL_TIMEOUT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_ALL_TIMEOUT);
            sqa1.setWon(true);
            winnerSubscriberId = sqa1.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_ALL_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_ALL_TIMEOUT);
            }

        } else if (!sqa1Correct && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            //sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set
            sqa1.setWon(true);
            winnerSubscriberId = sqa1.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }

        } else if (sqa1Correct && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIMEOUT);
            //sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set
            sqa1.setWon(true);
            winnerSubscriberId = sqa1.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }

        } else if (sqa1TimedOut && !sqa2Correct) {
            //sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            sqa2.setWon(true);
            winnerSubscriberId = sqa2.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }

        } else if (!sqa1Correct && !sqa2Correct) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);

            //faster time wins
            if (sqa1.getDurationMilliseconds() <= sqa2.getDurationMilliseconds()) {
                sqa1.setWon(true);
                winnerSubscriberId = sqa1.getSubscriberId();
            } else {
                sqa2.setWon(true);
                winnerSubscriberId = sqa2.getSubscriberId();
            }

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }

        } else if (sqa1Correct && !sqa2Correct) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_CORRECT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            sqa1.setWon(true);
            winnerSubscriberId = sqa1.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_CORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }

        } else if (sqa1TimedOut && sqa2Correct) {
            //sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIMEOUT);
            sqa2.setWon(true);
            winnerSubscriberId = sqa2.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIMEOUT);
            }

        } else if (!sqa1Correct && sqa2Correct) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_CORRECT);
            sqa2.setWon(true);
            winnerSubscriberId = sqa2.getSubscriberId();

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_CORRECT);
            }

        } else {
            //both answered correctly; use time as the determining factor
            //in the extremely unlikely (but still possible) event that there's a tie on time, just use sqa1
            if (sqa1.getDurationMilliseconds() <= sqa2.getDurationMilliseconds()) {
                sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIME);
                sqa1.setWon(true);
                sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIME);
                winnerSubscriberId = sqa1.getSubscriberId();

                if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIME);
                }
                if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIME);
                }

            } else {
                sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIME);
                sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIME);
                sqa2.setWon(true);
                winnerSubscriberId = sqa2.getSubscriberId();

                if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIME);
                }
                if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                    _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.QUESTIONS_CORRECT, 1);
                    _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_WON_TIME);
                }
            }
        }

        //close out the match question
        matchQuestion.setMatchQuestionStatus(Match.MATCH_STATUS.CLOSED);
        matchQuestion.setCompletedDate(new Date());
        matchQuestion.setWonSubscriberId(winnerSubscriberId);
        matchQuestion.setDetermination(Match.MATCH_DETERMINATION.WINNER);

        List<GamePlayer> gamePlayers = updateSqasAndUpdateMatchAndGetGamePlayers(game, matchQuestion, sqas);

        sendQuestionResult(game, matchQuestion, sqas, s1, s2, correctAnswerId, gamePlayers, botsInGame, sponsorsInGame);
    }

    //once a question has been scored, this will determine if more questions need to be asked (and if so, what type: regular or tie-breaker), or if the match is over, and take appropriate action
    private void processMatch(Game game, Round round, Match match, List<MatchPlayer> matchPlayers, List<MatchQuestion> matchQuestions, Long twitchSubscriberId, boolean isTieBreakerQuestion,
            List<Long> botsInGame, List<Long> sponsorsInGame, Map<Long, Subscriber> subscribers)
    {
        long s1Id = matchPlayers.get(0).getSubscriberId();
        long s2Id = matchPlayers.get(1).getSubscriberId();

        int s1CorrectCount = 0;
        int s2CorrectCount = 0;
        int questionsAsked = matchQuestions.size();
        int maxQuestionThatCanBeAsked = round.getMaximumActivityCount() != null ? round.getMaximumActivityCount() : Integer.MAX_VALUE;
        int correctQuestionsToWin = round.getMinimumActivityToWinCount();

        //boolean isThereAWinner;
        Long winnerSubscriberId;

        //step 1: find who won the most questions
        for (MatchQuestion q : matchQuestions) {
            Long winnerSubId = q.getWonSubscriberId();
            if (winnerSubId != null) {
                if (winnerSubId == s1Id) {
                    s1CorrectCount++;
                } else {
                    s2CorrectCount++;
                }
            }
        }

        //step 2: did someone correctly answer enough question to win OR did someone win more than someone else AND we've asked the max # of questions
        if (
                (s1CorrectCount >= correctQuestionsToWin || s2CorrectCount >= correctQuestionsToWin) ||
                (questionsAsked >= maxQuestionThatCanBeAsked && s1CorrectCount != s2CorrectCount)
           ) {
            //isThereAWinner = true;
            if (s1CorrectCount >= correctQuestionsToWin || s2CorrectCount >= correctQuestionsToWin) {
                winnerSubscriberId = s1CorrectCount >= correctQuestionsToWin ? s1Id : s2Id;
            } else {
                winnerSubscriberId = s1CorrectCount > s2CorrectCount ? s1Id : s2Id;
            }
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("THERE IS A WINNER; MATCH OVER. winnerSubscriberId: {0,number,#}", winnerSubscriberId));
}

            finishProcessMatch(game, round, match, matchPlayers, matchQuestions, /*isThereAWinner, */winnerSubscriberId, twitchSubscriberId, botsInGame, sponsorsInGame);
            return;

        } else {
            //isThereAWinner = false;
            winnerSubscriberId = null;
        }

        //step 3: can more questions be asked
        if (questionsAsked < maxQuestionThatCanBeAsked) {
            sendNextQuestion(game, round, match, matchPlayers, botsInGame, sponsorsInGame, subscribers);
            return;
        }

        if (isTieBreakerQuestion) {
            //someone has to have won
            //isThereAWinner = true;
            winnerSubscriberId = (Long) wrapInTransaction((x) -> {
                return _dao.getTieBreakerWinnerSubscriberId(match.getGameId(), match.getId());
            }, null);

        } else {
            match.setMatchStatus(Match.MATCH_STATUS.WAITING_FOR_TIEBREAKER_QUESTION);
            match.setMatchStatusSetAt(new Date());
            long tieBreakerComingInMs = round.getDurationBetweenActivitiesSeconds() * 1_000L + _tiebreakerDelayMs;
            Date sendNextQuestionAt = new Date(tieBreakerComingInMs + System.currentTimeMillis());
            match.setSendNextQuestionAt(sendNextQuestionAt);
            wrapInTransaction((x) -> {
                _shoutContestService.updateMatch(match);
                return null;
            }, null);

            //send a socket.io message indicating that a tiebreaker question will be coming in X time
            if (_socket != null) {
                Map<String, Object> tiebreakerMap = new HashMap<>();
                tiebreakerMap.put("tieBreakerComingInMs", tieBreakerComingInMs);

                @SuppressWarnings("unchecked")
                List<Long> botIds = (List<Long>) wrapInTransaction((x) -> {
                    return _botEngine.getBotsForGame(round.getGameId());
                }, null);

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
                        _socket.emit("send_tiebreaker", message);
                        SocketIoLogger.log(_triggerService, subscriber.getSubscriberId(), "send_tiebreaker", message, "SENT");
                    } catch (JsonProcessingException e) {
                        _logger.warn("unable to emit socket.io message", e);
                    }
                }
            }

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("no match winner yet. sending tie breaker question in {0} ms", round.getDurationBetweenActivitiesSeconds() * 1_000L));
}

            //send message: SEND_QUESTION_AT
            DelayedMessage msg = HandlerSendQuestion.getDelayedMessage(tieBreakerComingInMs, game, round, match, matchPlayers, subscribers, botsInGame, sponsorsInGame, true);
            if (_busLogger.isDebugEnabled()) {
                _busLogger.debug("HandlerScoreQuestionFixedLife sending delayed message: " + msg.type);
            }
            _messageBus.sendDelayedMessage(msg);
            return;
        }

        //if it got to here, someone has won and the match is over
        finishProcessMatch(game, round, match, matchPlayers, matchQuestions, /*isThereAWinner, */winnerSubscriberId, twitchSubscriberId, botsInGame, sponsorsInGame);
    }

}
