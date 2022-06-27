package com.meinc.gameplay.domain;

import java.io.Serializable;

public class SubscriberProfile
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int _xp;
    private int _points;
    
    public int getXp()
    {
        return _xp;
    }
    public void setXp(int xp)
    {
        _xp = xp;
    }
    public int getPoints()
    {
        return _points;
    }
    public void setPoints(int points)
    {
        _points = points;
    }
}
