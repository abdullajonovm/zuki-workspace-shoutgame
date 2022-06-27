package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.meinc.gameplay.domain.Localized;

public class WorkbenchCampaign implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1918301054373123012L;
    private Integer _workbenchCampaignId;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    private String _shortDisplayName;
    private String _shortDisplayNameUuid;
    private List<Localized> _shortDisplayNameLocalized;
    private String _shortDesc;
    private String _shortDescUuid;
    private List<Localized> _shortDescLocalized;
    private Integer _sponsorId;
    private Integer _adId;
    private Integer _appId;
    private Date _expirationDate;
    private String _imageUrl;
    private String _clickUrl;
    private Date _startDate;
    private Date _endDate;
    private String _currencyCode;
    private Double _longitude;
    private Double _latitude;
    private Double _geoRadius;
    private Boolean _locationRequired;
    private Date _createDate;
    private Date _lastUpdated;
    private List<WorkbenchDeal> _workbenchDeals;

    public WorkbenchCampaign clone(String nameSuffix)
    {
        WorkbenchCampaign clone = new WorkbenchCampaign();
        String nameUuid = UUID.randomUUID().toString();
        clone.setName(_name + nameSuffix);
        clone.setNameUuid(nameUuid);
        if (_nameLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            for (Localized localized : _nameLocalized)
            {
                newLocalized.add(new Localized(nameUuid, localized.getLanguageCode(), localized.getValue() + nameSuffix));
            }
        }
        
        String shortDisplayNameUuid = UUID.randomUUID().toString();
        clone.setShortDisplayNameUuid(shortDisplayNameUuid);
        
        if (_shortDisplayNameLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setShortDisplayNameLocalized(newLocalized);
            for (Localized localized : _shortDisplayNameLocalized)
            {
                newLocalized.add(new Localized(shortDisplayNameUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        String shortDescUuid = UUID.randomUUID().toString();
        clone.setShortDesc(_shortDesc);
        clone.setShortDescUuid(shortDescUuid);
        if (_shortDescLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            for (Localized localized : _shortDescLocalized)
            {
                newLocalized.add(new Localized(shortDescUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setSponsorId(_sponsorId);
        clone.setAdId(_adId);
        clone.setExpirationDate(_expirationDate);
        clone.setImageUrl(_imageUrl);
        clone.setStartDate(_startDate);
        clone.setEndDate(_endDate);
        clone.setCreateDate(_createDate);
        clone.setLastUpdated(_lastUpdated);
        clone.setWorkbenchDeals(_workbenchDeals);
        clone.setClickUrl(_clickUrl);
        clone.setCurrencyCode(_currencyCode);
        clone.setLongitude(_longitude);
        clone.setLatitude(_latitude);
        clone.setGeoRadius(_geoRadius);
        clone.setLocationRequired(_locationRequired);
        clone.setAppId(_appId);
        
        return clone;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
    
    public String getNameUuid()
    {
        return _nameUuid;
    }

    public void setNameUuid(String nameUuid)
    {
        _nameUuid = nameUuid;
    }

    public List<Localized> getNameLocalized()
    {
        return _nameLocalized;
    }

    public void setNameLocalized(List<Localized> nameLocalized)
    {
        _nameLocalized = nameLocalized;
    }
    
    public String getShortDisplayName()
    {
        return _shortDisplayName;
    }
    
    public void setShortDisplayName(String shortDisplayName)
    {
        _shortDisplayName = shortDisplayName;
    }
    
    public String getShortDisplayNameUuid()
    {
        return _shortDisplayNameUuid;
    }

    public void setShortDisplayNameUuid(String shortDisplayNameUuid)
    {
        _shortDisplayNameUuid = shortDisplayNameUuid;
    }

    public List<Localized> getShortDisplayNameLocalized()
    {
        return _shortDisplayNameLocalized;
    }

    public void setShortDisplayNameLocalized(List<Localized> shortDisplayNameLocalized)
    {
        _shortDisplayNameLocalized = shortDisplayNameLocalized;
    }

    public Integer getWorkbenchCampaignId()
    {
        return _workbenchCampaignId;
    }

    public void setWorkbenchCampaignId(Integer workbenchCampaignId)
    {
        this._workbenchCampaignId = workbenchCampaignId;
    }

    public Integer getSponsorId()
    {
        return _sponsorId;
    }

    public void setSponsorId(Integer sponsorId)
    {
        this._sponsorId = sponsorId;
    }
    
    public Integer getAdId()
    {
        return _adId;
    }

    public void setAdId(Integer adId)
    {
        _adId = adId;
    }

    public Date getExpirationDate()
    {
        return _expirationDate;
    }

    public void setExpirationDate(Date expireDate)
    {
        _expirationDate = expireDate;
    }
    
    public String getImageUrl()
    {
        return _imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        _imageUrl = imageUrl;
    }

    public Date getStartDate()
    {
        return _startDate;
    }

    public void setStartDate(Date startDate)
    {
        this._startDate = startDate;
    }

    public Date getEndDate()
    {
        return _endDate;
    }

    public void setEndDate(Date endDate)
    {
        this._endDate = endDate;
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

    public List<WorkbenchDeal> getWorkbenchDeals()
    {
        return _workbenchDeals;
    }

    public void setWorkbenchDeals(List<WorkbenchDeal> _workbenchDeals)
    {
        this._workbenchDeals = _workbenchDeals;
    }

    public String getShortDesc()
    {
        return _shortDesc;
    }

    public void setShortDesc(String shortDesc)
    {
        _shortDesc = shortDesc;
    }

    public String getShortDescUuid()
    {
        return _shortDescUuid;
    }

    public void setShortDescUuid(String shortDescUuid)
    {
        _shortDescUuid = shortDescUuid;
    }

    public List<Localized> getShortDescLocalized()
    {
        return _shortDescLocalized;
    }

    public void setShortDescLocalized(List<Localized> shortDescLocalized)
    {
        _shortDescLocalized = shortDescLocalized;
    }

    public String getClickUrl()
    {
        return _clickUrl;
    }

    public void setClickUrl(String clickUrl)
    {
        _clickUrl = clickUrl;
    }
    
    public String getCurrencyCode()
    {
        return _currencyCode;
    }

    public void setCurrencyCode(String currencyCode)
    {
        _currencyCode = currencyCode;
    }

    public Double getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(Double longitude)
    {
        _longitude = longitude;
    }

    public Double getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(Double latitude)
    {
        _latitude = latitude;
    }

    public Double getGeoRadius()
    {
        return _geoRadius;
    }

    public void setGeoRadius(Double geoRadius)
    {
        _geoRadius = geoRadius;
    }

    public Boolean getLocationRequired()
    {
        return _locationRequired;
    }

    public void setLocationRequired(Boolean locationRequired)
    {
        _locationRequired = locationRequired;
    }

    public Integer getAppId()
    {
        return _appId;
    }

    public void setAppId(Integer appId)
    {
        _appId = appId;
    }
}
