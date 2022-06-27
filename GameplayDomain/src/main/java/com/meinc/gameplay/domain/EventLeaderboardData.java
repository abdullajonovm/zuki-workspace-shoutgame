package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventLeaderboardData 
implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    /** if true, indicates that this is an "empty" or "placeholder" leaderboard while the real leaderboard is being calculated */
    private boolean _empty = true;
    private List<Leader> _leaders;
    private List<VipBoxScore> _vipBoxScores;
    
    public EventLeaderboardData()
    {
    }

    public List<Leader> getLeaders()
    {
        return _leaders;
    }

    public void setLeaders(List<Leader> leaders)
    {
        _leaders = leaders;
    }

    public List<VipBoxScore> getVipBoxScores()
    {
        return _vipBoxScores;
    }

    public void setVipBoxScores(List<VipBoxScore> vipBoxScores)
    {
        _vipBoxScores = vipBoxScores;
    }
    
    public boolean isEmpty()
    {
        return _empty;
    }

    public void setEmpty(boolean empty)
    {
        _empty = empty;
    }

    @Override
    public EventLeaderboardData clone() {
        EventLeaderboardData clone = new EventLeaderboardData();
        clone._leaders = new ArrayList<Leader>(_leaders);
        clone._vipBoxScores = new ArrayList<VipBoxScore>(_vipBoxScores);
        clone._empty = _empty;
        return clone;
    }
    
    @Override
    public String toString() {
        return String.format("Leaders=%s VipBoxScores=%s", _leaders, _vipBoxScores);
    }
}
