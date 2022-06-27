package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestTemplatePayoutRule implements Serializable
{
    private static final long serialVersionUID = -4927875301158520806L;
    
    private int _contestTemplatePayoutRuleId;
    private int _contestTemplateId;
    private int _payoutRuleId;
    private String _config;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestTemplatePayoutRule()
    {
    }

    public ContestTemplatePayoutRule(int contestTemplateId, int payoutRuleId, String config)
    {
        _contestTemplateId = contestTemplateId;
        _payoutRuleId = payoutRuleId;
        _config = config;
    }

    public int getContestTemplatePayoutRuleId()
    {
        return _contestTemplatePayoutRuleId;
    }

    public void setContestTemplatePayoutRuleId(int contestTemplatePayoutRuleId)
    {
        _contestTemplatePayoutRuleId = contestTemplatePayoutRuleId;
    }

    public int getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(int contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
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
        
        sb.append("ContestPayoutRule{contestTemplatePayoutRuleId: ").append(_contestTemplatePayoutRuleId).append(", contestTemplateId: ").append(_contestTemplateId);
        sb.append(", payoutRuleId: ").append(_payoutRuleId).append(", config: ").append(_config).append("}");
        
        return sb.toString();
    }
}
