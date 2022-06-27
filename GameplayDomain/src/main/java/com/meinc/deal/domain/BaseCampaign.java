package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.gameplay.domain.Localized;

public abstract class BaseCampaign
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int _id;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    private String _shortDisplayName;
    private String _shortDisplayNameUuid;
    private List<Localized> _shortDisplayNameLocalized;
    private Integer _appId;
    
    private Date _expirationDate;
    private boolean _default;
    private boolean _cloneable;
    private boolean _active;
    private Double _latitude;
    private Double _longitude;
    private Double _geoRadius;
    private boolean _locationRequired;
    
    protected void baseClone(String nameSuffix, BaseCampaign clone)
    {
        clone.setName(_name + nameSuffix);
        
        String uuid = UUID.randomUUID().toString();
        clone.setNameUuid(uuid);
        
        if (_nameLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setNameLocalized(newLocalized);
            for (Localized localized : _nameLocalized)
            {
                newLocalized.add(new Localized(uuid, localized.getLanguageCode(), localized.getValue() + nameSuffix));
            }
        }
        
        uuid = UUID.randomUUID().toString();
        clone.setShortDisplayNameUuid(uuid);
        
        if (_shortDisplayNameLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setShortDisplayNameLocalized(newLocalized);
            for (Localized localized : _shortDisplayNameLocalized)
            {
                newLocalized.add(new Localized(uuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setExpirationDate(_expirationDate);
        clone.setDefault(false);
        clone.setCloneable(false);
        clone.setLatitude(_latitude);
        clone.setLongitude(_longitude);
        clone.setGeoRadius(_geoRadius);
        clone.setLocationRequired(_locationRequired);
        clone.setAppId(_appId);
    }
    
    @JsonProperty(value="campaignId")
    public int getId() {
        return _id;
    }

    @JsonProperty(value="campaignId")
    public void setId(int id) {
        _id = id;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setName(String campaignName) {
        _name = campaignName;
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

    public Date getExpirationDate() {
        return _expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        _expirationDate = expirationDate;
    }

    public boolean isDefault() {
        return _default;
    }

    public void setDefault(boolean default1) {
        _default = default1;
    }

    public boolean isCloneable() {
        return _cloneable;
    }

    public void setCloneable(boolean cloneable) {
        _cloneable = cloneable;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public Double getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(Double latitude)
    {
        _latitude = latitude;
    }

    public Double getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(Double longitude)
    {
        _longitude = longitude;
    }

    public Double getGeoRadius()
    {
        return _geoRadius;
    }

    public void setGeoRadius(Double geoRadius)
    {
        _geoRadius = geoRadius;
    }

    public boolean isLocationRequired()
    {
        return _locationRequired;
    }

    public void setLocationRequired(boolean locationRequired)
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
