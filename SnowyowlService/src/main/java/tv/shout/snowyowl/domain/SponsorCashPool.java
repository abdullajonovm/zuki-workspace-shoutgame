package tv.shout.snowyowl.domain;

import java.io.Serializable;

public class SponsorCashPool
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _sponsorCashPoolId;
    private long _subscriberId;
    private double _amount;

    public int getSponsorCashPoolId()
    {
        return _sponsorCashPoolId;
    }
    public void setSponsorCashPoolId(int sponsorCashPoolId)
    {
        _sponsorCashPoolId = sponsorCashPoolId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public double getAmount()
    {
        return _amount;
    }
    public void setAmount(double amount)
    {
        _amount = amount;
    }
}
