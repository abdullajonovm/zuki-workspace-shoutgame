package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestCampaign implements Serializable
{
    private static final long serialVersionUID = -7189406429261725410L;
    
    private int _contestCampaignId;
    private int _contestId;
    private int _campaignId;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public ContestCampaign()
    {
    }

    public ContestCampaign(int contestId, int campaignId)
    {
        _contestId = contestId;
        _campaignId = campaignId;
    }
    
    public int getContestCampaignId()
    {
        return _contestCampaignId;
    }

    public void setContestCampaignId(int contestCampaignId)
    {
        _contestCampaignId = contestCampaignId;
    }

    public int getContestId()
    {
        return _contestId;
    }

    public void setContestId(int contestId)
    {
        _contestId = contestId;
    }

    public int getCampaignId()
    {
        return _campaignId;
    }

    public void setCampaignId(int campaignId)
    {
        _campaignId = campaignId;
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
        
        sb.append("ContestCampaign{contestCampaignId: ").append(_contestCampaignId).append(", contestId: ").append(_contestId);
        sb.append(", campaignId: ").append(_campaignId).append("}");
        
        return sb.toString();
    }
}
