package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.meinc.deal.domain.Campaign;

public class Contest implements Serializable
{
    private static final long serialVersionUID = -3010078782244448427L;

    public static enum CONTEST_TYPE
    {
        EVENT,    // PrimaryRefId will be an eventId.  The scope of the contest is an event and is tied to a single event.
        CHANNEL,  // PrimaryRefId will be a channelId.  The contest is tied to a channel, not an event and must be scoped to start/end dates.
        LEAGUE,   // PrimaryRefId will be a leagueId.  The contest is tied to a league, not an event and must be scoped to start/end dates.
        GLOBAL,   // PrimaryRefID will be null. The contest is over all questions and all events and must be scoped to start/end dates.
        VIPBOX,   // PrimaryRefId will be a vipBoxId.  The contest is over all questions for all events a specific VIPBOX is associated with and must be scoped to start/end dates.
        QUESTION, // PrimaryRefId will be a questionId.  The contest is for a single question, start/end date is irrelevant as the scope is the start/end of the question.
        CC_LEVEL, // PrimaryRefId will be a levelId. The contest is for a single level in a single event.
    };

    public static enum CONTEST_VIPBOX_TYPE
    {
        NONE,          // The contest is governed solely by the CONTEST_TYPE as is not further differentiated by a VIPBOX.

        VIPBOX,          // The contest is specific to a VIPBOX and the vipBoxId will be the vipBoxId whose subscriber are playing the contest.  If CONTEST_TYPE is...
                         // EVENT: the primaryRefId is the event that scopes the contest start/end and the VIPBox tied to it.
                         // CHANNEL: the primaryRefId is the channel whose associated events' questions' tied to this VIPBox will be considered.  Start/end date required to scope it.
                         // LEAGUE: the primaryRefId is the league whose associated events' questions' tied to this VIPBox will be considered.  Start/end date required to scope it.
                         // GLOBAL: primaryRefId is null and all events' questions' tied to this VIPBox will be considered.  Start/end date required to scope it.
                         // VIPBOX: when this is set in CONTEST_TYPE then CONTEST_VIPBOX_TYPE must be NONE and is invalid if set to VIPBOX
                         // QUESTION: primaryRefId is the question that will will have a contest specific to the players of this VIPBOX,

        ALL_VIPBOXES     // The contest is between VIPBOXES playing against one another
    }

    public static enum CONTEST_STATUS
    {
        NEW,
        INPROGRESS,
        COMPLETE
    }

    private int _contestId;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    private String _description;
    private String _descriptionUuid;
    private List<Localized> _descriptionLocalized;
    private CONTEST_TYPE _type;
    private CONTEST_VIPBOX_TYPE _vipBoxType;
    private Integer _primaryRefId;
    private Integer _vipBoxId;
    private Integer _contestTemplateId;
    private Integer _appId;
    private CONTEST_STATUS _status;
    private Date _startDate;
    private Date _endDate;
    private String _marketingHtml;
    private String _rulesHtml;
    private String _prizesHtml;
    private String _prizesHtmlUuid;
    private List<Localized> _prizesHtmlLocalized;
    private List<ContestScoringRule> _scoringRules;
    private List<ContestPayoutRule> _payoutRules;
    private Campaign _campaign;
    private Date _createdDate;
    private Date _lastUpdated;

    public Contest()
    {
    }

    public Contest(String name, String nameUuid, String description, String descriptionUuid, CONTEST_TYPE type, CONTEST_VIPBOX_TYPE vipBoxType,
                   Integer primaryRefId, Integer vipBoxId, Integer contestTemplateId, Date startDate,
                   Date endDate, String marketingHtml, String rulesHtml, String prizesHtml, String prizesHtmlUuid, Integer appId)
    {
        _name = name;
        _nameUuid = nameUuid;
        _description = description;
        _descriptionUuid = descriptionUuid;
        _type = type;
        _vipBoxType = vipBoxType;
        _primaryRefId = primaryRefId;
        _vipBoxId = vipBoxId;
        _contestTemplateId = contestTemplateId;
        _startDate = startDate;
        _endDate = endDate;
        _marketingHtml = marketingHtml;
        _rulesHtml = rulesHtml;
        _prizesHtml = prizesHtml;
        _prizesHtmlUuid = prizesHtmlUuid;
        _appId = appId;
    }

    public int getContestId()
    {
        return _contestId;
    }

    public void setContestId(int contestId)
    {
        _contestId = contestId;
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

    public Integer getPrimaryRefId()
    {
        return _primaryRefId;
    }

    public void setPrimaryRefId(Integer primaryRefId)
    {
        _primaryRefId = primaryRefId;
    }

    public Integer getVipBoxId()
    {
        return _vipBoxId;
    }

    public void setVipBoxId(Integer vipBoxId)
    {
        _vipBoxId = vipBoxId;
    }
    
    public Integer getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(Integer contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
    }

    public CONTEST_STATUS getStatus()
    {
        return _status;
    }

    public void setStatus(CONTEST_STATUS status)
    {
        _status = status;
    }

    public Date getStartDate()
    {
        return _startDate;
    }

    public void setStartDate(Date startDate)
    {
        _startDate = startDate;
    }

    public Date getEndDate()
    {
        return _endDate;
    }

    public void setEndDate(Date endDate)
    {
        _endDate = endDate;
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

    @JsonIgnore
    public List<ContestPayoutRule> getPayoutRules()
    {
        return _payoutRules;
    }

    public void setPayoutRules(List<ContestPayoutRule> payoutRules)
    {
        _payoutRules = payoutRules;
    }

    public List<Integer> getScoringRuleIds()
    {
        List<Integer> result = null;
        if (_scoringRules != null && _scoringRules.size() > 0)
        {
            result = new ArrayList<Integer>();
            for (ContestScoringRule scoringRule : _scoringRules)
            {
                result.add(scoringRule.getScoringRuleId());
            }
        }

        return result;
    }

    public List<Integer> getPayoutRuleIds()
    {
        List<Integer> result = null;
        if (_payoutRules != null && _payoutRules.size() > 0)
        {
            result = new ArrayList<Integer>();
            for (ContestPayoutRule payoutRule : _payoutRules)
            {
                result.add(payoutRule.getPayoutRuleId());
            }
        }

        return result;
    }

    public String getPrizesHtml()
    {
        return _prizesHtml;
    }

    public void setPrizesHtml(String prizeshtml)
    {
        _prizesHtml = prizeshtml;
    }

    @JsonIgnore
    public List<ContestScoringRule> getScoringRules()
    {
        return _scoringRules;
    }

    public void setScoringRules(List<ContestScoringRule> scoringRules)
    {
        _scoringRules = scoringRules;
    }

    public Campaign getCampaign()
    {
        return _campaign;
    }

    public void setCampaign(Campaign campaign)
    {
        _campaign = campaign;
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

        sb.append("Contest{contestId:").append(_contestId).append(", name: ").append(_name).append(", description").append(_description);
        sb.append(", type: ").append(_type).append(", vipboxType: ").append(_vipBoxType).append(", primaryRefId: ").append(_primaryRefId);
        sb.append(", vipBoxId: ").append(_vipBoxId).append(", status: ").append(_status).append(", startDate: ").append(_startDate);
        sb.append(", endDate: ").append(_endDate).append(", marketingHtml: ").append(_marketingHtml).append(", rulesHtml: ").append(_rulesHtml);
        sb.append(", prizesHtml: ").append(_prizesHtml).append(", appId: ").append(_appId);

        if (_scoringRules != null)
        {
            sb.append(", scoringRules: [");
            boolean first = true;
            for (ContestScoringRule rule : _scoringRules)
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
            for (ContestPayoutRule rule : _payoutRules)
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