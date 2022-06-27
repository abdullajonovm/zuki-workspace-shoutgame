package tv.shout.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.Subscriber.ROLE;

public class MockIdentityDao
{
    private List<Subscriber> _subscribers = new ArrayList<>();

    int getNextAutoIncrementSubscriberId()
    {
        return _subscribers.size() + 1; //use 1-based auto increment id so the first one doesn't start at 0
    }

    Subscriber getSubscriberById(long subscriberId)
    {
        return _subscribers.stream()
                .filter(s -> s.getSubscriberId() == subscriberId)
                .findFirst()
                .orElse(null);
    }

    void addSubscriber(Subscriber s)
    {
        _subscribers.add(s);
    }

    List<Long> getSubscriberIdsForRole(ROLE role)
    {
        return _subscribers.stream()
                .filter(s -> s.getRole() == role)
                .map(s -> s.getSubscriberId())
                .collect(Collectors.toList());
    }
}
