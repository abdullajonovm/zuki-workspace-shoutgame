package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.gameplay.domain.Localized;

public class Coupon implements Serializable {
    
    public static int URL_FULFILLMENT_TYPE = 0;
    public static int MANUAL_FULFILLMENT_TYPE = 1;
    public static int UNIQUE_URL_FULFILLMENT_TYPE = 2;
    public static int MANUAL_COUPON_CODE_FULFILLMENT_TYPE = 3;
    public static int UNIQUE_MANUAL_COUPON_CODE_FULFILLMENT_TYPE = 4;
    public static int CONCESSION_FULFILLMENT_TYPE = 5;
    public static int IN_VENUE_FULFILLMENT_TYPE = 6;
    public static int IN_STORE_FULFILLMENT_TYPE = 7;
    public static int AUTO_SHOUT_FULFILLMENT = 8;
    
    public static int FULFILLMENT_TYPE_MIN = 0;
    public static int FULFILLMENT_TYPE_MAX = 8;
    
    public static final String AUTO_FULFILL_CODE_POWERUP_PACK = "100";
    public static final String AUTO_FULFILL_CODE_WEEK_PASS = "200";
    public static final String AUTO_FULFILL_CODE_MONTH_PASS = "300";
    public static final String AUTO_FULFILL_CODE_CREDS_SMALL = "400";
    public static final String AUTO_FULFILL_CODE_CREDS_MEDIUM = "500";
    public static final String AUTO_FULFILL_CODE_CREDS_LARGE = "600";
    public static final String AUTO_FULFILL_CODE_MP3 = "700";
    
    private static final long serialVersionUID = 2768109075124144651L;
    private int _id;
    private double  _msrp;
    private int     _totalCoupons;
    private int     _numRemaining;
    private double  _price;
    private boolean _percentOff;
    private Date    _expirationDate;
    private String  _offerText;
    private String _offerTextUuid; 
    private List<Localized> _offerTextLocalized;
    private String  _offerDescription;
    private String _offerDescriptionUuid;
    private List<Localized> _offerDescriptionLocalized;
    private String  _redeemUrl;
    private String  _mobileBannerUrl;
    private String  _webBannerUrl;
    private String  _mobileDealImageUrl;
    private String  _webDealImageUrl;
    private int     _fulfillmentType;
    private String  _fulfillmentMessage;
    private String _fulfillmentMessageUuid;
    private List<Localized> _fulfillmentMessageLocalized;
    private String  _couponCode;
    private String _currencyCode;
    
    //will be one of the AUTO_FULFILL_CODE_* constants
    private String _autoFulfillType;
    
    //1="old" coupons where the AUTO_FULFILL_COUPON_CODE_* constant was stored in the couponCode field, 2="new" where the code is stored in the autoFulfillType field
    private int _version; 
    
    //so non AUTO_SHOUT_FULFILLMENT types can be auto fulfilled as well. in this case, the fulfillment_message_uuid field will contain a ref to json instructions on how to redeem
    // (usually a code and instructions to be displayed to the user)
    private boolean _autoFulfill;
    
    @JsonIgnore
    public Coupon clone() {
        Coupon clone = new Coupon();
        
        clone.setOfferText(_offerText);
        String offerTextUuid = UUID.randomUUID().toString();
        clone.setOfferText(_offerText);
        clone.setOfferTextUuid(offerTextUuid);
        if (_offerTextLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setOfferTextLocalized(newLocalized);
            for (Localized localized : _offerTextLocalized)
            {
                newLocalized.add(new Localized(offerTextUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setOfferDescription(_offerDescription);
        String offerDescriptionUuid = UUID.randomUUID().toString();
        clone.setOfferDescriptionUuid(offerDescriptionUuid);
        if (_offerDescriptionLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setOfferDescriptionLocalized(newLocalized);
            for (Localized localized : _offerDescriptionLocalized)
            {
                newLocalized.add(new Localized(offerDescriptionUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setFulfillmentMessage(_fulfillmentMessage);
        String fulfillmentMessageUuid = UUID.randomUUID().toString();
        clone.setFulfillmentMessageUuid(fulfillmentMessageUuid);
        
        if (_fulfillmentMessageLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setFulfillmentMessageLocalized(newLocalized);
            for (Localized localized : _fulfillmentMessageLocalized)
            {
                newLocalized.add(new Localized(fulfillmentMessageUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setMsrp(_msrp);
        clone.setTotalCoupons(_totalCoupons);
        clone.setNumRemaining(_numRemaining);
        clone.setPrice(_price);
        clone.setPercentOff(_percentOff);
        clone.setExpirationDate(_expirationDate);
        clone.setOfferText(_offerText);
        clone.setOfferDescription(_offerDescription);
        clone.setRedeemUrl(_redeemUrl);
        clone.setMobileBannerUrl(_mobileBannerUrl);
        clone.setWebBannerUrl(_webBannerUrl);
        clone.setMobileDealImageUrl(_mobileDealImageUrl);
        clone.setWebDealImageUrl(_webDealImageUrl);
        clone.setFulfillmentType(_fulfillmentType);
        clone.setFulfillmentMessage(_fulfillmentMessage);
        clone.setCouponCode(_couponCode);
        clone.setCurrencyCode(_currencyCode);
        clone.setAutoFulfillType(_autoFulfillType);
        clone.setVersion(_version);
        clone.setAutoFulfill(_autoFulfill);
        
        return clone;
    }
    
    @JsonProperty(value="couponId")
    public int getId() {
        return _id;
    }
    
    @JsonProperty(value="couponId")
    public void setId(int id) {
        _id = id;
    }
    
    public double getMsrp() {
        return _msrp;
    }
    
    public void setMsrp(double msrp) {
        _msrp = msrp;
    }
    
    public int getTotalCoupons() {
        return _totalCoupons;
    }
    
    public void setTotalCoupons(int totalCoupons) {
        _totalCoupons = totalCoupons;
    }
    
    public int getNumRemaining() {
        return _numRemaining;
    }
    
    public void setNumRemaining(int numRemaining) {
        _numRemaining = numRemaining;
    }
    
    public double getPrice() {
        return _price;
    }
    
    public void setPrice(double price) {
        _price = price;
    }
    
    public boolean isPercentOff() {
        return _percentOff;
    }

    public void setPercentOff(boolean percentOff) {
        _percentOff = percentOff;
    }

    public Date getExpirationDate() {
        return _expirationDate;
    }
    
    public void setExpirationDate(Date expirationDate) {
        _expirationDate = expirationDate;
    }
    
    public String getOfferText() {
        return _offerText;
    }
    
    public void setOfferText(String offerText) {
        _offerText = offerText;
    }
    
    public String getOfferDescription() {
        return _offerDescription;
    }
    
    public void setOfferDescription(String offerDescription) {
        _offerDescription = offerDescription;
    }
    
    public String getRedeemUrl() {
        return _redeemUrl;
    }

    public void setRedeemUrl(String redeemUrl) {
        _redeemUrl = redeemUrl;
    }

    public String getMobileBannerUrl() {
        return _mobileBannerUrl;
    }
    
    public void setMobileBannerUrl(String mobileBannerUrl) {
        _mobileBannerUrl = mobileBannerUrl;
    }
    
    public String getWebBannerUrl() {
        return _webBannerUrl;
    }
    
    public void setWebBannerUrl(String webBannerUrl) {
        _webBannerUrl = webBannerUrl;
    }
    
    public String getMobileDealImageUrl() {
        return _mobileDealImageUrl;
    }
    
    public void setMobileDealImageUrl(String mobileDealImageUrl) {
        _mobileDealImageUrl = mobileDealImageUrl;
    }
    
    public String getWebDealImageUrl() {
        return _webDealImageUrl;
    }
    
    public void setWebDealImageUrl(String webDealImageUrl) {
        _webDealImageUrl = webDealImageUrl;
    }

    public int getFulfillmentType() {
        return _fulfillmentType;
    }

    public void setFulfillmentType(int fulfillmentType) {
        _fulfillmentType = fulfillmentType;
    }

    public String getFulfillmentMessage() {
        return _fulfillmentMessage;
    }

    public void setFulfillmentMessage(String fulfillmentMessage) {
        _fulfillmentMessage = fulfillmentMessage;
    }

    public String getCouponCode() {
        return _couponCode;
    }

    public void setCouponCode(String couponCode) {
        _couponCode = couponCode;
    }

    public String getCurrencyCode()
    {
        return _currencyCode;
    }

    public void setCurrencyCode(String currencyCode)
    {
        _currencyCode = currencyCode;
    }

    public String getOfferTextUuid()
    {
        return _offerTextUuid;
    }

    public void setOfferTextUuid(String offerTextUuid)
    {
        _offerTextUuid = offerTextUuid;
    }

    public List<Localized> getOfferTextLocalized()
    {
        return _offerTextLocalized;
    }

    public void setOfferTextLocalized(List<Localized> offerTextLocalized)
    {
        _offerTextLocalized = offerTextLocalized;
    }

    public String getOfferDescriptionUuid()
    {
        return _offerDescriptionUuid;
    }

    public void setOfferDescriptionUuid(String offerDescriptionUuid)
    {
        _offerDescriptionUuid = offerDescriptionUuid;
    }

    public List<Localized> getOfferDescriptionLocalized()
    {
        return _offerDescriptionLocalized;
    }

    public void setOfferDescriptionLocalized(List<Localized> offerDescriptionLocalized)
    {
        _offerDescriptionLocalized = offerDescriptionLocalized;
    }

    public String getFulfillmentMessageUuid()
    {
        return _fulfillmentMessageUuid;
    }

    public void setFulfillmentMessageUuid(String fulfillmentMessageUuid)
    {
        _fulfillmentMessageUuid = fulfillmentMessageUuid;
    }

    public List<Localized> getFulfillmentMessageLocalized()
    {
        return _fulfillmentMessageLocalized;
    }

    public void setFulfillmentMessageLocalized(List<Localized> fulfillmentMessageLocalized)
    {
        _fulfillmentMessageLocalized = fulfillmentMessageLocalized;
    }

    public String getAutoFulfillType()
    {
        return _autoFulfillType;
    }

    public void setAutoFulfillType(String autoFulfillType)
    {
        _autoFulfillType = autoFulfillType;
    }

    public int getVersion()
    {
        return _version;
    }

    public void setVersion(int version)
    {
        _version = version;
    }

    public boolean isAutoFulfill()
    {
        return _autoFulfill;
    }

    public void setAutoFulfill(boolean autoFulfill)
    {
        _autoFulfill = autoFulfill;
    }
}
