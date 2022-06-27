package com.meinc.gameplay.domain;

import java.io.Serializable;

public class EventCcWinnerRow 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public int ccLevelNumber;
    public int cc1on1Id; //PK
    public int eventId;
    public int subscriberId; //PK
    public String nickname;
    public int couponInstanceId;
    public WinnerCoupon coupon;
    
    public EventCcWinnerRow()
    {
    }
    
    public EventCcWinnerRow(int ccLevelNumber, int cc1on1Id, int eventId, int subscriberId, int couponInstanceId)
    {
        this.ccLevelNumber = ccLevelNumber;
        this.cc1on1Id = cc1on1Id;
        this.eventId = eventId;
        this.subscriberId = subscriberId;
        this.couponInstanceId = couponInstanceId;
    }    
}
