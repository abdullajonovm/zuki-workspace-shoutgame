package com.meinc.gameplay.domain;

import java.io.Serializable;

public class ChampionsChallengeLevelVipBox implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer ccLevelId;
    private Integer vipboxId;
    
    public Integer getCcLevelId() {
        return ccLevelId;
    }
    
    public void setCcLevelId(Integer ccLevelId) {
        this.ccLevelId = ccLevelId;
    }
    
    public Integer getVipboxId() {
        return vipboxId;
    }
    
    public void setVipboxId(Integer vipboxId) {
        this.vipboxId = vipboxId;
    }
}
