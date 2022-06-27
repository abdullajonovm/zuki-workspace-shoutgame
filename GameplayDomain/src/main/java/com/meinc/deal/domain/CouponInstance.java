package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

public class CouponInstance 
implements Serializable 
{
    public static enum COUPON_TYPE {UNKNOWN, QUESTION, CC, LEADERBOARD, FIFTY_PERCENT};
    
    private static final long serialVersionUID = 1863483787036246538L;
    
    private int _id;
    private int _subscriberId;
    private COUPON_TYPE _couponType = COUPON_TYPE.UNKNOWN;
    private String _redeemUrl;
    private String _couponCode;
    private Date _dateClaimed;
    private Date _expirationDate;
    private Date _campaignExpirationDate;
    private Date _dateRedeemed;
    private Date _dateFulfilled;
    private Coupon _coupon;
    
    @JsonProperty(value="couponInstanceId")
    public int getId() {
        return _id;
    }

    @JsonProperty(value="couponInstanceId")
    public void setId(int id) {
        _id = id;
    }
 
    public int getSubscriberId() {
        return _subscriberId;
    }
    
    public void setSubscriberId(int subscriberId) {
        _subscriberId = subscriberId;
    }
    
    public COUPON_TYPE getCouponType()
    {
        return _couponType;
    }

    public void setCouponType(COUPON_TYPE couponType)
    {
        _couponType = couponType;
    }

    public String getRedeemUrl() {
        return _redeemUrl;
    }
    
    public void setRedeemUrl(String redeemUrl) {
        _redeemUrl = redeemUrl;
    }
    
    public String getCouponCode() {
        return _couponCode;
    }

    public void setCouponCode(String couponCode) {
        _couponCode = couponCode;
    }

    public Date getDateClaimed() {
        return _dateClaimed;
    }
    
    public void setDateClaimed(Date dateClaimed) {
        _dateClaimed = dateClaimed;
    }
    
    public Date getExpirationDate()
    {
        return _expirationDate;
    }

    public void setExpirationDate(Date expirationDate)
    {
        _expirationDate = expirationDate;
    }

    public Date getCampaignExpirationDate()
    {
        return _campaignExpirationDate;
    }

    public void setCampaignExpirationDate(Date campaignExpirationDate)
    {
        _campaignExpirationDate = campaignExpirationDate;
    }

    public Date getDateRedeemed() {
        return _dateRedeemed;
    }
    
    public Date getDateFulfilled()
    {
        return _dateFulfilled;
    }

    public void setDateFulfilled(Date dateFulfilled)
    {
        _dateFulfilled = dateFulfilled;
    }
    
    public void setDateRedeemed(Date dateRedeemed) {
        _dateRedeemed = dateRedeemed;
    }
    
    public void setCoupon(Coupon coupon) {
        _coupon = coupon;
    }
    
    public Coupon getCoupon() {
        return _coupon;
    }

}
