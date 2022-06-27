package tv.shout.reactive;

import java.util.function.Function;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.collector.CommonBusinessLogic;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerBeginBracketPlay;
import tv.shout.snowyowl.reactiveengine.fixedround.MatchMaker;

public class MockHandlerBeginBracketPlay
extends HandlerBeginBracketPlay
{
    public MockHandlerBeginBracketPlay(IShoutContestService shoutContestService, CommonBusinessLogic commonBusinessLogic, MatchMaker matchMaker)
    {
        _shoutContestService = shoutContestService;
        _commonBusinessLogic = commonBusinessLogic;
        _matchMaker = matchMaker;
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }
}
