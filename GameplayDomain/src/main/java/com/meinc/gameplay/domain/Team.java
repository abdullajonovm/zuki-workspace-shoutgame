package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1008;
    
    private int _teamId;
    private int _parentId;
    private String _name;
    private boolean _active;
    private List<Metadata> _metadata;

    public Team()
    {
        _metadata = new ArrayList<Metadata>();
    }

    public int getTeamId()
    {
        return _teamId;
    }

    public void setTeamId(int teamId)
    {
        _teamId = teamId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public List<Metadata> getMetadata()
    {
        return _metadata;
    }

    public void setMetadata(List<Metadata> metadata)
    {
        _metadata = metadata;
    }

    public void setParentId(int parentId)
    {
        _parentId = parentId;
    }

    public int getParentId()
    {
        return _parentId;
    }
    
    public void setActive(boolean active)
    {
        _active = active;
    }

    public boolean isActive()
    {
        return _active;
    }
    
    @Override
    public String toString()
    {
        return _name;
    }
}
