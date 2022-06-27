package com.meinc.gameplay.domain;

public class PendingAction 
extends BaseDomainObject
{
    private static final long serialVersionUID = 1L;
    
    public static final String TYPE_JOIN_VIPBOX = "JOIN_VIP_BOX_INVITATION";
    public static final String TYPE_JOIN_SHOUT = "JOIN_SHOUT_INVITATION";
    public static final String TYPE_WON_COUPON = "WON_COUPON";
    
    private int _pendingActionId;
    private int _originatingSubscriberId;
    private int _externalSomeoneId;
    private String _actionType;
    private String _payload;

    public PendingAction()
    {
    }

    public int getPendingActionId()
    {
        return _pendingActionId;
    }

    public void setPendingActionId(int pendingActionId)
    {
        _pendingActionId = pendingActionId;
    }

    public int getOriginatingSubscriberId()
    {
        return _originatingSubscriberId;
    }

    public void setOriginatingSubscriberId(int originatingSubscriberId)
    {
        _originatingSubscriberId = originatingSubscriberId;
    }

    public int getExternalSomeoneId()
    {
        return _externalSomeoneId;
    }

    public void setExternalSomeoneId(int externalSomeoneId)
    {
        _externalSomeoneId = externalSomeoneId;
    }

    public String getActionType()
    {
        return _actionType;
    }

    public void setActionType(String actionType)
    {
        _actionType = actionType;
    }

    public void setPayload(String payload)
    {
        _payload = payload;
    }

    public String getPayload()
    {
        return _payload;
    }
    
}
