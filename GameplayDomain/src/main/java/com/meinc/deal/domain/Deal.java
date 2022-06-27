package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.gameplay.domain.Localized;

public class Deal 
implements Serializable
{
    private static final long serialVersionUID = 9048889722497912288L;
    private int _id;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    private Sponsor _sponsor;
    private int sponsorId;
    private String _imageUrl;
    
    /** deal flip under-image text */
    private String _shortDesc;
    private String _shortDescUuid;
    private List<Localized> _shortDescLocalized;
    
    private List<Coupon> _coupons;

    public Deal clone(String nameSuffix)
    {
        Deal clone = new Deal();
        
        clone.setName(_name + nameSuffix);
        String nameUuid = UUID.randomUUID().toString();
        clone.setNameUuid(nameUuid);
        if (_nameLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setNameLocalized(newLocalized);
            for (Localized localized : _nameLocalized)
            {
                newLocalized.add(new Localized(nameUuid, localized.getLanguageCode(), localized.getValue() + nameSuffix));
            }
        }
        
        clone.setShortDesc(_shortDesc);
        String shortDescUuid = UUID.randomUUID().toString();
        clone.setShortDescUuid(shortDescUuid);
        if (_shortDescLocalized != null)
        {
            List<Localized> newLocalized = new ArrayList<Localized>();
            clone.setShortDescLocalized(newLocalized);
            for (Localized localized : _shortDescLocalized)
            {
                newLocalized.add(new Localized(shortDescUuid, localized.getLanguageCode(), localized.getValue()));
            }
        }
        
        clone.setSponsor(_sponsor);
        clone.setImageUrl(_imageUrl);
        clone.setShortDesc(_shortDesc);
        List<Coupon> coupons = new ArrayList<Coupon>();
        for (Coupon coupon : _coupons) {
            coupons.add(coupon.clone());
        }
        clone.setCoupons(coupons);
        return clone;
    }

    @JsonProperty(value = "dealId")
    public int getId()
    {
        return _id;
    }

    @JsonProperty(value = "dealId")
    public void setId(int id)
    {
        _id = id;
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

    public Sponsor getSponsor()
    {
        return _sponsor;
    }

    public void setSponsor(Sponsor sponsor)
    {
        _sponsor = sponsor;
    }

    public String getImageUrl()
    {
        return _imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        _imageUrl = imageUrl;
    }

    public List<Coupon> getCoupons()
    {
        return _coupons;
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

    public void setCoupons(List<Coupon> coupons)
    {
        _coupons = coupons;
    }

    public int getSponsorId()
    {
        return sponsorId;
    }

    public void setSponsorId(int sponsorId)
    {
        this.sponsorId = sponsorId;
    }
}
