package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ChampionsChallenge1On1 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer cc1On1Id;
    private Integer ccLevelId;
    private int vipboxId;
    
    private Integer ccId;
    private Integer eventId;
    
    private VipBox oneOnOneVipbox;
    private List<ChampionsChallenge1On1Result> cc1On1Results;
    private Date createdDate;
    private Date updatedDate;
    
    public Integer getCc1On1Id() {
        return cc1On1Id;
    }

    public void setCc1On1Id(Integer cc1On1Id) {
        this.cc1On1Id = cc1On1Id;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Integer getCcLevelId() {
        return ccLevelId;
    }

    public void setCcLevelId(Integer ccLevelId) {
        this.ccLevelId = ccLevelId;
    }

    public int getVipboxId() {
        return vipboxId;
    }

    public void setVipboxId(int vipboxId) {
        this.vipboxId = vipboxId;
    }

    public Integer getCcId() {
        return ccId;
    }

    public void setCcId(Integer ccId) {
        this.ccId = ccId;
    }

    public VipBox getOneOnOneVipbox() {
        return oneOnOneVipbox;
    }

    public void setOneOnOneVipbox(VipBox oneOnOneVipbox) {
        this.oneOnOneVipbox = oneOnOneVipbox;
    }

    public List<ChampionsChallenge1On1Result> getCc1On1Results() {
        return cc1On1Results;
    }

    public void setCc1On1Results(List<ChampionsChallenge1On1Result> cc1On1Results) {
        this.cc1On1Results = cc1On1Results;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}