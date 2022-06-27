package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestScoringRule implements Serializable
{
    private static final long serialVersionUID = 4082140213665050605L;
    
    private int _contestScoringRuleId;
    private int _contestId;
    private int _scoringRuleId;
    private String _config;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestScoringRule()
    {
    }

    public ContestScoringRule(int contestId, int scoringRuleId, String config)
    {
        _contestId = contestId;
        _scoringRuleId = scoringRuleId;
        _config = config;
    }

    public int getContestScoringRuleId()
    {
        return _contestScoringRuleId;
    }

    public void setContestScoringRuleId(int contestScoringRuleId)
    {
        _contestScoringRuleId = contestScoringRuleId;
    }

    public int getContestId()
    {
        return _contestId;
    }

    public void setContestId(int contestId)
    {
        _contestId = contestId;
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
        
        sb.append("ContestScoringRule{contestScoringRuleId: ").append(_contestScoringRuleId).append(", contestId: ").append(_contestId);
        sb.append(", scoringRuleId: ").append(_scoringRuleId).append(", config: ").append(_config).append("}");
        
        return sb.toString();
    }
}
