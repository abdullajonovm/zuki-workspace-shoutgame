package com.meinc.gameplay.powerup;

import java.io.Serializable;

import com.meinc.gameplay.powerup.Powerup.POWERUP_TYPE;

public class PowerupTypeAmount 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private POWERUP_TYPE type;
    private int quantity;
    
    public POWERUP_TYPE getType()
    {
        return type;
    }
    public void setType(POWERUP_TYPE type)
    {
        this.type = type;
    }
    public int getQuantity()
    {
        return quantity;
    }
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
}
