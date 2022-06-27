package com.meinc.gameplay.domain;

/**
 * Simple struct to hold subscriberId/childId
 */
public class SubscriberChild
{
    private int _subscriberId;
    private int _childId;
    
    public SubscriberChild(int subscriberId, int childId)
    {
        setSubscriberId(subscriberId);
        setChildId(childId);
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public int getChildId()
    {
        return _childId;
    }

    public void setChildId(int childId)
    {
        _childId = childId;
    }
}
