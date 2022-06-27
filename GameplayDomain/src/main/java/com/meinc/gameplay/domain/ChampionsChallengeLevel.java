package com.meinc.gameplay.domain;

import java.io.Serializable;

public class ChampionsChallengeLevel 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1010;
    private Integer ccLevelId;
    private Integer ccId;
    private Integer level;
    private Integer firstEventId;
    private Integer contestTemplateId;
    private Integer contestTemplateCampaignId;
//    private Date createdDate;
//    private Date updatedDate;
    
    public Integer getCcLevelId() {
        return ccLevelId;
    }

    public void setCcLevelId(Integer ccLevelId) {
        this.ccLevelId = ccLevelId;
    }

    public Integer getCcId() {
        return ccId;
    }

    public void setCcId(Integer ccId) {
        this.ccId = ccId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getFirstEventId() {
        return firstEventId;
    }

    public void setFirstEventId(Integer firstEventId) {
        this.firstEventId = firstEventId;
    }

    public Integer getContestTemplateId() {
        return contestTemplateId;
    }

    public void setContestTemplateId(Integer contestTemplateId) {
        this.contestTemplateId = contestTemplateId;
    }

    public Integer getContestTemplateCampaignId() {
        return contestTemplateCampaignId;
    }

    public void setContestTemplateCampaignId(Integer contestTemplateCampaignId) {
        this.contestTemplateCampaignId = contestTemplateCampaignId;
    }

//    public Date getCreatedDate() {
//        return createdDate;
//    }
//
//    public void setCreatedDate(Date createdDate) {
//        this.createdDate = createdDate;
//    }
//
//    public Date getUpdatedDate() {
//        return updatedDate;
//    }
//
//    public void setUpdatedDate(Date updatedDate) {
//        this.updatedDate = updatedDate;
//    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(ccLevelId);
        buf.append(", ccId: ").append(ccId);
        buf.append(", level: ").append(level);
        buf.append(", firstEventId: ").append(firstEventId);
        buf.append(", contestTemplateId: ").append(contestTemplateId);
        
        return buf.toString();
    }
}
