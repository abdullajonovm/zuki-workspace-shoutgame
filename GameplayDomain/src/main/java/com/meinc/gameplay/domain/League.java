package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A league (NFL, NBA, etc..).  Used when creating Events. 
 */
public class League 
implements Serializable
{
    private static final long serialVersionUID = -6071831806149186244L;
    private int _id;
    private String _name;
    private boolean _active;
    private List<League> _children;
    private List<Team> _teams;
    
    public League()
    {
        _children = new ArrayList<League>();
        _teams = new ArrayList<Team>();
    }
    
    public int getId() 
    {
        return _id;
    }
    
    public void setId(int id) 
    {
        _id = id;
    }
    
    public String getName() 
    {
        return _name;
    }
    
    public void setName(String name) 
    {
        _name = name;
    }
    
    public void addLeague(League child)
    {
        _children.add(child);
    }
    
    public List<League> getLeagues()
    {
        return _children;
    }

    public void setTeams(List<Team> teams)
    {
        _teams = teams;
    }
    
    public List<Team> getTeams()
    {
        return _teams;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public boolean isActive()
    {
        return _active;
    }
}
