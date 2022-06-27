package tv.shout.snowyowl.engine;

import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.meinc.identity.domain.Subscriber;

import tv.shout.collector.PublishResponseError;
import tv.shout.simplemessagebus.Message;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.AiQuestionBundle;
import tv.shout.snowyowl.domain.QuestionAnswer;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerAnswer;

public class BaseAiEngine
{
    private static Logger _logger = Logger.getLogger(BaseAiEngine.class);
    private static Logger _busLogger = Logger.getLogger("messagebus");

    @Autowired
    protected CommonBusinessLogic _commonBusinessLogic;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    @Autowired
    protected IDaoMapper _dao;

    @Autowired
    private MessageBus _messageBus;

    public void submitAnswer(final AiQuestionBundle bundle)
    {
        //do on a background thread so it doesn't interrupt the workflow
        //FUxTURE: this isn't necessary if we're using doctoredTime (which is most likely the case in production environments)
        new Thread() {
            @Override
            public void run()
            {
                Subscriber bot = new Subscriber();
                bot.setSubscriberId(bundle.aiSubscriberId);

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
                TransactionStatus txStatus = _transactionManager.getTransaction(txDef);

                try {
                    //request the decrypt key
                    /*String questionDecryptKey = */_commonBusinessLogic.getQuestionDecryptKey(null, null, "botSubmitAnswer", bot, bundle.sqaId);

                    //pick an answer time that is between 1 and 2 seconds before timeout time (we want bots to be slow and allow the real players to win)
                    long answerDurationMs = bundle.numMsBeforeTimeout - 1_000L - new Random().nextInt(1000);

                    //shouldn't happen, but bad data has slipped through before causing it to be negative, which caused bad things later on down the road
                    if (answerDurationMs <= 0) {
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
_logger.info("AI " + bot.getSubscriberId() + " is submitting answer " + answerId + " on sqa " + bundle.sqaId + ". time: " + answerDurationMs);

//                    _commonBusinessLogic.submitAnswer(bot, bundle.sqaId, answerId, answerDurationMs); x
                    SubscriberQuestionAnswer sqa = _dao.getSubscriberQuestionAnswer(bundle.sqaId);
                    sqa.setSelectedAnswerId(answerId);
                    sqa.setDurationMilliseconds(answerDurationMs);
                    _dao.setAnswerOnSubscriberQuestionAnswer(sqa);

                    Message msg = HandlerAnswer.getBotAnswerMessage(sqa, bundle.gameEngineTYpe);
                    if (_busLogger.isDebugEnabled()) {
                        _busLogger.debug("BaseAiEngine sending message: " + msg.type);
                    }
                    _messageBus.sendMessage(msg);

                    _transactionManager.commit(txStatus);
                    txStatus = null;

                } catch (PublishResponseError e) {
                    _logger.error("unexpected error while bot was submitting answer", e);
                } finally {
                    if (txStatus != null) {
                        _transactionManager.rollback(txStatus);
                        txStatus = null;
                    }
                }
            };
        }.start();
    }

}
