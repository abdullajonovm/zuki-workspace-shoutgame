package tv.shout.reactive;

import java.util.List;

import tv.shout.snowyowl.common.SubscriberStatsHandler;
import tv.shout.snowyowl.domain.SubscriberStats;

public class MockSubscriberStatsHandler
extends SubscriberStatsHandler
{
//    private static Logger _logger = Logger.getLogger(MockSubscriberStatsHandler.class);

    @Override
    public void incrementSubscriberStat(long subscriberId, SubscriberStats.STATS_TYPE statsType, int incrementBy)
    {
        //no-op
//        _logger.debug(MessageFormat.format("incrementSubscriberStat. sId: {0,number,#}, type: {1}, amount: {2}", subscriberId, statsType, incrementBy));
    }

    @Override
    public void publishSubscriberStatsDocuments(String gameId, List<Long> subscriberIds)
    {
        //no-op
    }
}
