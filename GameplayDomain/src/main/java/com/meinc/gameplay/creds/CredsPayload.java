package com.meinc.gameplay.creds;

import java.io.Serializable;
import java.text.MessageFormat;

public class CredsPayload 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 1 time reward for a subscriber answering their very first question */ 
    public static final String KEY_ROOKIE = "CREDS.ROOKIE";
    
    /** recurring reward every time a subscriber answers every question of an event */
    public static final String KEY_GAMER = "CREDS.GAMER";
    
    /** recurring reward every time a subscriber wins in their CC One on One VIP Box */
    public static final String KEY_1v1Winner = "CREDS.1v1.WINNER";
    
    /** recurring reward whenever a subscriber places in the top 50 on the main leaderboard of an event */
    public static final String KEY_TOP50Winner = "CREDS.TOP50.WINNER";

    private int subscriberId;
    private int eventId;
    private Integer rank;
    private int credsAmount;
    private int giftReasonId;
    private String message;
    
    private CredsPayload()
    {
    }
    
    public int getSubscriberId()
    {
        return subscriberId;
    }
    public int getEventId()
    {
        return eventId;
    }
    public Integer getRank()
    {
        return rank;
    }
    public int getCredsAmount()
    {
        return credsAmount;
    }
    public int getGiftReasonId()
    {
        return giftReasonId;
    }
    public String getMessage()
    {
        return message;
    }

    public static CredsPayload getPayload(int subscriberId, int eventId, Integer rank, int credsAmount, int giftReasonId, String message)
    {
        CredsPayload cp = new CredsPayload();
        cp.subscriberId = subscriberId;
        cp.eventId = eventId;
        cp.rank = rank;
        cp.credsAmount = credsAmount;
        cp.giftReasonId = giftReasonId;
        cp.message = message;
        
        return cp;
    }
    
    @Override
    public String toString()
    {
        return MessageFormat.format(
            "subscriberId: {0}, eventId: {1}, rank: {2}, amount: {3}, reason: {4}, message: {5}", 
            subscriberId, eventId, rank, credsAmount, giftReasonId, message);
    }

}
