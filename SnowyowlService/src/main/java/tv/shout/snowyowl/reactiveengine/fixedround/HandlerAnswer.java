package tv.shout.snowyowl.reactiveengine.fixedround;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.DelayedMessage;
import tv.shout.simplemessagebus.Message;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;
import tv.shout.util.MaxSizeHashMap;

public class HandlerAnswer
extends BaseHandler
{
    private static Logger _logger = Logger.getLogger(HandlerAnswer.class);

    private static final String ANSWER = "ANSWER";
    private static final String BOT_ANSWER = "BOT_ANSWER";
    private static final String QUESTION_NOT_RETRIEVED_TIMEOUT_AT = "QUESTION_NOT_RETRIEVED_TIMEOUT_AT";
    private static final String ANSWER_TIMEOUT_AT = "ANSWER_TIMEOUT_AT";

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    private Lock _lock = new ReentrantLock();

    public static Message getAnswerMessage(SubscriberQuestionAnswer sqa, String selectedAnswerId, String gameEngineType)
    {
        Map<String, Object> payload = new FastMap<>(
            "sqa", sqa,
            "selectedAnswerId", selectedAnswerId,
            "gameEngineType", gameEngineType
        );

        return new Message(ANSWER, payload);
    }

    public static Message getBotAnswerMessage(SubscriberQuestionAnswer sqa, String gameEngineType)
    {
        Map<String, Object> payload = new FastMap<>(
            "sqa", sqa,
            "gameEngineType", gameEngineType
        );

        return new Message(BOT_ANSWER, payload);
    }

    public static DelayedMessage getAnswerTimeoutMessage(long delayMs, String sqaId, String gameEngineType)
    {
        Map<String, Object> payload = new FastMap<>(
            "timeoutAt", System.currentTimeMillis() + delayMs,
            "sqaId", sqaId,
            "gameEngineType", gameEngineType
        );

        return new DelayedMessage(ANSWER_TIMEOUT_AT, payload, delayMs);
    }

    public static DelayedMessage getRequestQuestionNotRetrievedTimeoutMessage(long delayMs, String sqaId, String gameEngineType)
    {
        Map<String, Object> payload = new FastMap<>(
                "timeoutAt", System.currentTimeMillis() + delayMs,
                "sqaId", sqaId,
                "gameEngineType", gameEngineType
        );

        return new DelayedMessage(QUESTION_NOT_RETRIEVED_TIMEOUT_AT, payload, delayMs);
    }

    @Override
    //MessageProcessor
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message)
    {
        switch (message.type)
        {
            case ANSWER:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "selectedAnswerId", payload.get("selectedAnswerId"),
                        "gameEngineType", payload.get("gameEngineType"),
                        "sqa", getSqaFromPayload(payload, "sqa")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerAnswer received ANSWER\n{0}", JsonUtil.print(map)));
                }
                handleAnswer(message);
                break;

            case BOT_ANSWER:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "gameEngineType", payload.get("gameEngineType"),
                        "sqa", getSqaFromPayload(payload, "sqa")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerAnswer received BOT_ANSWER\n{0}", JsonUtil.print(map)));
                }
                handleBotAnswer(message);
                break;

            case ANSWER_TIMEOUT_AT:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "gameEngineType", payload.get("gameEngineType"),
                        "sqa", getSqaFromPayload(payload, "sqa"),
                        "timeoutAt", payload.get("timeoutAt")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerAnswer received ANSWER_TIMEOUT_AT\n{0}", JsonUtil.print(map)));
                }
                handleAnswerTimeoutAt(message);
                break;

            case QUESTION_NOT_RETRIEVED_TIMEOUT_AT:
                if (_busLogger.isDebugEnabled()) {
                    Map<String, Object> payload = (Map<String, Object>) message.payload;

                    Map<String, Object> map = new FastMap<>(
                        "gameEngineType", payload.get("gameEngineType"),
                        "sqa", getSqaFromPayload(payload, "sqa"),
                        "timeoutAt", payload.get("timeoutAt")
                    );

                    _busLogger.debug(MessageFormat.format("HandlerAnswer received QUESTION_NOT_RETRIEVED_TIMEOUT_AT\n{0}", JsonUtil.print(map)));
                }
                handleAQuestionNotRetrievedTimeoutAt(message);
                break;

            //else ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAnswer(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) payload.get("sqa");
        String selectedAnswerId = (String) payload.get("selectedAnswerId");
        String gameEngineType = (String) payload.get("gameEngineType");

        _lock.lock();
        try {
            //if an answer has already arrived, ignore this message
            if (sqa.getSelectedAnswerId() != null) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("ignoring ANSWER message for subscriber {0,number,#}: subscriber already answered", sqa.getSubscriberId()));
                }
                return;
            }

            List<MatchPlayer> matchPlayers = (List<MatchPlayer>) wrapInTransaction((x) -> {
                return _shoutContestService.getMatchPlayersForMatch(sqa.getMatchId());
            }, null);

            //record the answer
            long durationMilliseconds = (Long) wrapInTransaction((x) -> {
                return _commonBusinessLogic.submitAnswer(sqa, selectedAnswerId);
            }, null);

if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format(
            "received answer for subscriber {0,number,#}. duration: {1} ms",
            sqa.getSubscriberId(), durationMilliseconds));
}

            //send the twitch update (maybe)
            Long twitchSubscriberId = getTwitchSubscriberId(sqa.getGameId());
            if (twitchSubscriberId != null) {
                boolean isMatchTwitchMonitored = false;
                for (MatchPlayer mp : matchPlayers) {
                    if (mp.getSubscriberId() == twitchSubscriberId) {
                        isMatchTwitchMonitored = true;
                        break;
                    }
                }
                if (isMatchTwitchMonitored && sqa.getSubscriberId() == twitchSubscriberId) {
                    sendTwitchUpdate(twitchSubscriberId, sqa, matchPlayers, selectedAnswerId, durationMilliseconds);
                }
            }

            checkAndPossiblySendScoreQuestion(gameEngineType, sqa, matchPlayers);

        } finally {
            _lock.unlock();
        }

    }

    @SuppressWarnings("unchecked")
    private void handleBotAnswer(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) payload.get("sqa");
        String gameEngineType = (String) payload.get("gameEngineType");

        _lock.lock();
        try {
            checkAndPossiblySendScoreQuestion(gameEngineType, sqa, new ArrayList<>());
        } finally {
            _lock.unlock();
        }
    }


    @SuppressWarnings("unchecked")
    private void handleAnswerTimeoutAt(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        long timeoutAt = (Long) payload.get("timeoutAt");
        String sqaId = (String) payload.get("sqaId");
        String gameEngineType = (String) payload.get("gameEngineType");

        //see if an answer has already arrived.
        _lock.lock();
        try {
            Object[] obj = (Object[]) wrapInTransaction((x) -> {
                SubscriberQuestionAnswer xSqa = _dao.getSubscriberQuestionAnswer(sqaId);
                //List<Long> xBotSubscriberIds = _botEngine.getBotsForGame(xSqa.getGameId());
                //List<Long> xSponsorSubscriberIds = _sponsorEngine.getSponsorsForGame(xSqa.getGameId());

                return new Object[] {xSqa/*, xBotSubscriberIds, xSponsorSubscriberIds*/};
            }, null);
            SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) obj[0];
            //List<Long> botSubscriberIds = (List<Long>) obj[1];
            //List<Long> sponsorSubscriberIds = (List<Long>) obj[2];

            if (sqa.getSelectedAnswerId() != null) {
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug(MessageFormat.format("HandlerAnswer ignoring ANSWER_TIMEOUT_AT message for subscriber {0,number,#}: subscriber already answered", sqa.getSubscriberId()));
                }
                return;
            }

            if (timeoutAt <= System.currentTimeMillis()) {
                //they didn't answer in time
                handleTimeout(gameEngineType, sqa);

            } else {
                //not yet time; wait and try again
if (_logger.isDebugEnabled()) _logger.debug("HandlerAnswer ANSWER_TIMEOUT_AT message arrived too early. not yet time to timeout question. adding back to queue...");
                _messageBus.sendDelayedMessage(getAnswerTimeoutMessage(
                    timeoutAt - System.currentTimeMillis(), sqaId, gameEngineType
                ));
            }

        } finally {
            _lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAQuestionNotRetrievedTimeoutAt(Message message)
    {
        //parse the payload
        Map<String, Object> payload = (Map<String, Object>) message.payload;
        long timeoutAt = (Long) payload.get("timeoutAt");
        String sqaId = (String) payload.get("sqaId");
        String gameEngineType = (String) payload.get("gameEngineType");

        //see if the question decrypt key has been requested. if so, ignore this message
        _lock.lock();
        try {
            SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) wrapInTransaction((x) -> {
                return _dao.getSubscriberQuestionAnswer(sqaId);
            }, null);

            if (sqa.getQuestionPresentedTimestamp() != null) {
                if (_busLogger.isDebugEnabled()) {
                    _busLogger.debug(MessageFormat.format("HandlerAnswer ignoring QUESTION_NOT_RETRIEVED_TIMEOUT_AT message for subscriber {0,number,#}: subscriber already requested decrypt key", sqa.getSubscriberId()));
                }
                return;
            }

            if (timeoutAt <= System.currentTimeMillis()) {
                //they didn't retrieve the decrypt key in time
                handleTimeout(gameEngineType, sqa);

            } else {
                //not yet time; wait and try again
if (_logger.isDebugEnabled()) _logger.debug("HandlerAnswer QUESTION_NOT_RETRIEVED_TIMEOUT_AT message arrived too early. not yet time to timeout retrieving decrypt key. adding back to queue...");
                _messageBus.sendDelayedMessage(getRequestQuestionNotRetrievedTimeoutMessage(
                    timeoutAt - System.currentTimeMillis(), sqaId, gameEngineType
                ));
            }

        } finally {
            _lock.unlock();
        }

    }

    //locked to prevent race condition, but not wrapped in db transaction
    private void handleTimeout(String gameEngineType, SubscriberQuestionAnswer sqa)
    {
if (_logger.isDebugEnabled()) {
    _logger.debug(MessageFormat.format("subscriber {0,number,#} timed out", sqa.getSubscriberId()));
}
        sqa.setSelectedAnswerId(null);
        sqa.setDetermination(SubscriberQuestionAnswer.ANSWER_DETERMINATION.LOST_TIMEOUT);

        wrapInTransaction((x) -> {
            _dao.updateSubscriberQuestionAnswer(sqa);
            return null;
        }, null);

        checkAndPossiblySendScoreQuestion(gameEngineType, sqa, new ArrayList<>());
    }

    protected void sendTwitchUpdate(Long twitchSubscriberId, SubscriberQuestionAnswer sqa, List<MatchPlayer> matchPlayers, String selectedAnswerId, long durationMilliseconds)
    {
        if (_socket != null) {
            long opId = 0;
            for (MatchPlayer mp : matchPlayers) {
                if (mp.getSubscriberId() != twitchSubscriberId) {
                    opId = mp.getSubscriberId();
                    break;
                }
            }

            Map<String, Object> twitchMap = new FastMap<>(
                "type", "TWITCH_QUESTION_ANSWERED",
                "gameid", sqa.getGameId(),
                "roundId", sqa.getRoundId(),
                "matchId", sqa.getMatchId(),
                sqa.getSubscriberId() == twitchSubscriberId ? "subscriberId" : "opponentId", sqa.getSubscriberId() == twitchSubscriberId ? twitchSubscriberId : opId,
                "selectedAnswerId", selectedAnswerId,
                "durationMilliseconds", durationMilliseconds
            );

            try {
                _socket.emit("send_twitch_message", JsonUtil.getObjectMapper().writeValueAsString(twitchMap));
            } catch (JsonProcessingException e) {
                _logger.error("unable to emit send_twitch_message", e);
            }
        }
    }

    //locked to prevent race condition, but not wrapped in db transaction
    private void checkAndPossiblySendScoreQuestion(String gameEngineType, SubscriberQuestionAnswer sqa, final List<MatchPlayer> matchPlayers)
    {
        SubscriberQuestionAnswer opponentSqa = (SubscriberQuestionAnswer) wrapInTransaction((x) -> {
            if (matchPlayers.isEmpty()) {
                matchPlayers.addAll(_shoutContestService.getMatchPlayersForMatch(sqa.getMatchId()));
            }

            long opponentSubscriberId = matchPlayers.stream()
                    .filter(mp -> sqa.getSubscriberId() != mp.getSubscriberId())
                    .map(mp -> mp.getSubscriberId())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException());

           return
                _dao.getSubscriberQuestionAnswersForMatch(sqa.getMatchId(), opponentSubscriberId).stream()
                        .filter(xsqa -> sqa.getMatchQuestionId().equals(xsqa.getMatchQuestionId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException());
        }, null);

        //another instance of when something can be called multiple times when it shouldn't be.
        //if both opponents answer very close to each other (like bots often do), then they could both get a message in here to score the question
        // and both of them come in and go "yep, my opponent has answered", and thus the score question is called twice. this should NOT be allowed

        if (opponentSqa.getSelectedAnswerId() != null) {
            _scoreQuestionLock.lock();
            try {
                if (!_alreadyScoredMatchQuestion.containsKey(opponentSqa.getMatchQuestionId())) {
                    _alreadyScoredMatchQuestion.put(opponentSqa.getMatchQuestionId(), true);

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("both subscribers have answered (or timed out). time to score the question...");
                    }
                    Message msg = BaseHandlerScoreQuestion.getScoreQuestionMessage(gameEngineType, matchPlayers, Arrays.asList(sqa, opponentSqa));
                    if (_busLogger.isDebugEnabled()) {
                        _busLogger.debug("HandlerAnswer sending message: " + msg.type);
                    }
                    _messageBus.sendMessage(msg);
                }

            } finally {
                _scoreQuestionLock.unlock();
            }

        } else {
if (_logger.isDebugEnabled()) {
    _logger.debug("still waiting for opponent to answer");
}
        }
    }

    private Lock _scoreQuestionLock = new ReentrantLock();
    private Map<String, Boolean> _alreadyScoredMatchQuestion = new MaxSizeHashMap<String, Boolean>().withMaxSize(29_999);
}
