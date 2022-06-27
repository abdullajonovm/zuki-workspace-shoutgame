package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LeagueHeirarchy 
implements Serializable
{
    private static final long serialVersionUID = 507245219894987438L;
    private int _leagueGroupId;
    private String _name;
    private List<League> _leagues;
    
    public LeagueHeirarchy()
    {
        _leagues = new ArrayList<League>();
    }

    public int getLeagueGroupId()
    {
        return _leagueGroupId;
    }

    public void setLeagueGroupId(int leagueGroupId)
    {
        _leagueGroupId = leagueGroupId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public List<League> getLeagues()
    {
        return _leagues;
    }

    public void setLeagues(List<League> leagues)
    {
        _leagues = leagues;
    }
    
    public void addLeague(League league)
    {
        _leagues.add(league);
    }
}
