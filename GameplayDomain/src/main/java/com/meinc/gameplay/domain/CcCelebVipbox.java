package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CcCelebVipbox implements Serializable
{
    private static final long serialVersionUID = 8901327608326931560L;
    
    private Integer _ccCelebVipboxId;
    private Integer _ccId;
    private Integer _celebVipboxId;
    private String _marketingHtml;
    private String _marketingHtmlUuid;
    private List<Localized> _marketingHtmlLocalized;
    private String _contestTemplateId;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public CcCelebVipbox()
    {
    }

    public Integer getCcCelebVipboxId()
    {
        return _ccCelebVipboxId;
    }

    public void setCcCelebVipboxId(Integer ccCelebVipboxId)
    {
        _ccCelebVipboxId = ccCelebVipboxId;
    }

    public Integer getCcId()
    {
        return _ccId;
    }

    public void setCcId(Integer ccId)
    {
        _ccId = ccId;
    }

    public Integer getCelebVipboxId()
    {
        return _celebVipboxId;
    }

    public void setCelebVipboxId(Integer celebVipboxId)
    {
        _celebVipboxId = celebVipboxId;
    }

    public String getMarketingHtml()
    {
        return _marketingHtml;
    }

    public void setMarketingHtml(String marketingHtml)
    {
        _marketingHtml = marketingHtml;
    }
    
    public String getMarketingHtmlUuid()
    {
        return _marketingHtmlUuid;
    }

    public void setMarketingHtmlUuid(String marketingHtmlUuid)
    {
        _marketingHtmlUuid = marketingHtmlUuid;
    }

    public List<Localized> getMarketingHtmlLocalized()
    {
        return _marketingHtmlLocalized;
    }

    public void setMarketingHtmlLocalized(List<Localized> marketingHtmlLocalized)
    {
        _marketingHtmlLocalized = marketingHtmlLocalized;
    }

    public String getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(String contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
    }

    public Date getCreatedDate()
    {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }
}
