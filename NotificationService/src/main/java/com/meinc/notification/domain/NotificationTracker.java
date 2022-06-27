package com.meinc.notification.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NotificationTracker implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -376476164520681776L;
    private long _subscriberId;
    private Map<String, Integer> _contextTracker;

    public NotificationTracker() {
        _contextTracker = new HashMap<String, Integer>();
    }

    public long getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        _subscriberId = subscriberId;
    }

    public void incrementContext(String noteType, int contextId) {
        Integer numNotifications = _contextTracker.get(noteType + "_" + contextId);
        if (numNotifications == null) {
            numNotifications = 0;
        }
        _contextTracker.put(noteType + "_" + contextId, numNotifications.intValue() + 1);
    }

    public int getNotificationsForContext(String noteType, int contextId) {
        Integer numNotifications = _contextTracker.get(noteType + "_" + contextId);
        if (numNotifications == null) {
            return 0;
        }
        return numNotifications;
    }
}
