package tv.shout.snowyowl.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.service.IIdentityService;

import io.socket.client.Socket;
import tv.shout.sc.service.IShoutContestService;
import tv.shout.snowyowl.common.FileHandler;
import tv.shout.snowyowl.common.GamePublisher;
import tv.shout.snowyowl.common.SyncMessageSender;
import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.GamePayout;
import tv.shout.snowyowl.domain.GameStats;
import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.sync.service.ISyncService;
import tv.shout.util.JsonUtil;

public abstract class BaseEngine
implements SyncMessageSender, FileHandler, GamePublisher
{
    protected static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    @Value("${sm.engine.statedir}")
    protected String _stateDir;

    @Autowired
    protected ISyncService _syncService;

    @Autowired
    protected IDaoMapper _dao;

    @Autowired
    protected PlatformTransactionManager _transactionManager;

    @Autowired
    protected BotEngine _botEngine;

    @Autowired
    protected SponsorEngine _sponsorEngine;

    @Autowired
    protected IShoutContestService _shoutContestService;

    @Autowired
    protected IIdentityService _identityService;

    protected Socket _socketIoSocket;

    public BaseEngine()
    {
    }

    //make sure the caller has this wrapped in a transaction
    protected void publishGameWithExtrasToWds(String gameId)
    {
        publishGameWithExtrasToWds(gameId, null, null, null);
    }

    //make sure the caller has this wrapped in a transaction
    protected void publishGameWithExtrasToWds(String gameId, PayoutModel pm, GamePayout gp, GameStats gameStats)
    {
        publishGameWithExtrasToWds(gameId, pm, gp, gameStats, _dao, _shoutContestService);
    }
}
