package tv.shout.reactive;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;

import com.meinc.identity.service.IIdentityService;
import com.meinc.push.service.IPushService;

import tv.shout.sc.domain.Game;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.engine.fixedround.PayoutManagerFixedRoundSingleLife;

public class MockPayoutManager
extends PayoutManagerFixedRoundSingleLife
{
    private static Logger _logger = Logger.getLogger(MockPayoutManager.class);

    public MockPayoutManager(IDaoMapper dao, IShoutContestService shoutContestService, IIdentityService identityService)
    {
        _dao = dao;
        _shoutContestService = shoutContestService;
        _identityService = identityService;
    }

    @Override
    protected PayoutModel getPayoutModel(GamePayout gamePayout)
    {
        return _dao.getPayoutModel(gamePayout.getPayoutModelId());
    }


    @Override
    public void sendGamePush(
            PlatformTransactionManager transactionManager, IDaoMapper dao, IPushService pushService, Logger logger,
            long subscriberId, String languageCode, Game game, String apsCategory, String notificationTitle, String notificationBody, String type, Map<String, Object> extras)
    {
        _logger.debug(MessageFormat.format("sending game push to {0,number,#}. type: {1}, title: {2}, body: {3}", subscriberId, type, notificationTitle, notificationBody));
    }
}
