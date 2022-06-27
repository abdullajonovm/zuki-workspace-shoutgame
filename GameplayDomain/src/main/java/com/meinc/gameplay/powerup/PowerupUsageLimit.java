package com.meinc.gameplay.powerup;

import java.io.Serializable;

public class PowerupUsageLimit 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static enum SCOPE {EVENT, CC}

    private Powerup.POWERUP_TYPE _type;
    private int _limit;
    private SCOPE _scope;
    private boolean _combo;
    
    public Powerup.POWERUP_TYPE getType()
    {
        return _type;
    }
    public void setType(Powerup.POWERUP_TYPE type)
    {
        _type = type;
    }
    public int getLimit()
    {
        return _limit;
    }
    public void setLimit(int limit)
    {
        _limit = limit;
    }
    public SCOPE getScope()
    {
        return _scope;
    }
    public void setScope(SCOPE scope)
    {
        _scope = scope;
    }
    public boolean isCombo()
    {
        return _combo;
    }
    public void setCombo(boolean combo)
    {
        _combo = combo;
    }
    
    
}
