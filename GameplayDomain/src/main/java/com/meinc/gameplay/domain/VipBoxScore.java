package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A struct containing summary information about a VIP Box from an aggregate scoring perspective.
 */
public class VipBoxScore 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _vipBoxId;
    private int _score;
    private String _vipBoxName;
    private int _numPlayers;
    
    //TODO: make a struct that contains all these lists rather than setting them all individually
    //private List<VipBoxScoreWinnerData> _winnerData;
        //subscriberId, amount, nickname, email
    private List<Integer> _winnerSubscriberIds = new ArrayList<Integer>();
    private List<Integer> _winnerAmounts = new ArrayList<Integer>();
    private List<String> _winnerNicknames = new ArrayList<String>();
    private List<String> _winnerEmails = new ArrayList<String>();
    
    public VipBoxScore()
    {
    }
    
    public VipBoxScore(int vipBoxId, int score, String vipBoxName, int numPlayers)
    {
        _vipBoxId = vipBoxId;
        _score = score;
        _vipBoxName = vipBoxName;
        _numPlayers = numPlayers;
    }

    @JsonProperty("id")
    public int getVipBoxId()
    {
        return _vipBoxId;
    }

    public void setVipBoxId(int vipBoxId)
    {
        _vipBoxId = vipBoxId;
    }

    public int getScore()
    {
        return _score;
    }

    public void setScore(int score)
    {
        _score = score;
    }

    @JsonProperty("name")
    public String getVipBoxName()
    {
        return _vipBoxName;
    }

    public void setVipBoxName(String vipBoxName)
    {
        _vipBoxName = vipBoxName;
    }

    public int getNumPlayers()
    {
        return _numPlayers;
    }

    public void setNumPlayers(int numPlayers)
    {
        _numPlayers = numPlayers;
    }

    public List<Integer> getWinnerSubscriberIds()
    {
        return _winnerSubscriberIds;
    }

    public void setWinnerSubscriberIds(List<Integer> winnerSubscriberIds)
    {
        _winnerSubscriberIds = winnerSubscriberIds;
    }
    
    public void addWinnerSubscriberId(int winnerSubscriberId)
    {
        _winnerSubscriberIds.add(winnerSubscriberId);
    }

    public List<Integer> getWinnerAmounts()
    {
        return _winnerAmounts;
    }

    public void setWinnerAmounts(List<Integer> winnerAmounts)
    {
        _winnerAmounts = winnerAmounts;
    }
    
    public void addWinnerAmount(int amount)
    {
        _winnerAmounts.add(amount);
    }
    
    public void setWinnerNicknames(List<String> winnerNicknames)
    {
        _winnerNicknames = winnerNicknames;
    }
    
    public List<String> getWinnerNicknames()
    {
        return _winnerNicknames;
    }

    public List<String> getWinnerEmails()
    {
        return _winnerEmails;
    }

    public void setWinnerEmails(List<String> winnerEmails)
    {
        _winnerEmails = winnerEmails;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(_vipBoxId).append(", ");
        buf.append("score: ").append(_score).append(", ");
        buf.append("name: ").append(_vipBoxName).append(", ");
        buf.append("#players: ").append(_numPlayers).append("\n");
        buf.append("\tVIP BOX WINNERS:");
        
        for (int i=0; i<_winnerSubscriberIds.size(); i++) {
            buf.append("\n\t\t");
            buf.append("id: ").append(_winnerSubscriberIds.get(i)).append(", ");
            buf.append("amount: ").append(_winnerAmounts.get(i)).append(", ");
            buf.append("nickname: ").append(_winnerNicknames.get(i)).append(", ");
            buf.append("email: ").append(_winnerEmails.get(i)).append(", ");
        }
        
        return buf.toString();
    }
    
}
