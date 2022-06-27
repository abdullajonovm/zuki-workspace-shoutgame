package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ContestTemplateCampaign implements Serializable
{
    private static final long serialVersionUID = -7189406429261725410L;
    
    private int _contestTemplateCampaignId;
    private int _contestTemplateId;
    private int _campaignId;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public ContestTemplateCampaign()
    {
    }

    public ContestTemplateCampaign(int contestTemplateId, int campaignId)
    {
        _contestTemplateId = contestTemplateId;
        _campaignId = campaignId;
    }
    
    public int getContestTemplateCampaignId()
    {
        return _contestTemplateCampaignId;
    }

    public void setContestTemplateCampaignId(int contestTemplateCampaignId)
    {
        _contestTemplateCampaignId = contestTemplateCampaignId;
    }

    public int getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(int contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
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
        
        sb.append("ContestTemplateCampaign{contestTemplateCampaignId: ").append(_contestTemplateCampaignId).append(", contestTemplateId: ").append(_contestTemplateId);
        sb.append(", campaignId: ").append(_campaignId).append("}");
        
        return sb.toString();
    }
}
