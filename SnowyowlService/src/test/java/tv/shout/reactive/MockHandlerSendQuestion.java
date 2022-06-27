package tv.shout.reactive;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.sc.domain.Match;
import tv.shout.sc.domain.Round;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.QuestionSupplier;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerAnswer;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerSendQuestion;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.JsonUtil;

public class MockHandlerSendQuestion
extends HandlerSendQuestion
{
    private static Logger _logger = Logger.getLogger(MockHandlerSendQuestion.class);
    private String _gameEngineType;

    public MockHandlerSendQuestion(
            MessageBus messageBus, IDaoMapper dao, QuestionSupplier questionSupplier, IShoutContestService shoutContestService,
            BotEngine botEngine, SponsorEngine sponsorEngine, String gameEngineType)
    {
        _messageBus = messageBus;
        _dao = dao;
        _questionSupplier = questionSupplier;
        _shoutContestService = shoutContestService;
        _botEngine = botEngine;
        _sponsorEngine = sponsorEngine;
        _gameEngineType = gameEngineType;
    }

    public static Map<String, Integer> _matchIdToQuestionNumber = new HashMap<>();

    @Override
    public void enqueueSyncMessage(
        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
        String contextualId, String messageType, Map<String, Object> payload,
        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
    {
        _logger.debug(MessageFormat.format(
                "enqueueing sync message. recipient: {0,number,#}, messageType: {1}",
                subscriber.getSubscriberId(), messageType));

        //find out which question number this is
        String sqaId = (String) payload.get("subscriberQuestionAnswerId");
        SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(sqaId);
        String matchId = sqa.getMatchId();
        Integer questionNumber = _matchIdToQuestionNumber.get(matchId);
        if (questionNumber == null) {
            questionNumber = 1;
        } else {
            questionNumber++;
        }
        _matchIdToQuestionNumber.put(matchId, questionNumber);

        //for the first and third and fourth, question, kick of a timer that waits 500ms and then retrieves the decrypt key
        if (questionNumber != 2) {
            final String encryptedQuestionBody = (String) payload.get("question");

            new Thread(() -> {
                try { Thread.sleep(500L); } catch (InterruptedException ignored) {}
                requestDecryptKey(sqaId, encryptedQuestionBody, _gameEngineType);
            }).start();
        }
        //else: for the 2nd question, do a timeout (which basically means do nothing here)
    }

    @Override
    protected void sendTwitchUpdate(Game game, Round round, Match match, Long twitchSubscriberId, List<SubscriberQuestionAnswer> sqas, Question question, boolean isTieBreaker, String correctAnswerId)
    {
        _logger.debug(MessageFormat.format(
            "sending twitch update [TWITCH_QUESTION]. twitch subscriberId: {0,number,#}",
            twitchSubscriberId));
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

    private void requestDecryptKey(String sqaId, String encryptedQuestionBody, String gameEngineType)
    {
        //this mocks out the client requesting the decrypt key
        Object[] obj = (Object[]) wrapInTransaction((x) -> {
            //set the presented timestamp
            SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(sqaId);
            sqa.setQuestionPresentedTimestamp(new Date());
            _dao.setQuestionViewedTimestampOnSubscriberQuestionAnswer(sqa);

            Round round = _shoutContestService.getRound(sqa.getRoundId());

            return new Object[] {sqa, round};
        }, null);
        SubscriberQuestionAnswer sqa = (SubscriberQuestionAnswer) obj[0];
        Round round = (Round) obj[1];

//        _logger.debug(MessageFormat.format("subscriber {0,number,#} just requested the question decrypt key", sqa.getSubscriberId()));

        //now the clock starts ticking for an answer timeout
        _messageBus.sendDelayedMessage(HandlerAnswer.getAnswerTimeoutMessage(round.getPlayerMaximumDurationSeconds()*1_000L, sqaId, gameEngineType));
//        _logger.debug(MessageFormat.format("subscriber {0,number,#} now has {1} ms to answer the question", sqa.getSubscriberId(), round.getPlayerMaximumDurationSeconds()*1_000L));

        //wait 4 seconds, and then submit the answer
        new Thread(() -> {
            try { Thread.sleep(4_000L); } catch (InterruptedException ignored) {}
            submitAnswer(round, sqa, encryptedQuestionBody, gameEngineType);
        }).start();
    }

    private void submitAnswer(Round round, SubscriberQuestionAnswer sqa, String encryptedQuestionBody, String gameEngineType)
    {
        String p = sqa.getQuestionDecryptKey().substring(0, 16);
        String iv = sqa.getQuestionDecryptKey().substring(16);
        String unencryptedQuestionBody = aesDecrypt(encryptedQuestionBody, "UTF-8", p, iv);

        String matchId = sqa.getMatchId();
        Integer questionNumber = _matchIdToQuestionNumber.get(matchId);
        if (questionNumber == null) {
            questionNumber = 1;
        }

//        _logger.info("unencrypted question:\n" + unencryptedQuestionBody);
        try {
            Question question = JsonUtil.getObjectMapper().readValue(unencryptedQuestionBody, Question.class);

            String correctAnswerId = question.getAnswers().get(0).getId();
            String incorrectAnswerId = question.getAnswers().get(1).getId();

            //for all but 3rd question, answer correctly
            if (questionNumber != 3) {
                _messageBus.sendMessage(HandlerAnswer.getAnswerMessage(sqa, correctAnswerId, gameEngineType));
            } else {
                _messageBus.sendMessage(HandlerAnswer.getAnswerMessage(sqa, incorrectAnswerId, gameEngineType));
            }

        } catch (IOException e) {
            _logger.error("unable to parse question json", e);
        }
    }

    private String aesDecrypt(String message, String encoding, String passphrase, String initializationVector)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes(encoding));
            SecretKeySpec skeySpec = new SecretKeySpec(passphrase.getBytes(encoding), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] inBytes = Base64.getDecoder().decode(message.getBytes(encoding));
            return new String(cipher.doFinal(inBytes), encoding);

        } catch (Exception e) {
            _logger.error("unable to decrypt question", e);
            return null;
        }
    }
}
