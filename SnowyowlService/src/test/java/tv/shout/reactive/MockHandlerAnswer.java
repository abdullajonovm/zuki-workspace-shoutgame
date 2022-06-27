package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

import org.apache.log4j.Logger;

import tv.shout.sc.domain.MatchPlayer;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.SubscriberQuestionAnswer;
import tv.shout.snowyowl.engine.BotEngine;
import tv.shout.snowyowl.engine.SponsorEngine;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerAnswer;

public class MockHandlerAnswer
extends HandlerAnswer
{
    private static Logger _logger = Logger.getLogger(MockHandlerAnswer.class);

    public MockHandlerAnswer(MessageBus messageBus, IDaoMapper dao, IShoutContestService shoutContestService, MockCommonBusinessLogic commonBusinessLogic,
            BotEngine botEngine, SponsorEngine sponsorEngine)
    {
        _messageBus = messageBus;
        _dao = dao;
        _shoutContestService = shoutContestService;
        _commonBusinessLogic = commonBusinessLogic;
        _botEngine = botEngine;
        _sponsorEngine = sponsorEngine;
    }

//    @Override
//    public void enqueueSyncMessage(
//        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
//        String contextualId, String messageType, Map<String, Object> payload,
//        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
//    {
//        _logger.debug(MessageFormat.format(
//                "enqueueing sync message. recipient: {0,number,#}, messageType: {1}",
//                subscriber.getSubscriberId(), messageType));
//    }

    @Override
    protected void sendTwitchUpdate(Long twitchSubscriberId, SubscriberQuestionAnswer sqa, List<MatchPlayer> matchPlayers, String selectedAnswerId, long durationMilliseconds)
    {
        _logger.debug(MessageFormat.format(
            "sending twitch update [TWITCH_QUESTION_ANSWERED]. twitch subscriberId: {0,number,#}",
            twitchSubscriberId));
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

}
