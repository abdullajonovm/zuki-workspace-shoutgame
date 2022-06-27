package tv.shout.snowyowl.domain;

import java.io.Serializable;

public class AffiliatePlan
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _affiliatePlanId;
    private boolean _current;
    private double _affiliateDirectPayoutPct;
    private double _affiliateSecondaryPayoutPct;
    private double _affiliateTertiaryPayoutPct;
    private double _playerInitialPayoutPct;

    public int getAffiliatePlanId()
    {
        return _affiliatePlanId;
    }
    public void setAffiliatePlanId(int affiliatePlanId)
    {
        _affiliatePlanId = affiliatePlanId;
    }
    public boolean isCurrent()
    {
        return _current;
    }
    public void setCurrent(boolean current)
    {
        _current = current;
    }
    public double getAffiliateDirectPayoutPct()
    {
        return _affiliateDirectPayoutPct;
    }
    public void setAffiliateDirectPayoutPct(double affiliateDirectPayoutPct)
    {
        _affiliateDirectPayoutPct = affiliateDirectPayoutPct;
    }
    public double getAffiliateSecondaryPayoutPct()
    {
        return _affiliateSecondaryPayoutPct;
    }
    public void setAffiliateSecondaryPayoutPct(double affiliateSecondaryPayoutPct)
    {
        _affiliateSecondaryPayoutPct = affiliateSecondaryPayoutPct;
    }
    public double getAffiliateTertiaryPayoutPct()
    {
        return _affiliateTertiaryPayoutPct;
    }
    public void setAffiliateTertiaryPayoutPct(double affiliateTertiaryPayoutPct)
    {
        _affiliateTertiaryPayoutPct = affiliateTertiaryPayoutPct;
    }
    public double getPlayerInitialPayoutPct()
    {
        return _playerInitialPayoutPct;
    }
    public void setPlayerInitialPayoutPct(double playerInitialPayoutPct)
    {
        _playerInitialPayoutPct = playerInitialPayoutPct;
    }

}
