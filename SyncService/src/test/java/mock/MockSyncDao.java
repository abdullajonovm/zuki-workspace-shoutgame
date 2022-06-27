package mock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import tv.shout.sync.dao.ISyncServiceDao;
import tv.shout.sync.domain.SyncMessage;

public class MockSyncDao
implements ISyncServiceDao
{
    private List<SyncMessage> _syncMessagesList;

    public MockSyncDao()
    {
        _syncMessagesList = new ArrayList<>();
    }

    @Override
    public void insertSyncMessage(SyncMessage message)
    {
        _syncMessagesList.add(message);
        //return message;
    }

    @Override
    public List<SyncMessage> getSyncMessagesWithContext(String contextualId, long subscriberId, Date since)
    {
        return _syncMessagesList.stream()
                .filter(m -> m.getContextualId().equals(contextualId))
                .filter(m -> m.getSubscriberId() == subscriberId)
                .filter(m -> m.getCreateDate().after(since))
                .sorted(Comparator.comparing( SyncMessage::getCreateDate, Comparator.naturalOrder() ))
                .collect(Collectors.toList());
    }

    @Override
    public List<SyncMessage> getSyncMessages(long subscriberId, Date since)
    {
        return _syncMessagesList.stream()
                .filter(m -> m.getSubscriberId() == subscriberId)
                .filter(m -> m.getCreateDate().after(since))
                .sorted(Comparator.comparing( SyncMessage::getCreateDate, Comparator.naturalOrder() ))
                .collect(Collectors.toList());
    }

}
