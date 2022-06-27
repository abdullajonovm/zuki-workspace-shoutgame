package com.meinc.deal.domain;


public class Campaign 
extends BaseCampaign
{
    private static final long serialVersionUID = 1L;
    
    private Sponsor _sponsor;
    private Ad _ad;
    private Deal _deal;

    public Campaign clone(String nameSuffix) 
    {
        Campaign clone = new Campaign();
        baseClone(nameSuffix, clone);
        
        clone.setSponsor(_sponsor);
        clone.setAd(_ad);
        clone.setDeal(_deal.clone(nameSuffix));
        
        return clone;
    }
    
    public Sponsor getSponsor() {
        return _sponsor;
    }
    
    public void setSponsor(Sponsor sponsor) {
        _sponsor = sponsor;
    }
    
    public Ad getAd() {
        return _ad;
    }
    
    public void setAd(Ad ad) {
        _ad = ad;
    }
    
    public Deal getDeal() {
        return _deal;
    }
    
    public void setDeal(Deal deal) {
        _deal = deal;
    }

}
