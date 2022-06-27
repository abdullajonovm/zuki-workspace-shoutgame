package com.meinc.gameplay.powerup;

import java.io.Serializable;
import java.util.List;

public class PowerupPricingInfo 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PowerupPassInfo passFree;
    private PowerupPassInfo pass1Day;
    private PowerupPassInfo pass1Week;
    private PowerupPassInfo pass1Month;
    private PowerupPassInfo passEvent;
    private PowerupCredsBundleInfo bundleSmall;
    private PowerupCredsBundleInfo bundleMedium;
    private PowerupCredsBundleInfo bundleLarge;
    private List<PowerupUsageLimit> usageLimit;
    
    public PowerupPassInfo getPassFree()
    {
        return passFree;
    }
    public void setPassFree(PowerupPassInfo passFree)
    {
        this.passFree = passFree;
    }
    public PowerupPassInfo getPass1Day()
    {
        return pass1Day;
    }
    public void setPass1Day(PowerupPassInfo pass1Day)
    {
        this.pass1Day = pass1Day;
    }
    public PowerupPassInfo getPass1Week()
    {
        return pass1Week;
    }
    public void setPass1Week(PowerupPassInfo pass1Week)
    {
        this.pass1Week = pass1Week;
    }
    public PowerupPassInfo getPass1Month()
    {
        return pass1Month;
    }
    public void setPass1Month(PowerupPassInfo pass1Month)
    {
        this.pass1Month = pass1Month;
    }
    public PowerupPassInfo getPassEvent()
    {
        return passEvent;
    }
    public void setPassEvent(PowerupPassInfo passEvent)
    {
        this.passEvent = passEvent;
    }
    public PowerupCredsBundleInfo getBundleSmall()
    {
        return bundleSmall;
    }
    public void setBundleSmall(PowerupCredsBundleInfo bundleSmall)
    {
        this.bundleSmall = bundleSmall;
    }
    public PowerupCredsBundleInfo getBundleMedium()
    {
        return bundleMedium;
    }
    public void setBundleMedium(PowerupCredsBundleInfo bundleMedium)
    {
        this.bundleMedium = bundleMedium;
    }
    public PowerupCredsBundleInfo getBundleLarge()
    {
        return bundleLarge;
    }
    public void setBundleLarge(PowerupCredsBundleInfo bundleLarge)
    {
        this.bundleLarge = bundleLarge;
    }
    public List<PowerupUsageLimit> getUsageLimit()
    {
        return usageLimit;
    }
    public void setUsageLimit(List<PowerupUsageLimit> usageLimit)
    {
        this.usageLimit = usageLimit;
    }
}
