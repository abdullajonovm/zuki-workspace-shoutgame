package com.meinc.gameplay.domain;

import java.io.Serializable;

public class SubscriberAnswerAbbreviated
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private int answerId;
    private Integer powerupTypeId;
    
    public SubscriberAnswerAbbreviated()
    {
    }

    public int getSubscriberId()
    {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId)
    {
        this.subscriberId = subscriberId;
    }

    public int getAnswerId()
    {
        return answerId;
    }

    public void setAnswerId(int answerId)
    {
        this.answerId = answerId;
    }

    public Integer getPowerupTypeId()
    {
        return powerupTypeId;
    }

    public void setPowerupTypeId(Integer powerupTypeId)
    {
        this.powerupTypeId = powerupTypeId;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append(answerId).append("|").append( (powerupTypeId == null ? "0" : powerupTypeId) );
        
        return buf.toString();
    }
    
    public static SubscriberAnswerAbbreviated fromString(String s)
    {
        SubscriberAnswerAbbreviated a = new SubscriberAnswerAbbreviated();
        String[] vals = s.split("\\|");
        a.answerId = Integer.parseInt(vals[0]);
        Integer x = Integer.parseInt(vals[1]);
        if (x != 0) a.powerupTypeId = Integer.parseInt(vals[1]);
        return a;
    }
    
}
