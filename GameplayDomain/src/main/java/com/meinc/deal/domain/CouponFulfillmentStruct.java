package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.Date;

public class CouponFulfillmentStruct 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int _couponInstanceId;
    private CouponInstance.COUPON_TYPE _couponType;
    private Date _couponInstanceExpireDate;
    private Date _campaignExpireDate;
    private Date _redeemedDate;
    private String _instanceCouponCode;
    private String _couponCode;
    private String _offerText;
    private String _email;
    private String _nickname;
    private String _countryCode;
    private String _languageCode;
    
    public int getCouponInstanceId()
    {
        return _couponInstanceId;
    }
    public void setCouponInstanceId(int couponInstanceId)
    {
        _couponInstanceId = couponInstanceId;
    }
    public CouponInstance.COUPON_TYPE getCouponType()
    {
        return _couponType;
    }
    public void setCouponType(CouponInstance.COUPON_TYPE couponType)
    {
        _couponType = couponType;
    }
    public Date getCouponInstanceExpireDate()
    {
        return _couponInstanceExpireDate;
    }
    public void setCouponInstanceExpireDate(Date couponInstanceExpireDate)
    {
        _couponInstanceExpireDate = couponInstanceExpireDate;
    }
    public Date getCampaignExpireDate()
    {
        return _campaignExpireDate;
    }
    public void setCampaignExpireDate(Date campaignExpireDate)
    {
        _campaignExpireDate = campaignExpireDate;
    }
    public Date getRedeemedDate()
    {
        return _redeemedDate;
    }
    public void setRedeemedDate(Date redeemedDate)
    {
        _redeemedDate = redeemedDate;
    }
    public String getInstanceCouponCode()
    {
        return _instanceCouponCode;
    }
    public void setInstanceCouponCode(String instanceCouponCode)
    {
        _instanceCouponCode = instanceCouponCode;
    }
    public String getCouponCode()
    {
        return _couponCode;
    }
    public void setCouponCode(String couponCode)
    {
        _couponCode = couponCode;
    }
    public String getOfferText()
    {
        return _offerText;
    }
    public void setOfferText(String offerText)
    {
        _offerText = offerText;
    }
    public String getEmail()
    {
        return _email;
    }
    public void setEmail(String email)
    {
        _email = email;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public String getCountryCode()
    {
        return _countryCode;
    }
    public void setCountryCode(String countryCode)
    {
        _countryCode = countryCode;
    }
    public String getLanguageCode()
    {
        return _languageCode;
    }
    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }
}
