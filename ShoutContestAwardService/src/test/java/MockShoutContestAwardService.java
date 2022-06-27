import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meinc.trigger.domain.Trigger;

import tv.shout.shoutcontestaward.dao.IShoutContestAwardServiceDao;
//import tv.shout.shoutcontestaward.dao.InMemoryAwardsDao;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.SubscriberStats;
import tv.shout.shoutcontestaward.domain.GamePayout;
import tv.shout.shoutcontestaward.service.IShoutContestAwardService;

public class MockShoutContestAwardService
implements IShoutContestAwardService
{
    private static Logger _logger = Logger.getLogger(MockShoutContestAwardService.class);

    private final List<String> _validTriggerKeys = Arrays.asList(
            IShoutContestAwardService.AWARD_MESSAGE_TRIGGER_KEY
        );

    private Map<Integer, Date> _subscriberSyncHighWaterMarkMap = new HashMap<>();

    private IShoutContestAwardServiceDao _dao = null; //new InMemoryAwardsDao();

    @Override
    public void start()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService starting...");
        }

        _logger.info("SyncService started");
    }

    @Override
    public void stop()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopping...");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("SyncService stopped");
        }
    }

    private boolean isMyTrigger(String triggerKey)
    {
        return _validTriggerKeys.contains(triggerKey);
    }

    @Override
    public boolean processTriggerMessages(Trigger trigger) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<GamePayout> getGamePayoutsForSubscriber(int subscriberId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void redeemGamePayout(int gamePayoutId) {
        // TODO Auto-generated method stub

    }


    @Override
    public List<SubscriberStats> getSubscriberStats(int subscriberId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GameBadge> getBadgesForSubscriber(int subscriberId) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public List<GameInteractionEvent> getEventsForSubscriber(int subscriberId) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public GamePayout createGamePayout(int contextId, Integer subscriberId, String gameId, String roundId, String roundSequence, Double amount) {
        // TODO Auto-generated method stub
        return null;
    }

}
