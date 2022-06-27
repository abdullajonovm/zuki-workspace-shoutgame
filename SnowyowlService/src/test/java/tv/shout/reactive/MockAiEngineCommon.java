package tv.shout.reactive;

import java.util.Random;

import org.apache.log4j.Logger;

import com.meinc.identity.domain.Subscriber;

import tv.shout.collector.PublishResponseError;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.AiQuestionBundle;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerAnswer;

public interface MockAiEngineCommon
{
    default void mockSubmitAnswer(
            final AiQuestionBundle bundle, Logger logger, CommonBusinessLogic commonBusinessLogic, IDaoMapper dao, MessageBus messageBus)
    {
        new Thread() {
            @Override
            public void run()
            {
                Subscriber bot = new Subscriber();
                bot.setSubscriberId(bundle.aiSubscriberId);

                try {
                    //request the decrypt key
                    /*String questionDecryptKey = */commonBusinessLogic.getQuestionDecryptKey(null, null, "botSubmitAnswer", bot, bundle.sqaId);

                    //pick an answer time that is between 1 and 2 seconds before timeout time (we want bots to be slow and allow the real players to win)
                    long answerDurationMs = bundle.numMsBeforeTimeout - 1_000L - new Random().nextInt(1000);

                    //shouldn't happen, but bad data has slipped through before causing it to be negative, which caused bad things later on down the road
                    if (answerDurationMs <= 0) {
logger.warn("invalid answerDurationMs. bundle.numMsBeforeTimeout: " + bundle.numMsBeforeTimeout);
                        answerDurationMs = 9999L;
                    }

                    //pick a random answer
                    QuestionAnswer answer = bundle.question.getAnswers().get(new Random().nextInt(bundle.question.getAnswers().size()));
                    String answerId = answer.getId();

                    //pick the correct answer
                    //String answerId = bundle.correctAnswerId;

                    //pick a wrong answer
                    //`String answerId = bundle.incorrectAnswerId;

                    if (!bundle.useDoctoredTime) {
                        try { Thread.sleep(answerDurationMs); } catch (InterruptedException ignored) {}
                    }

                    //submit the answer
if (logger.isDebugEnabled()) {
    logger.debug("AI " + bot.getSubscriberId() + " is submitting answer " + answerId + " on sqa " + bundle.sqaId + ". time: " + answerDurationMs);
}
                    SubscriberQuestionAnswer sqa = dao.getSubscriberQuestionAnswer(bundle.sqaId);
                    sqa.setSelectedAnswerId(answerId);
                    sqa.setDurationMilliseconds(answerDurationMs);
                    dao.setAnswerOnSubscriberQuestionAnswer(sqa);

                    messageBus.sendMessage(HandlerAnswer.getBotAnswerMessage(sqa, bundle.gameEngineTYpe));

                } catch (PublishResponseError e) {
                    logger.error("unexpected error while bot was submitting answer", e);
                }
            };
        }.start();
    }
}
