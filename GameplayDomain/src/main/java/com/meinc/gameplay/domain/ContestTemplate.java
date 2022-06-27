package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.meinc.deal.domain.Campaign;

import static com.meinc.gameplay.domain.Contest.CONTEST_TYPE;
import static com.meinc.gameplay.domain.Contest.CONTEST_VIPBOX_TYPE;

public class ContestTemplate implements Serializable
{
    private static final long serialVersionUID = -3218708482812703402L;
    
    private int _contestTemplateId;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    private String _description;
    private String _descriptionUuid;
    private List<Localized> _descriptionLocalized;
    private CONTEST_TYPE _type;
    private CONTEST_VIPBOX_TYPE _vipBoxType;
    private String _marketingHtml;
    private String _rulesHtml;
    private String _prizesHtml;
    private String _prizesHtmlUuid;
    private Integer _appId;
    private List<Localized> _prizesHtmlLocalized;
    private List<ContestTemplateScoringRule> _scoringRules;
    private List<ContestTemplatePayoutRule> _payoutRules;
    private Campaign _campaign;
    
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestTemplate()
    {
    }

    public ContestTemplate(String name, String nameUuid, String description, String descriptionUuid, CONTEST_TYPE type, CONTEST_VIPBOX_TYPE vipBoxType,
                           String marketingHtml, String rulesHtml, String prizesHtml, String prizesHtmlUuid, Integer appId)
    {
        _name = name;
        _nameUuid = nameUuid;
        _description = description;
        _descriptionUuid = descriptionUuid;
        _type = type;
        _vipBoxType = vipBoxType;
        _marketingHtml = marketingHtml;
        _rulesHtml = rulesHtml;
        _prizesHtml = prizesHtmlUuid;
        _appId = appId;
    }

    public int getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(int contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
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

    public String getDescriptionUuid()
    {
        return _descriptionUuid;
    }

    public void setDescriptionUuid(String descriptionUuid)
    {
        _descriptionUuid = descriptionUuid;
    }

    public List<Localized> getDescriptionLocalized()
    {
        return _descriptionLocalized;
    }

    public void setDescriptionLocalized(List<Localized> descriptionLocalized)
    {
        _descriptionLocalized = descriptionLocalized;
    }

    public String getPrizesHtmlUuid()
    {
        return _prizesHtmlUuid;
    }

    public void setPrizesHtmlUuid(String prizesHtmlUuid)
    {
        _prizesHtmlUuid = prizesHtmlUuid;
    }

    public List<Localized> getPrizesHtmlLocalized()
    {
        return _prizesHtmlLocalized;
    }

    public void setPrizesHtmlLocalized(List<Localized> prizesHtmlLocalized)
    {
        _prizesHtmlLocalized = prizesHtmlLocalized;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public CONTEST_TYPE getType()
    {
        return _type;
    }

    public void setType(CONTEST_TYPE type)
    {
        _type = type;
    }

    public CONTEST_VIPBOX_TYPE getVipBoxType()
    {
        return _vipBoxType;
    }

    public void setVipBoxType(CONTEST_VIPBOX_TYPE vipBoxType)
    {
        _vipBoxType = vipBoxType;
    }

    public String getMarketingHtml()
    {
        return _marketingHtml;
    }

    public void setMarketingHtml(String marketingHtml)
    {
        _marketingHtml = marketingHtml;
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

    public List<ContestTemplatePayoutRule> getPayoutRules()
    {
        return _payoutRules;
    }

    public void setPayoutRules(List<ContestTemplatePayoutRule> payoutRules)
    {
        _payoutRules = payoutRules;
    }

    
    public List<ContestTemplateScoringRule> getScoringRules()
    {
        return _scoringRules;
    }

    public void setScoringRules(List<ContestTemplateScoringRule> scoringRules)
    {
        _scoringRules = scoringRules;
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
    
    public Campaign getCampaign()
    {
        return _campaign;
    }

    public void setCampaign(Campaign campaign)
    {
        _campaign = campaign;
    }
    
    public Integer getAppId()
    {
        return _appId;
    }

    public void setAppId(Integer appId)
    {
        _appId = appId;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Contest{contestTemplateId:").append(_contestTemplateId).append(", name: ").append(_name).append(", description").append(_description);
        sb.append(", type: ").append(_type).append(", vipboxType: ").append(_vipBoxType).append(", marketingHtml: ").append(_marketingHtml);
        sb.append(", rulesHtml: ").append(_rulesHtml).append(", prizesHtml: ").append(_prizesHtml).append(", appId: ").append(_appId);
        
        if (_scoringRules != null)
        {
            sb.append(", scoringRules: [");
            boolean first = true;
            for (ContestTemplateScoringRule rule : _scoringRules)
            {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                
                sb.append(rule);
            }
            sb.append("]");
        }
        
        if (_payoutRules != null)
        {
            sb.append(", payoutRules: [");
            boolean first = true;
            for (ContestTemplatePayoutRule rule : _payoutRules)
            {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                
                sb.append(rule);
            }
            sb.append("]");
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}
