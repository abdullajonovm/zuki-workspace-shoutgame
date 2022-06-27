package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.meinc.gameplay.domain.Localized;

public class WorkbenchDeal implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1918301054373123012L;
    private Integer _workbenchDealId;
    private Integer _workbenchCampaignId;
    private String _offerText;
    private String _offerTextUuid;
    private List<Localized> _offerTextLocalized;
    private String _offerDescription;
    private String _offerDescriptionUuid;
    private List<Localized> _offerDescriptionLocalized;
    private String _offerInstructions;
    private String _offerInstructionsUuid;
    private List<Localized> _offerInstructionsLocalized;
    private String _redeemUrl;
    private Double _price;
    private Boolean _percentOff;
    private Double _msrp;
    private String _imageUrl;
    private Integer  _fulfillmentType;
    private String _couponCode;
    
    private Integer _totalCoupons;
    
    private Date _createDate;
    private Date _lastUpdated;
    private List<WorkbenchCode> _workbenchCodes;

    //will be one of the AUTO_FULFILL_CODE_* constants
    private String _autoFulfillType;
    
    //1="old" coupons where the AUTO_FULFILL_COUPON_CODE_* constant was stored in the couponCode field, 2="new" where the code is stored in the autoFulfillType field
    private int _version; 
    
    //so non AUTO_SHOUT_FULFILLMENT types can be auto fulfilled as well. in this case, the fulfillment_message_uuid field will contain a ref to json instructions on how to redeem
    // (usually a code and instructions to be displayed to the user)
    private boolean _autoFulfill;
    
    public WorkbenchDeal clone(String nameSuffix)
    {
        WorkbenchDeal clone = new WorkbenchDeal();
        
        String offerTextUuid = UUID.randomUUID().toString();
        clone.setOfferText(_offerText);
        clone.setOfferTextUuid(offerTextUuid);
        if (_offerTextLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            for (Localized localized : _offerTextLocalized)
            {
                newLocalized.add(new Localized(offerTextUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        String offerDescriptionUuid = UUID.randomUUID().toString();
        clone.setOfferDescription(_offerDescription);
        clone.setOfferDescriptionUuid(offerDescriptionUuid);
        if (_offerDescriptionLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            for (Localized localized : _offerDescriptionLocalized)
            {
                newLocalized.add(new Localized(offerDescriptionUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        String offerInstructionsUuid = UUID.randomUUID().toString();
        clone.setOfferInstructions(_offerInstructions);
        clone.setOfferInstructionsUuid(offerInstructionsUuid);
        if (_offerInstructionsLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            for (Localized localized : _offerInstructionsLocalized)
            {
                newLocalized.add(new Localized(offerInstructionsUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setWorkbenchCampaignId(_workbenchCampaignId);
        clone.setOfferText(_offerText);
        clone.setOfferDescription(_offerDescription);
        clone.setOfferInstructions(_offerInstructions);
        clone.setMsrp(_msrp);
        clone.setPrice(_price);
        clone.setPercentOff(_percentOff);
        clone.setCreateDate(_createDate);
        clone.setLastUpdated(_lastUpdated);
        clone.setWorkbenchCodes(_workbenchCodes);
        clone.setImageUrl(_imageUrl);
        clone.setRedeemUrl(_redeemUrl);
        clone.setFulfillmentType(_fulfillmentType);
        clone.setCouponCode(_couponCode);
        clone.setAutoFulfillType(_autoFulfillType);
        clone.setVersion(_version);
        clone.setAutoFulfill(_autoFulfill);
        
        return clone;
    }

    public Integer getWorkbenchDealId()
    {
        return _workbenchDealId;
    }

    public void setWorkbenchDealId(Integer id)
    {
        _workbenchDealId = id;
    }

    public Integer getWorkbenchCampaignId()
    {
        return _workbenchCampaignId;
    }

    public void setWorkbenchCampaignId(Integer workbenchCampaignId)
    {
        this._workbenchCampaignId = workbenchCampaignId;
    }

    public String getOfferText()
    {
        return _offerText;
    }

    public void setOfferText(String offerText)
    {
        this._offerText = offerText;
    }

    public String getOfferDescription()
    {
        return _offerDescription;
    }

    public void setOfferDescription(String offerDescription)
    {
        _offerDescription = offerDescription;
    }

    public String getOfferInstructions()
    {
        return _offerInstructions;
    }

    public void setOfferInstructions(String offerInstructions)
    {
        this._offerInstructions = offerInstructions;
    }

    public Double getMsrp()
    {
        return _msrp;
    }

    public void setMsrp(Double msrp)
    {
        _msrp = msrp;
    }

    public Double getPrice()
    {
        return _price;
    }

    public void setPrice(Double price)
    {
        _price = price;
    }

    public Boolean isPercentOff()
    {
        return _percentOff;
    }

    public void setPercentOff(Boolean percentOff)
    {
        _percentOff = percentOff;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this._createDate = createDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        this._lastUpdated = lastUpdated;
    }

    public List<WorkbenchCode> getWorkbenchCodes()
    {
        return _workbenchCodes;
    }

    public void setWorkbenchCodes(List<WorkbenchCode> _workbenchCodes)
    {
        this._workbenchCodes = _workbenchCodes;
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

    public String getOfferInstructionsUuid()
    {
        return _offerInstructionsUuid;
    }

    public void setOfferInstructionsUuid(String offerInstructionsUuid)
    {
        _offerInstructionsUuid = offerInstructionsUuid;
    }

    public List<Localized> getOfferInstructionsLocalized()
    {
        return _offerInstructionsLocalized;
    }

    public void setOfferInstructionsLocalized(List<Localized> offerInstructionsLocalized)
    {
        _offerInstructionsLocalized = offerInstructionsLocalized;
    }

    public String getImageUrl()
    {
        return _imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        _imageUrl = imageUrl;
    }

    public Integer getTotalCoupons()
    {
        return _totalCoupons;
    }

    public void setTotalCoupons(Integer totalCoupons)
    {
        _totalCoupons = totalCoupons;
    }

    public String getRedeemUrl()
    {
        return _redeemUrl;
    }

    public void setRedeemUrl(String redeemUrl)
    {
        _redeemUrl = redeemUrl;
    }

    public Integer getFulfillmentType()
    {
        return _fulfillmentType;
    }

    public void setFulfillmentType(Integer fulfillmentType)
    {
        _fulfillmentType = fulfillmentType;
    }

    public String getCouponCode()
    {
        return _couponCode;
    }

    public void setCouponCode(String couponCode)
    {
        _couponCode = couponCode;
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
