package tv.shout.reactive;

import java.util.function.Function;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginMatch;

public class MockHandlerBeginMatch
extends HandlerBeginMatch
{
    public MockHandlerBeginMatch(MessageBus messageBus, IDaoMapper dao, IShoutContestService shoutContestService)
    {
        _messageBus = messageBus;
        _dao = dao;
        _shoutContestService = shoutContestService;
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }
}
