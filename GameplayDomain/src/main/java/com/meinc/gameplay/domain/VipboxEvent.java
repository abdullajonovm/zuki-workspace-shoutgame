package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class VipboxEvent implements Serializable
{
    private static final long serialVersionUID = 1553324868942120449L;
    
    private Integer _vipboxId;
    private Integer _eventId;
    private String _marketingHtml;
    private String _marketingHtmlUuid;
    private List<Localized> _marketingHtmlLocalized;
    
    //TODO: remove these as soon as sure that the clients won't break if not there.
    private String _rulesHtml;
    private String _prizesHtml;
    
    public VipboxEvent()
    {
    }
    
    public VipboxEvent(Integer vipboxId, Integer eventId, String marketingHtml, String marketingHtmlUuid)
    {
        _vipboxId = vipboxId;
        _eventId = eventId;
        _marketingHtml = marketingHtml;
        _marketingHtmlUuid = marketingHtmlUuid;
    }

    public Integer getEventId()
    {
        return _eventId;
    }

    public void setEventId(Integer eventId)
    {
        _eventId = eventId;
    }

    public Integer getVipboxId()
    {
        return _vipboxId;
    }

    public void setVipboxId(Integer vipboxId)
    {
        _vipboxId = vipboxId;
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

    public String getRulesHtml()
    {
        return _rulesHtml;
    }

    public void setRulesHtml(String rulesHtml)
    {
        _rulesHtml = rulesHtml;
    }

    public String getPrizesHtml()
    {
        return _prizesHtml;
    }

    public void setPrizesHtml(String prizesHtml)
    {
        _prizesHtml = prizesHtml;
    }
}
