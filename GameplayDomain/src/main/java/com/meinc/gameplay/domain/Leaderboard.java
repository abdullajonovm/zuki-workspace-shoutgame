package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class Leaderboard
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int _eventId;
    private Integer _vipBoxId;
    private Leader _subscriberLeader;
    private EventLeaderboardData _leaders;
    private Date _lastUpdated;
    private boolean _isEmpty;
    
    public Leaderboard(int eventId, EventLeaderboardData leaders, Leader subscriberLeader, Integer vipBoxId, Date lastUpdated, boolean isEmpty)
    {
        _eventId = eventId;
        _leaders = leaders;
        _subscriberLeader = subscriberLeader;
        _vipBoxId = vipBoxId;
        _lastUpdated = lastUpdated;
        _isEmpty = isEmpty;
    }

    public void setSubscriberLeader(Leader subscriberLeader)
    {
        _subscriberLeader = subscriberLeader;
    }

    public Leader getSubscriberLeader()
    {
        return _subscriberLeader;
    }

    public void setLeaders(EventLeaderboardData leaders)
    {
        _leaders = leaders;
    }

    public EventLeaderboardData getLeaders()
    {
        return _leaders;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public Integer getVipBoxId() {
        return _vipBoxId;
    }

    public void setVipBoxId(Integer vipBoxId) {
        _vipBoxId = vipBoxId;
    }

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdate) {
        _lastUpdated = lastUpdate;
    }

    public boolean isEmpty() {
        return _isEmpty;
    }

    public void setEmpty(boolean isEmpty) {
        _isEmpty = isEmpty;
    }

}
