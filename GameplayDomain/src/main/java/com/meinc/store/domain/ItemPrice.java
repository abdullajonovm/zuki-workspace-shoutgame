package com.meinc.store.domain;

import java.io.Serializable;

public class ItemPrice 
implements Serializable
{
    private static final long serialVersionUID = 9081811642549477602L;
    public static final int classId = 3002;
    
    private String _currencyCode;
    private double _price;
    private String _formattedPrice;
    
    public ItemPrice()
    {
        
    }
    
    public ItemPrice(String currencyCode, double price, String formattedPrice)
    {
        _currencyCode = currencyCode;
        _price = price;
        _formattedPrice = formattedPrice;
    }
    
    public String getCurrencyCode()
    {
        return _currencyCode;
    }

    public void setCurrencyCode(String currencyCode)
    {
        _currencyCode = currencyCode;
    }

    public double getPrice()
    {
        return _price;
    }

    public void setPrice(double price)
    {
        _price = price;
    }

    public String getFormattedPrice()
    {
        return _formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice)
    {
        _formattedPrice = formattedPrice;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("currencyCode: ").append(_currencyCode);
        buf.append(", price: ").append(_price);
        buf.append(", formattedPrice: ").append(_formattedPrice);
        
        return buf.toString();
    }
}
