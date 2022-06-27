package com.meinc.gameplay.powerup;

import java.io.Serializable;
import java.util.List;

import com.meinc.store.domain.ItemPrice;

public class PowerupCredsBundleInfo 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private double price; //default "USD" price
    private List<ItemPrice> prices; //prices based on currencyCode
    private String itemId; //uuid
    private int savingsPercent; //ex: 18 = "18%)
    private int numCoins;
    private List<PowerupTypeAmount> powerupTypesAndAmounts;
    
    public double getPrice()
    {
        return price;
    }
    public void setPrice(double price)
    {
        this.price = price;
    }
    public List<ItemPrice> getPrices()
    {
        return prices;
    }
    public void setPrices(List<ItemPrice> prices)
    {
        this.prices = prices;
    }
    public String getItemId()
    {
        return itemId;
    }
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }
    public int getSavingsPercent()
    {
        return savingsPercent;
    }
    public void setSavingsPercent(int savingsPercent)
    {
        this.savingsPercent = savingsPercent;
    }
    public int getNumCoins()
    {
        return numCoins;
    }
    public void setNumCoins(int numCoins)
    {
        this.numCoins = numCoins;
    }
    public List<PowerupTypeAmount> getPowerupTypesAndAmounts()
    {
        return powerupTypesAndAmounts;
    }
    public void setPowerupTypesAndAmounts(List<PowerupTypeAmount> powerupTypesAndAmounts)
    {
        this.powerupTypesAndAmounts = powerupTypesAndAmounts;
    }

}
