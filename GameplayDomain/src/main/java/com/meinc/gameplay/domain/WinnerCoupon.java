package com.meinc.gameplay.domain;

import java.io.Serializable;

public class WinnerCoupon 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public double price;
    public String offerText;
    
    public WinnerCoupon()
    {
    }
    
    public WinnerCoupon(double price, String offerText)
    {
        this.price = price;
        this.offerText = offerText;
    }

}