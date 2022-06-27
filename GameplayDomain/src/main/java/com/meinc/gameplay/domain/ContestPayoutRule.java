package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestPayoutRule implements Serializable
{
    private static final long serialVersionUID = 2433859665439311199L;
    
    private int _contestPayoutRuleId;
    private int _contestId;
    private int _payoutRuleId;
    private String _config;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestPayoutRule()
    {
    }

    public ContestPayoutRule(int contestId, int payoutRuleId, String config)
    {
        _contestId = contestId;
        _payoutRuleId = payoutRuleId;
        _config = config;
    }

    public int getContestPayoutRuleId()
    {
        return _contestPayoutRuleId;
    }

    public void setContestPayoutRuleId(int contestPayoutRuleId)
    {
        _contestPayoutRuleId = contestPayoutRuleId;
    }

    public int getContestId()
    {
        return _contestId;
    }

    public void setContestId(int contestId)
    {
        _contestId = contestId;
    }
    
    public int getPayoutRuleId()
    {
        return _payoutRuleId;
    }

    public void setPayoutRuleId(int payoutRuleId)
    {
        _payoutRuleId = payoutRuleId;
    }

    public String getConfig()
    {
        return _config;
    }

    public void setConfig(String config)
    {
        _config = config;
    }

    public Date getCreatedDate()
    {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("ContestPayoutRule{contestPayoutRuleId: ").append(_contestPayoutRuleId).append(", contestId: ").append(_contestId);
        sb.append(", payoutRuleId: ").append(_payoutRuleId).append(", config: ").append(_config).append("}");
        
        return sb.toString();
    }
}
