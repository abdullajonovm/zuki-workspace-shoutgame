package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.GamePlayer;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.MatchPlayer.MATCH_PLAYER_DETERMINATION;
import tv.shout.sc.domain.Round;
import tv.shout.sc.domain.Round.ROUND_TYPE;
import tv.shout.sc.domain.RoundPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.common.SocketIoLogger;
import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberStats;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.QuestionSupplier;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;
import tv.shout.util.MaxSizeHashMap;

public abstract class BaseHandlerScoreQuestion
extends BaseHandler
implements SyncMessageSender
{
    private static Logger _logger = Logger.getLogger(BaseHandlerScoreQuestion.class);

    protected static final String SCORE_QUESTION = "SCORE_QUESTION";

    @Value("${sm.substats.WON_CORRECT}")
    protected int _cumulativeScore_WON_CORRECT;

    @Value("${sm.substats.WON_TIME}")
    protected int _cumulativeScore_WON_TIME;

    @Value("${sm.substats.WON_TIMEOUT}")
    protected int _cumulativeScore_WON_TIMEOUT;

    @Value("${sm.substats.LOST_TIME}")
    protected int _cumulativeScore_LOST_TIME;

    @Value("${sm.substats.LOST_INCORRECT}")
    protected int _cumulativeScore_LOST_INCORRECT;

    @Value("${sm.substats.LOST_TIMEOUT}")
    protected int _cumulativeScore_LOST_TIMEOUT;

    @Value("${sm.substats.LOST_ALL_TIMEOUT}")
    protected int _cumulativeScore_LOST_ALL_TIMEOUT;

    @Value("${mme.tiebreaker.delay.ms:0}")
    protected long _tiebreakerDelayMs;

    @Autowired
    protected SubscriberStatsHandler _subscriberStatsHandler;

    @Autowired
    protected CurrentRankCalculator _currentRankCalculator;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IIdentityService _identityService;

    @Autowired
    protected ISyncService _syncService;

    @Autowired
    protected ITriggerService _triggerService;

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    @Autowired
    protected QuestionSupplier _questionSupplier;

    @Autowired
    protected CommonBusinessLogic _commonBusinessLogic;

    private Lock _saveCounterLock = new ReentrantLock();

    public static Message getScoreQuestionMessage(String gameEngineType, List<MatchPlayer> matchPlayers, List<SubscriberQuestionAnswer> sqas)
    {
        Map<String, Object> payload = new FastMap<>(
            "gameEngineType", gameEngineType,
            "sqas", sqas,
            "matchPlayers", matchPlayers
        );

        return new Message(SCORE_QUESTION, payload);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDataFromMessagePayload(Message message)
    {
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) payload.get("matchPlayers");
        List<SubscriberQuestionAnswer> sqas = (List<SubscriberQuestionAnswer>) payload.get("sqas");

        //grab everything that will be needed later on
        Object[] obj = (Object[]) wrapInTransaction((x) -> {
            Game game = _shoutContestService.getGame(sqas.get(0).getGameId());
            Round round = _shoutContestService.getRound(sqas.get(0).getRoundId());
            Match match = _shoutContestService.getMatch(sqas.get(0).getMatchId());
            String correctAnswerId = getCorretAnswerId(sqas.get(0).getQuestionId());
            List<Long> botsInGame = _botEngine.getBotsForGame(sqas.get(0).getGameId());
            List<Long> sponsorsInGame = _sponsorEngine.getSponsorsForGame(sqas.get(0).getGameId());
            List<MatchQuestion> matchQuestions = _dao.getMatchQuestionsForMatch(sqas.get(0).getMatchId());
            Subscriber s1 = _identityService.getSubscriberById(sqas.get(0).getSubscriberId());
            Subscriber s2 = _identityService.getSubscriberById(sqas.get(1).getSubscriberId());
            boolean isTieBreakerQuestion = _dao.isTieBreakerQuestion(match.getGameId(), match.getId());

            return new Object[] {game, round, match, correctAnswerId, botsInGame, sponsorsInGame, matchQuestions, s1, s2, isTieBreakerQuestion};
        }, null);

        return new FastMap<>(
            "matchPlayers", matchPlayers,
            "sqas", sqas,
            "game", obj[0],
            "round", obj[1],
            "match", obj[2],
            "correctAnswerId", obj[3],
            "botsInGame", obj[4],
            "sponsorsInGame", obj[5],
            "matchQuestions", obj[6],
            "s1", obj[7],
            "s2", obj[8],
            "isTieBreakerQuestion", obj[9]
        );
    }

    private String getCorretAnswerId(String questionId)
    {
        //check the cache first
        String cai = _questionSupplier.getQuestionCorrectAnswerMap().get(questionId);
        if (cai == null) {
            QuestionAnswer correctQuestionAnswer = _dao.getQuestionAnswersForQuestion(questionId).stream()
                .filter(a -> a.getCorrect())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("attempting to score question that does not have a correct answer!"));
            cai = correctQuestionAnswer.getId();

            //add to questionSupplier cache
            _questionSupplier.getQuestionCorrectAnswerMap().put(questionId, cai);
        }
        return cai;
    }

    protected List<GamePlayer> updateSqasAndUpdateMatchAndGetGamePlayers(Game game, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas)
    {
        @SuppressWarnings("unchecked")
        List<GamePlayer> gamePlayers = (List<GamePlayer>) wrapInTransaction((x) -> {
            SubscriberQuestionAnswer sqa1 = sqas.get(0);
            SubscriberQuestionAnswer sqa2 = sqas.get(1);

            _dao.updateSubscriberQuestionAnswer(sqa1);
            _dao.updateSubscriberQuestionAnswer(sqa2);

            _dao.updateMatchQuestion(matchQuestion);

            GamePlayer gp1 = _shoutContestService.getGamePlayer(game.getId(), sqa1.getSubscriberId());
            GamePlayer gp2 = _shoutContestService.getGamePlayer(game.getId(), sqa2.getSubscriberId());

            return Arrays.asList(gp1, gp2);
        }, null);

        return gamePlayers;
    }

    protected void scoreQuestionAsRegularAndSetDeterminationAndSendSubscriberStatsAndCloseMatch(
            Game game, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas, String correctAnswerId, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
        SubscriberQuestionAnswer sqa1 = sqas.get(0);
        SubscriberQuestionAnswer sqa2 = sqas.get(1);

        boolean sqa1TimedOut = sqa1.getDetermination() != null && sqa1.getDetermination() == SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT;
        boolean sqa2TimedOut = sqa2.getDetermination() != null && sqa2.getDetermination() == SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT;
        boolean sqa1Correct = !sqa1TimedOut && sqa1.getSelectedAnswerId().equals(correctAnswerId);
        boolean sqa2Correct = !sqa2TimedOut && sqa2.getSelectedAnswerId().equals(correctAnswerId);

        Long winnerSubscriberId = null;

        if (sqa1TimedOut && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_ALL_TIMEOUT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_ALL_TIMEOUT);

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_ALL_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_ALL_TIMEOUT);
            }

        } else if (!sqa1Correct && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            //sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }

        } else if (sqa1Correct && sqa2TimedOut) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_TIMEOUT);
            sqa1.setWon(true);
            //sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT); //already set
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

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_TIMEOUT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }

        } else if (!sqa1Correct && !sqa2Correct) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);

            if (game.isProductionGame() && !botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa1.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }
            if (game.isProductionGame() && !botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(sqa2.getSubscriberId(), SubscriberStats.STATS_TYPE.CUMULATIVE_QUESTION_SCORE, _cumulativeScore_LOST_INCORRECT);
            }

        } else if (sqa1Correct && !sqa2Correct) {
            sqa1.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.WON_CORRECT);
            sqa1.setWon(true);
            sqa2.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_INCORRECT);
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
        matchQuestion.setDetermination(winnerSubscriberId != null ? Match.MATCH_DETERMINATION.WINNER : Match.MATCH_DETERMINATION.NO_WINNER);
    }

    protected void sendQuestionResult(
        Game game, MatchQuestion matchQuestion, List<SubscriberQuestionAnswer> sqas,
        Subscriber s1, Subscriber s2, String correctAnswerId, List<GamePlayer> gamePlayers, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
        SubscriberQuestionAnswer sqa1 = sqas.get(0);
        SubscriberQuestionAnswer sqa2 = sqas.get(1);

        //grab the relevant data about the total/remaining lives for each player
        List<Map<String, Object>> lives = new ArrayList<>();
        lives.add(new FastMap<>("subscriberId", sqa1.getSubscriberId(), "totalLives", gamePlayers.get(0).getTotalLives(), "remainingLives", gamePlayers.get(0).getCountdownToElimination()));
        lives.add(new FastMap<>("subscriberId",   sqa2.getSubscriberId(), "totalLives", gamePlayers.get(1).getTotalLives(), "remainingLives", gamePlayers.get(1).getCountdownToElimination()));

        //send question_result sync message
        if (!botsInGame.contains(sqa1.getSubscriberId()) && !sponsorsInGame.contains(sqa1.getSubscriberId())) {
            enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_QUESTION_RESULT,
                    new FastMap<>(
                            "matchQuestion", matchQuestion,
                            "subscriberQuestionAnswers", sqas,
                            "correctAnswerId", correctAnswerId,
                            "lives", lives), s1, _socket, _triggerService);
        }
        if (!botsInGame.contains(sqa2.getSubscriberId()) && !sponsorsInGame.contains(sqa2.getSubscriberId())) {
            enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    game.getId(), ISnowyowlService.SYNC_MESSAGE_QUESTION_RESULT,
                    new FastMap<>(
                            "matchQuestion", matchQuestion,
                            "subscriberQuestionAnswers", sqas,
                            "correctAnswerId", correctAnswerId,
                            "lives", lives), s2, _socket, _triggerService);
        }

    }

    protected void sendNextQuestion(
            Game game, Round round, Match match, List<MatchPlayer> matchPlayers, List<Long> botsInGame, List<Long> sponsorsInGame, Map<Long, Subscriber> subscribers)
        {
            match.setMatchStatus(Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION);
            match.setMatchStatusSetAt(new Date());
            Date sendNextQuestionAt = new Date(round.getDurationBetweenActivitiesSeconds() * 1_000L + System.currentTimeMillis());
            match.setSendNextQuestionAt(sendNextQuestionAt);
            wrapInTransaction((x) -> {
                _shoutContestService.updateMatch(match);
                return null;
            }, null);

    if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("no match winner yet. sending next question in {0} ms", round.getDurationBetweenActivitiesSeconds() * 1_000L));
    }

            //send message: SEND_QUESTION_AT
            if (_busLogger.isDebugEnabled()) {
                _busLogger.debug("HandlerScoreQuestion sending delayed message SEND_QUESTION_AT");
            }
            _messageBus.sendDelayedMessage(
                HandlerSendQuestion.getDelayedMessage(round.getDurationBetweenActivitiesSeconds() * 1_000L, game, round, match, matchPlayers, subscribers, botsInGame, sponsorsInGame, false)
            );
        }

    //the match is over. score it and determine where to go next
    protected void finishProcessMatch(
            Game game, Round round, Match match, List<MatchPlayer> matchPlayers, List<MatchQuestion> matchQuestions, /*boolean isThereAWinner, */Long winnerSubscriberId, Long twitchSubscriberId,
            List<Long> botsInGame, List<Long> sponsorsInGame)
    {
        //finalize each MatchPlayer object and update the database
        wrapInTransaction((x) -> {
            for (MatchPlayer mp : matchPlayers) {
                MatchPlayer.MATCH_PLAYER_DETERMINATION determination = winnerSubscriberId != null && mp.getSubscriberId() == winnerSubscriberId ?
                        MatchPlayer.MATCH_PLAYER_DETERMINATION.WON : MatchPlayer.MATCH_PLAYER_DETERMINATION.LOST;

                //if the MatchPlayer lost, see if they should be saved. If so, save them.
                if (round.getRoundType() == ROUND_TYPE.BRACKET && determination == MATCH_PLAYER_DETERMINATION.LOST) {
                    _saveCounterLock.lock();
                    try {
                        //are there any saves left? if so, save them
                        GameStats gameStats = _gameStatsHandler.getGameStats(mp.getGameId());
                        Integer remainingSavePlayerCount = (gameStats == null) ? null : gameStats.getRemainingSavePlayerCount();
                        if (remainingSavePlayerCount != null && remainingSavePlayerCount > 0) {
                            determination = MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED;
                            _gameStatsHandler.setGameStats(new GameStats(mp.getGameId()).withRemainingSavePlayerCount(--remainingSavePlayerCount));
                        }
                    } finally {
                        _saveCounterLock.unlock();
                    }
                }

                //update the determination
                mp.setDetermination(determination);

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
        "MME: sId: {0,number,#}, mPlayer: {1}, matchPlayer determination: {2}",
        mp.getSubscriberId(), mp.getId(), determination));
}

                _shoutContestService.updateMatchPlayer(mp);

                //apply a scoring algorithm
                updatePlayerRank(round, mp);
            }

            sendTwitchUpdate(round, match, twitchSubscriberId, matchPlayers);

            //update the Match
            match.setCompleteDate(new Date());
            match.setActualActivityCount(matchQuestions.size());
            match.setDetermination(Match.MATCH_DETERMINATION.WINNER);
            match.setWonSubscriberId(winnerSubscriberId);
            match.setMatchStatus(Match.MATCH_STATUS.PROCESSING);
            match.setMatchStatusSetAt(new Date());
_logger.info(MessageFormat.format("setting match {0} status to PROCESSING", match.getId()));
            _shoutContestService.updateMatch(match);

            //send the match_result
            for (MatchPlayer mp : matchPlayers) {
                if (botsInGame.contains(mp.getSubscriberId()) || sponsorsInGame.contains(mp.getSubscriberId())) {
                    continue;
                }

                long subscriberId = mp.getSubscriberId();
                String roundId = round.getId();
                MatchPlayer.MATCH_PLAYER_DETERMINATION determination = mp.getDetermination();

                enqueueSyncMessage(
                    JsonUtil.getObjectMapper(), _syncService, _logger,
                    round.getGameId(), ISnowyowlService.SYNC_MESSAGE_MATCH_RESULT,
                    new FastMap<> (
                            "subscriberId", subscriberId,
                            "roundId", roundId,
                            "determination", determination,
                            "roundType", round.getRoundType()
                    ), _identityService.getSubscriberById(subscriberId), _socket, _triggerService);
            }

            return null;
        }, null);

        //in a separate transaction because the above transaction needs to complete first in case multiple messages come in simultaneously
        // since if they do the state of the match (processing) will not be properly visible to everyone
        wrapInTransaction((x) -> {
            if (round.getRoundType() == Round.ROUND_TYPE.POOL) {
                finishPoolPlay(game, round, match, matchPlayers, botsInGame, sponsorsInGame);

            } else {
                finishBracketPlay(game, round, match, matchPlayers, botsInGame, sponsorsInGame);
            }

            return null;
        }, null);
    }

    //wrapped in a transaction
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
//if (_logger.isDebugEnabled()) {
//    _logger.debug(MessageFormat.format("subscriber {0,number,#} skill set to: {1}", roundPlayer.getSubscriberId(), roundPlayer.getSkill() ));
//}

        _shoutContestService.updateRoundPlayer(roundPlayer);
        _currentRankCalculator.clear(round.getGameId());
    }

    protected void sendTwitchUpdate(Round round, Match match, Long twitchSubscriberId, List<MatchPlayer> matchPlayers)
    {
        if (_socket != null) {
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

                if (_socket != null) {
                    try {
                        _socket.emit("send_twitch_message", JsonUtil.getObjectMapper().writeValueAsString(twitchMap));
                    } catch (JsonProcessingException e) {
                        _logger.error("unable to emit send_twitch_message", e);
                    }
                }

                //if the opponent won, switch the twitch followed subscriber
                if (twitchDetermination != MatchPlayer.MATCH_PLAYER_DETERMINATION.WON && twitchDetermination != MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED) {
                    _gameStatsHandler.setGameStats(new GameStats(round.getGameId()).withTwitchConsoleFollowedSubscriberId(opId));
                }
            }
        }
    }

    //wrapped in a transaction
    private void finishPoolPlay(Game game, Round round, Match match, List<MatchPlayer> matchPlayers, List<Long> botsInGame, List<Long> sponsorsInGame)
    {
        for (MatchPlayer matchPlayer : matchPlayers) {
            RoundPlayer roundPlayer = _shoutContestService.getRoundPlayer(matchPlayer.getRoundPlayerId());
            GamePlayer gamePlayer =  _shoutContestService.getGamePlayer(game.getId(), roundPlayer.getSubscriberId());

            if (game.isProductionGame() && !botsInGame.contains(gamePlayer.getSubscriberId()) && !sponsorsInGame.contains(gamePlayer.getSubscriberId())) {
                _subscriberStatsHandler.incrementSubscriberStat(gamePlayer.getSubscriberId(), SubscriberStats.STATS_TYPE.POOL_ROUNDS_PLAYED, 1);
            }

            switch (matchPlayer.getDetermination())
            {
                case WON: {
                    roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.WON);

                    //there is always another round to play (pool play is always followed by bracket play)
                    int currentRoundSequence = round.getRoundSequence();
                    Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                    gamePlayer.setLastRoundId(round.getId());
                    gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                }
                break;

                case LOST: {
                    roundPlayer.setDetermination(RoundPlayer.ROUND_PLAYER_DETERMINATION.LOST);

                    //there is always another round to play (pool play is always followed by bracket play)
                    int currentRoundSequence = round.getRoundSequence();
                    Round nextRoundInSequence = _shoutContestService.getRoundForGameAndSequence(game.getId(), currentRoundSequence + 1);
                    gamePlayer.setLastRoundId(round.getId());
                    gamePlayer.setNextRoundId(nextRoundInSequence.getId());
                }
                break;

                default:
                    throw new IllegalStateException("invalid determination: " + matchPlayer.getDetermination());
            }

            //update the player records
            _shoutContestService.updateRoundPlayer(roundPlayer);
            _shoutContestService.updateGamePlayer(gamePlayer);
            _currentRankCalculator.clear(game.getId());

            if (botsInGame.contains(gamePlayer.getSubscriberId())) {
                _commonBusinessLogic.removeFromRound(game.getId(), round, roundPlayer);
            }
        }

        //close the match
        match.setMatchStatus(Match.MATCH_STATUS.CLOSED);
        match.setMatchStatusSetAt(new Date());
        _shoutContestService.updateMatch(match);
if (_logger.isDebugEnabled()) {
    _logger.debug("POOL match is complete");
}
    }

    //wrapped in a transaction
    private void finishBracketPlay(Game game, Round round, Match match, List<MatchPlayer> matchPlayers, List<Long> botIds, List<Long> sponsorIds)
    {
if (_logger.isDebugEnabled()) {
    _logger.debug("BRACKET match is complete");
}
        _saveCounterLock.lock();
        try {
            //decrement remaining player count
            int remainingPlayerCount = _gameStatsHandler.getGameStats(match.getGameId()).getRemainingPlayers();

            for (MatchPlayer mp : matchPlayers) {
                if (mp.getDetermination() != MatchPlayer.MATCH_PLAYER_DETERMINATION.WON && mp.getDetermination() != MatchPlayer.MATCH_PLAYER_DETERMINATION.SAVED) {
                    remainingPlayerCount--;
                }
            }

            //update the db
            _gameStatsHandler.setGameStats(new GameStats(match.getGameId()).withRemainingPlayers(remainingPlayerCount));

            sendPlayerCountSocketIoMessage(remainingPlayerCount);

        } catch (Throwable t) {
            _logger.error("unable to get/set remaining players and/or emit send_playercount socket.io message", t);

        } finally {
            _saveCounterLock.unlock();
        }

        //find how many matches are still outstanding for this round
        int matchesNotYetProcessingForRound;
        List<Match> matchesNotYetProcessingForRoundList =
            _shoutContestService.getMatchesByRoundAndStatus(
                round.getId(),
                Match.MATCH_STATUS.NEW, Match.MATCH_STATUS.OPEN, Match.MATCH_STATUS.WAITING_FOR_NEXT_QUESTION, Match.MATCH_STATUS.WAITING_FOR_TIEBREAKER_QUESTION);
        matchesNotYetProcessingForRound = matchesNotYetProcessingForRoundList.size();
_logger.info(MessageFormat.format("# matches not yet processing: {0}", matchesNotYetProcessingForRound));

        //publish how many matches are still outstanding for this round
        Message msg1 = HandlerDocPublisher.getBracketOutstandingMatchCountMessage(round, matchesNotYetProcessingForRound);
        if (_busLogger.isDebugEnabled()) {
            _busLogger.debug("HandlerScoreQuestion sending message " + msg1.type);
        }
        _messageBus.sendMessage(msg1);

        //FUTURE: this is where the partial/incremental scoring/payout algorithm that bruce wants would be applied

        if (matchesNotYetProcessingForRound == 0) {
            //all matches are complete. score the round, unless it's already been scored
            // this is possible due to the concurrent nature of the message bus. multiple matches may have been outstanding, and all scored, thus dropping the matchesNotYetProcessingForRound to 0,
            // but the messages are still filtering in via the bus. but this call must only happen once per bracket round. thus the extra check to see if it's already been done
            _bracketMatchProcessedLock.lock();
            try {
                if (!_bracketMatchProcessedMap.containsKey(round.getId())) {
                    _bracketMatchProcessedMap.put(round.getId(), true);

_logger.info("sending SCORE_BRACKET_ROUND: message...");
                    Message msg2 = BaseHandlerScoreBracket.getScoreBracketRoundMessage(game, round, botIds, sponsorIds);
                    if (_busLogger.isDebugEnabled()) {
                        _busLogger.debug("HandlerScoreQuestion sending message " + msg2.type);
                    }
                    _messageBus.sendMessage(msg2);

                }
else _logger.info("NOT sending SCORE_BRACKET_ROUND: message already sent");
            } finally {
                _bracketMatchProcessedLock.unlock();
            }
        }
else _logger.info(MessageFormat.format("not yet time to score bracket round. there are {0} outstanding matches", matchesNotYetProcessingForRound));
    }

    private Lock _bracketMatchProcessedLock = new ReentrantLock();
    private Map<String, Boolean> _bracketMatchProcessedMap = new MaxSizeHashMap<String, Boolean>().withMaxSize(100);

    protected void sendPlayerCountSocketIoMessage(int remainingPlayerCount)
    {
        //send socket.io message
        if (_socket != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("count", remainingPlayerCount);
_logger.info("sending updated player count to socket.io: " + remainingPlayerCount);

            try {
                ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();
                String message = _jsonMapper.writeValueAsString(msg);
                SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENDING");
                _socket.emit("send_playercount", message);
                SocketIoLogger.log(_triggerService, null, "send_playercount", message, "SENT");
            } catch (JsonProcessingException e) {
                _logger.error("unable to convert map to json", e);
            }
        }
    }

}
