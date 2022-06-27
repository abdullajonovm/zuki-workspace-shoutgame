package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestTemplateScoringRule implements Serializable
{
    private static final long serialVersionUID = -821472097502703172L;
    
    private int _contestTemplateScoringRuleId;
    private int _contestTemplateId;
    private int _scoringRuleId;
    private String _config;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestTemplateScoringRule()
    {
    }

    public ContestTemplateScoringRule(int contestTemplateId, int scoringRuleId, String config)
    {
        _contestTemplateId = contestTemplateId;
        _scoringRuleId = scoringRuleId;
        _config = config;
    }

    public int getContestTemplateScoringRuleId()
    {
        return _contestTemplateScoringRuleId;
    }

    public void setContestTemplateScoringRuleId(int contestTemplateScoringRuleId)
    {
        _contestTemplateScoringRuleId = contestTemplateScoringRuleId;
    }

    public int getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(int contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
    }
    
    public int getScoringRuleId()
    {
        return _scoringRuleId;
    }

    public void setScoringRuleId(int scoringRuleId)
    {
        _scoringRuleId = scoringRuleId;
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
        
        sb.append("ContestScoringRule{contestTemplateScoringRuleId: ").append(_contestTemplateScoringRuleId).append(", contestTemplateId: ").append(_contestTemplateId);
        sb.append(", scoringRuleId: ").append(_scoringRuleId).append(", config: ").append(_config).append("}");
        
        return sb.toString();
    }
}
