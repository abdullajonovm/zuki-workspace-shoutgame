package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meinc.identity.domain.Subscriber;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.domain.AiQuestionBundle;
import tv.shout.snowyowl.domain.MatchQuestion;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.NoQuestionFoundException;
import tv.shout.snowyowl.engine.QuestionSupplier;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.service.ISnowyowlService;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class HandlerSendQuestion
extends BaseHandler
implements SyncMessageSender
{
    private static Logger _logger = Logger.getLogger(HandlerSendQuestion.class);

    private static final String SEND_QUESTION_AT = "SEND_QUESTION_AT";
    private static final String QUESTIONS_CHANGED = "QUESTIONS_CHANGED";

    @Autowired
    protected QuestionSupplier _questionSupplier;

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    private ISyncService _syncService;

    @Autowired
    private ITriggerService _triggerService;

    private Map<String, List<MatchQuestion>> _matchQuestionCache = new HashMap<>();

    public static DelayedMessage getDelayedMessage(
            long delayMs, Game game, Round round, Match match, List<MatchPlayer> matchPlayers, Map<Long, Subscriber> subscribers,
            List<Long> botsInGame, List<Long> sponsorsInGame, boolean isTieBreaker)
    {
        Map<String, Object> payload = new FastMap<>(
            "game", game,
            "round", round,
            "match", match,
            "matchPlayers", matchPlayers,
            "subscribers", subscribers,
            "botsInGame", botsInGame,
            "sponsorsInGame", sponsorsInGame,
            "isTieBreaker", isTieBreaker,
            "sendQuestionAt", System.currentTimeMillis() + delayMs
        );

        return new DelayedMessage(SEND_QUESTION_AT, payload, delayMs);
    }

    public static Message getQuestionsChangedMessage()
    {
        return new Message(QUESTIONS_CHANGED, null);
    }

    @Override
    //MessageProcessor
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case SEND_QUESTION_AT:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "game", ((Game)payload.get("game")).getId(),
                        "round", ((Round)payload.get("round")).getId(),
                        "match", ((Match)payload.get("match")).getId(),
                        "matchPlayers", getMatchPlayersFromPayload(payload, "matchPlayers"),
                        "isTieBreaker", payload.get("isTieBreaker"),
                        "subscribers", getSubscribersFromPayload(payload, "subscribers"),
                        "sendQuestionAt", payload.get("sendQuestionAt")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerSendQuestion received SEND_QUESTION_AT\n{0}", JsonUtil.print(map)));
                }
                handleSendQuestionAt(message);
                break;

            case QUESTIONS_CHANGED:
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug("HandlerSendQuestion received QUESTIONS_CHANGED");
                }
                handleQuestionsChanged(message);
                break;

            //else ignore
        }
    }

    private void handleQuestionsChanged(Message message)
    {
        _questionSupplier.notifyQuestionListChanged();
    }

    @SuppressWarnings("unchecked")
    private void handleSendQuestionAt(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        Game game = (Game) payload.get("game");
        Round round = (Round) payload.get("round");
        Match match = (Match) payload.get("match");
        List<MatchPlayer> matchPlayers = (List<MatchPlayer>) payload.get("matchPlayers");
        Map<Long, Subscriber> subscribers = (Map<Long, Subscriber>) payload.get("subscribers");
        List<Long> botsInGame = (List<Long>) payload.get("botsInGame");
        List<Long> sponsorsInGame = (List<Long>) payload.get("sponsorsInGame");
        boolean isTieBreaker= (Boolean) payload.get("isTieBreaker");
        long sendQuestionAt = (Long) payload.get("sendQuestionAt");

        if (sendQuestionAt <= System.currentTimeMillis()) {
            _logger.info("TIME TO SEND QUESTION");
            addQuestionToMatch(game, round, match, matchPlayers, subscribers, botsInGame, sponsorsInGame, isTieBreaker);

        } else {
            //not yet time; wait and try again
            if (_busLogger.isDebugEnabled()) {
                _busLogger.debug("HandlerSendQuestion SEND_QUESTION_AT message arrived too early. not yet time to send question. adding back to queue...");
            }
            _messageBus.sendDelayedMessage(getDelayedMessage(
                sendQuestionAt - System.currentTimeMillis(), game, round, match, matchPlayers, subscribers, botsInGame, sponsorsInGame, isTieBreaker
            ));
        }

    }

    private void addQuestionToMatch(
        Game game, Round round, Match match, List<MatchPlayer> matchPlayers, Map<Long, Subscriber> subscribers,
        List<Long> botsInGame, List<Long> sponsorsInGame, boolean isTieBreaker)
    {
        //other filter criteria from data already at hand
        Set<String> roundCategoryUuids = round.getCategories();
        Set<String> allowedLanguageCodes = game.getAllowableLanguageCodes();
        int minDifficulty = round.getActivityMinimumDifficulty() != null ? round.getActivityMinimumDifficulty() : Round.MIN_DIFFICULTY;
        int maxDifficulty = round.getActivityMaximumDifficulty() != null ? round.getActivityMaximumDifficulty() : Round.MAX_DIFFICULTY;

        List<Long> subscriberIds = matchPlayers.stream().map(mp -> mp.getSubscriberId()).collect(Collectors.toList());

        //grab a question and encrypt it (along with necessary decrypt metadata)
        Question question = (Question) wrapInTransaction((x) -> {
            try {
                Question q = _questionSupplier.getQuestion(roundCategoryUuids, allowedLanguageCodes, minDifficulty, maxDifficulty, game.getId(), subscriberIds);

                //if this is a tiebreaker, mark it as such
                if (isTieBreaker) {
                    q.setType(Question.TYPE.TIEBREAKER);
_logger.info(MessageFormat.format(">>> marking question as tiebreaker. matchId: {0}, questionId: {1}", match.getId(), q.getId()));
                    _dao.addTieBreakerQuestion(game.getId(), match.getId());
                }

                return q;

            } catch (NoQuestionFoundException e) {
                //this should never occur; checks above this make sure this sitation doesn't happen
                _logger.error("no question matching given criteria!", e);
            }
            return null;
        }, null);
        if (question == null) return;

        //store for later for the bots to use if they need (smarter bot AI's might have a percentage of time they pick the correct answer)
        String correctAnswerId = null;
        String incorrectAnswerId = null;
        for (QuestionAnswer a : question.getAnswers()) {
            if (a.getCorrect() != null && a.getCorrect()) {
                correctAnswerId = a.getId();
            } else {
                incorrectAnswerId = a.getId();
            }
        };

        String questionWithAnswers;
        String questionWithoutAnswers;
        try {
            //a version of the json which has the correct answer embedded
            questionWithAnswers = JsonUtil.getObjectMapper().writeValueAsString(question);

            //a version of the json which has the correct answer removed
            Question clonedQuestion = (Question) question.clone();
            clonedQuestion.getAnswers().forEach(a -> a.setCorrect(null));
            questionWithoutAnswers = JsonUtil.getObjectMapper().writeValueAsString(clonedQuestion);

        } catch (JsonProcessingException | CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        String questionDecryptKey = UUID.randomUUID().toString().replaceAll("-", "");
        String encryptedQuestionAndAnswersBodyWithAnswers;
        String encryptedQuestionAndAnswersBodyWithoutAnswers;
        encryptedQuestionAndAnswersBodyWithAnswers = _shoutContestService.aesEncrypt(questionDecryptKey.substring(0, 16), questionDecryptKey.substring(16), questionWithAnswers);
        encryptedQuestionAndAnswersBodyWithoutAnswers = _shoutContestService.aesEncrypt(questionDecryptKey.substring(0, 16), questionDecryptKey.substring(16), questionWithoutAnswers);

        //create the MatchQuestion row and the SubscriberQuestionAnswer rows for each player
        MatchQuestion matchQuestion = new MatchQuestion(game.getId(), round.getId(), match.getId(), question.getId(), round.getRoundActivityValue());
        List<SubscriberQuestionAnswer> subQuestionAnswers = new ArrayList<SubscriberQuestionAnswer>(matchPlayers.size());
        for (MatchPlayer matchPlayer : matchPlayers) {
            SubscriberQuestionAnswer sqa = new SubscriberQuestionAnswer(
                    game.getId(), round.getId(), match.getId(), question.getId(), matchQuestion.getId(), matchPlayer.getSubscriberId(), questionDecryptKey);
            subQuestionAnswers.add(sqa);
        }
        //store them in the database and update the sync doc with the "question" message
        wrapInTransaction((x) -> {
            _dao.insertMatchQuestion(matchQuestion);
            return null;
        }, null);

        //update the local cache
        List<MatchQuestion> mqs = _matchQuestionCache.get(matchQuestion.getMatchId());
        if (mqs == null) {
            mqs = new ArrayList<MatchQuestion>();
        }
        mqs.add(matchQuestion);
        _matchQuestionCache.put(matchQuestion.getMatchId(), mqs);

        String questionBody = game.isIncludeActivityAnswersBeforeScoring() ? encryptedQuestionAndAnswersBodyWithAnswers : encryptedQuestionAndAnswersBodyWithoutAnswers;

        final List<AiQuestionBundle> aiQuestionList = new ArrayList<>();
        wrapInTransaction((params) -> {
            String l_correctAnswerId = (String) ((Object[])params)[0];
            String l_incorrectAnswerId = (String) ((Object[])params)[1];
            for (SubscriberQuestionAnswer sqa : subQuestionAnswers) {
                _dao.insertSubscriberQuestionAnswer(sqa);

                //if this is an AI player, let it know they have a new question to answer
                if (botsInGame.contains(sqa.getSubscriberId()) || sponsorsInGame.contains(sqa.getSubscriberId())) {
                    aiQuestionList.add(new AiQuestionBundle(
                            sqa.getSubscriberId(), sqa.getId(), question, round.getPlayerMaximumDurationSeconds()*1_000L, l_correctAnswerId, l_incorrectAnswerId,
                            game.isUseDoctoredTimeForBots(), game.getEngineType()));
                }
            }
            return null;
        }, new Object[] {correctAnswerId, incorrectAnswerId});

        final String gameId = game.getId();
        final String fCorrectAnswerId = correctAnswerId;

        Long twitchSubscriberId = getTwitchSubscriberId(game.getId());
        boolean doesMatchContainTwitchSubscriber = (Boolean)
            wrapInTransaction((x) -> {
                boolean twitchy = false;

                for (SubscriberQuestionAnswer sqa : subQuestionAnswers) {
                    if (twitchSubscriberId != null && twitchSubscriberId == sqa.getSubscriberId()) {
                        twitchy = true;
                    }

                    Subscriber subscriber = subscribers.get(sqa.getSubscriberId());
                    if (!botsInGame.contains(sqa.getSubscriberId()) && !sponsorsInGame.contains(sqa.getSubscriberId())) {
                        enqueueSyncMessage(
                                JsonUtil.getObjectMapper(), _syncService, _logger,
                                gameId, ISnowyowlService.SYNC_MESSAGE_QUESTION,
                                new FastMap<>("subscriberQuestionAnswerId", sqa.getId(), "question", questionBody), subscriber, _socket, _triggerService);
                    }

                    //send a QUESTION_NOT_RETRIEVED_TIMEOUT_AT delayed message
                    DelayedMessage delayedMessage = HandlerAnswer.getRequestQuestionNotRetrievedTimeoutMessage(round.getActivityMaximumDurationSeconds()*1_000L, sqa.getId(), game.getEngineType() );
                    if (_busLogger.isDebugEnabled()) {
                        _busLogger.debug("HandlerSendQuestion sending delayed message: " + delayedMessage.type);
                    }
                    _messageBus.sendDelayedMessage(delayedMessage);
//if (_logger.isDebugEnabled()) {
//    _logger.debug(MessageFormat.format("non bot subscribers now have {0} ms to request the decrypt key", round.getActivityMaximumDurationSeconds()*1_000L));
//}
                }

                for (AiQuestionBundle bundle : aiQuestionList) {
                    if (botsInGame.contains(bundle.aiSubscriberId)) {
if (_logger.isDebugEnabled()) {
    _logger.debug("letting bot " + bundle.aiSubscriberId + " know it's time to answer a question");
}
                        _botEngine.submitAnswer(bundle);

                    } else if (sponsorsInGame.contains(bundle.aiSubscriberId)) {
if (_logger.isDebugEnabled()) {
    _logger.debug("letting sponsor " + bundle.aiSubscriberId + " know it's time to answer a question");
}
                        _sponsorEngine.submitAnswer(bundle);
                    }
                }

                return twitchy;
            }, null);

        //send a twitch socket.io message
        if (doesMatchContainTwitchSubscriber) {
            sendTwitchUpdate(game, round, match, twitchSubscriberId, subQuestionAnswers, question, isTieBreaker, fCorrectAnswerId);
        }
    }

    protected void sendTwitchUpdate(Game game, Round round, Match match, Long twitchSubscriberId, List<SubscriberQuestionAnswer> sqas, Question question, boolean isTieBreaker, String correctAnswerId)
    {
        if (_socket != null) {
            long opId = 0;
            for (SubscriberQuestionAnswer sqa : sqas) {
                if (sqa.getSubscriberId() != twitchSubscriberId) {
                    opId = sqa.getSubscriberId();
                    break;
                }
            }

            //put the correct answer back in
            for (QuestionAnswer a: question.getAnswers()) {
                if (a.getId().equals(correctAnswerId)) {
                    a.setCorrect(true);
                    break;
                }
            }

            Map<String, Object> twitchMap = new FastMap<>(
                "type", "TWITCH_QUESTION",
                "gameid", game.getId(),
                "roundId", round.getId(),
                "matchId", match.getId(),
                "subscriberId", twitchSubscriberId,
                "opponentId", opId,
                "question", question,
                "tieBreaker", isTieBreaker
            );

            if (_socket != null) {
                try {
                    _socket.emit("send_twitch_message", JsonUtil.getObjectMapper().writeValueAsString(twitchMap));
                } catch (JsonProcessingException e) {
                    _logger.error("unable to emit send_twitch_message", e);
                }
            }
        }
    }
}
