package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.Date;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.webdatastore.service.IWebDataStoreService;

import tv.shout.sc.service.IShoutContestService;
import tv.shout.simplemessagebus.MessageBus;
import tv.shout.snowyowl.common.GameStatsHandler;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.engine.MMECommon;
import tv.shout.snowyowl.reactiveengine.fixedround.HandlerDocPublisher;

public class MockHandlerDocPublisher
extends HandlerDocPublisher
{
    private static Logger _logger = Logger.getLogger(MockHandlerDocPublisher.class);

    public MockHandlerDocPublisher(MessageBus messageBus, IDaoMapper dao,
            IIdentityService identityService, IShoutContestService shoutContestService, MockSubscriberStatsHandler subscriberStatsHandler,
            MMECommon mmeCommon)
    {
        _messageBus = messageBus;
        _dao = dao;
        _identityService = identityService;
        _shoutContestService = shoutContestService;
        _subscriberStatsHandler = subscriberStatsHandler;
        _mmeCommon = mmeCommon;
    }

    @Override
    public void publishJsonWdsDoc(Logger logger, IWebDataStoreService wdsService, Date expireDate, String path, Object data)
    {
        logger.debug("MOCK published doc: " + path);
    }

    @Override
    protected void sendCurrentRankSocketIoMessage(Subscriber subscriber, String payoutTableRowUuid)
    {
        _logger.debug(MessageFormat.format("MOCK sending socket.io msg: send_current_rank to subscriber {0,number,#}", subscriber.getSubscriberId()));
    }

    @Override
    public void publishGameWithExtrasToWds(
        String gameId, PayoutModel pm, GamePayout gp, GameStatsHandler gsh, GameStats gameStats,
        IDaoMapper _dao, IShoutContestService _shoutContestService)
    {
        _logger.debug("MOCK: publishing game with extras");
    }

    @Override
    protected Object wrapInTransaction(Function<Object, Object> transactionalMethod, Object params)
    {
        Object result;
        result = transactionalMethod.apply(params);
        return result;
    }
}
