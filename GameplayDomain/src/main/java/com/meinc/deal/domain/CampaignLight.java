package com.meinc.deal.domain;


public class CampaignLight 
extends BaseCampaign
{
    private static final long serialVersionUID = 1L;
    
    private int _sponsorId;
    private int _adId;
    private int _dealId;

    public CampaignLight clone(String nameSuffix) 
    {
        CampaignLight clone = new CampaignLight();
        baseClone(nameSuffix, clone);
        
        clone.setSponsorId(_sponsorId);
        clone.setAdId(_adId);
        clone.setDealId(_dealId);
        
        return clone;
    }
    
    public int getSponsorId()
    {
        return _sponsorId;
    }

    public void setSponsorId(int sponsorId)
    {
        _sponsorId = sponsorId;
    }

    public int getAdId()
    {
        return _adId;
    }

    public void setAdId(int adId)
    {
        _adId = adId;
    }

    public int getDealId()
    {
        return _dealId;
    }

    public void setDealId(int dealId)
    {
        _dealId = dealId;
    }
    
}
