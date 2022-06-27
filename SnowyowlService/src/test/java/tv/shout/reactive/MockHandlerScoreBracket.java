package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerScoreBracketFixedLife;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;
import tv.shout.snowyowl.service.CurrentRankCalculator;
import tv.shout.sync.service.ISyncService;

public class MockHandlerScoreBracket
extends HandlerScoreBracketFixedLife
{
    private static Logger _logger = Logger.getLogger(MockHandlerScoreBracket.class);

    public MockHandlerScoreBracket(
        MessageBus messageBus, IDaoMapper dao,
        IIdentityService identityService, IShoutContestService shoutContestService,
        MockSubscriberStatsHandler subscriberStatsHandler, CurrentRankCalculator currentRankCalculator,
        MatchMaker matchMaker)
    {
        _messageBus = messageBus;
        _dao = dao;
        _identityService = identityService;
        _shoutContestService = shoutContestService;
        _subscriberStatsHandler = subscriberStatsHandler;
        _currentRankCalculator = currentRankCalculator;
        _matchMaker = matchMaker;
    }

    @Override
    public void enqueueSyncMessage(
        ObjectMapper jsonMapper, ISyncService syncService, Logger logger,
        String contextualId, String messageType, Map<String, Object> payload,
        Subscriber subscriber, Socket socketIoSocket, ITriggerService triggerService)
    {
        _logger.debug(MessageFormat.format(
                "enqueueing sync message. recipient: {0,number,#}, messageType: {1}",
                subscriber.getSubscriberId(), messageType));
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }

}
