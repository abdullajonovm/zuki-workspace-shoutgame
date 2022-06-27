package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class SubscriberXpHistory 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int _xp;
    private String _type;
    
    /**
     * The contextId varies based on the type.
     * <ul>
     *   <li>QUESTION - quesitonId</li>
     *   <li>LB_TOP_THIRD - eventId</li>
     *   <li>LB_TOP_ONE_PERCENT - eventId</li>
     *   <li>ONE_ON_ONE_WINNER - vipboxId</li>
     * </ul>
     */
    private Integer _contextId;
    
    private Date _awardedDate;
    
    public int getXp()
    {
        return _xp;
    }
    public void setXp(int xp)
    {
        _xp = xp;
    }
    public String getType()
    {
        return _type;
    }
    public void setType(String type)
    {
        _type = type;
    }
    public Integer getContextId()
    {
        return _contextId;
    }
    public void setContextId(Integer contextId)
    {
        _contextId = contextId;
    }
    public Date getAwardedDate()
    {
        return _awardedDate;
    }
    public void setAwardedDate(Date awardedDate)
    {
        _awardedDate = awardedDate;
    }
}
