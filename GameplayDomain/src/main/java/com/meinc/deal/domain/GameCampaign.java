package com.meinc.deal.domain;

import java.io.Serializable;

public class GameCampaign implements Serializable {
    
    public static final int TYPE_CONSOLATION = 1;
    public static final int TYPE_LEADERBOARD = 2;
    
    /**
     * 
     */
    private static final long serialVersionUID = -1532660822186580319L;
    
    private int _gameId;
    private int _type;
    private Campaign _campaign;
    
    public int getGameId() {
        return _gameId;
    }
    
    public void setGameId(int gameId) {
        _gameId = gameId;
    }
    
    public int getType() {
        return _type;
    }
    
    public void setType(int type) {
        _type = type;
    }
    
    public Campaign getCampaign() {
        return _campaign;
    }
    
    public void setCampaign(Campaign campaign) {
        _campaign = campaign;
    }

}
