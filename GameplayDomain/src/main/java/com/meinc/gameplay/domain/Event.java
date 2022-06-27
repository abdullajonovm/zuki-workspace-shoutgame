package com.meinc.gameplay.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.commons.helper.ConcurrentDateFormatHelper;
import com.meinc.commons.helper.IConcurrentDateFormat;

public class Event 
implements Serializable
{
    private static final long serialVersionUID = 8392492400684606635L;
    public static final int classId = 1002;
    private static IConcurrentDateFormat dateFormat = ConcurrentDateFormatHelper.getFormatterForFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * What information to place within the Event object.
     */
    public static enum SERIALIZE_TYPE {
        /** Only basic event information is contained (id, name, keyword, dates, etc..) */
        BASIC,
        /** BASIC + team data */
        TEAM,
        /** TEAM + list of questions/answers */
        QUESTION,
        /** QUESTION + answer percentages */
        QUESTION_WITH_RESULT,
        /** QUESTION_WITH_RESULT + all the scoring / payout information */
        ADMIN
    };
    
    private SERIALIZE_TYPE _serializeType = SERIALIZE_TYPE.BASIC;
    
    /* **************************************** */
    /* Fields tied directly to event db columns */
    /* **************************************** */
    
    //  `event_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    private int _eventId;
    
    //  `created_date` DATETIME NOT NULL,
    private Date _createdDate;
    
    //  `name` VARCHAR(254) NOT NULL,
    private String _name;
    
    private String _nameUuid;
    
    private List<Localized> _nameLocalized;

    public static enum SCOPE_TYPE {
        PUBLIC,  // Public events 
        PRIVATE, // Private events
        ANY      // Any event
    };
    
    public static enum STATUS_TYPE {
        NEW,            // New
        INPROGRESS,     // Inprogress
        COMPLETE,       // Complete
        NOT_NEW,        // Inprogress or complete 
        NOT_CLOSED,     // New or inprogress
        ALL             // Inprogress, new or complete
    };
    
    //  `status` ENUM('NEW','INPROGRESS','COMPLETE') NOT NULL,
    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_IN_PROGRESS = "INPROGRESS";
    public static final String STATUS_COMPLETE = "COMPLETE";
    //for the rest calls
    public static final int NOT_NEW = 0;
    public static final int NOT_CLOSED = 1;
    public static final int CLOSED = 2;
    public static final int BOTH = 3;
    public static final int INPROGRESS = 4;
    private String _status;
    
    //  `textwire_keyword` VARCHAR(25),
    private String _keyword;
    
    //  `expected_start_date` DATETIME NOT NULL,
    private Date _expectedEventStartDateTime;
    
    //  `expected_stop_date` DATETIME NOT NULL,
    private Date _expectedEventStopDateTime;
    
    //  `actual_start_date` DATETIME,
    private Date _actualStartDate;
    
    //  `actual_stop_date` DATETIME,
    private Date _actualStopDate;
    
    //  `private_evt` INTEGER(1) UNSIGNED NOT NULL DEFAULT 0,
    private boolean _private;
    
    //  `sponsor` VARCHAR(45) NULL, 
    /** the name of the game sponsor.  ex: Groove */
    private String _sponsor;
    
    //  `coupon` VARCHAR(45) NULL, 
    /** the name of the "everybody wins" coupon.  ex: St. George Golf */
    private String _coupon;
    
    //  `league_id` INT NULL DEFAULT 0,
    private int _leagueId;
    
    //  `highlighted` INT(1) NOT NULL DEFAULT 0,
    private boolean _highlighted;
    
    // `min_game_purse` DECIMAL(9,2) NOT NULL DEFAULT 0.0
    private double _minimumGamePurse;
    
    // `scoring_data` TEXT NOT NULL DEFAULT ''
    /** a json bag containing the scoring data specific to the event */
    private String _scoringData;
    
    // `marketing_html` TEXT NULL
    private String _marketingHtml;
    
    private String _marketingHtmlUuid;
    
    private List<Localized> _marketingHtmlLocalized;
    
    private Date _lastUpdated;
    
    private Integer _fiftyPctCampaignId;
    
    /* **************************************** */
    /*   Fields by join tables or subselects    */
    /* **************************************** */
    
    /** a list of teams associated with this event */
    private List<Team> _teams;
    
    private int _numPlayers;
    
    private List<Question> _questions;
    
    private List<EventMediaRef> _eventMedia;
    
    /** may be null */
    private List<ClockFreezeSponsor> _clockFreezeSponsors;

    /** may NOT be null */
    private List<Integer> _appIds;
    
    public Event() 
    {
        _teams = new ArrayList<Team>();
    }

    @JsonIgnore
    public void setSerializeType(SERIALIZE_TYPE serializeType)
    {
        _serializeType = serializeType;
    }

    @JsonIgnore
    public SERIALIZE_TYPE getSerializeType()
    {
        return _serializeType;
    }

    @JsonProperty(value="gameId")
    public int getEventId() {
        return _eventId;
    }

    @JsonProperty(value="gameId")
    public void setEventId(int eventId) {
        _eventId = eventId;
    }

    public List<Integer> getAppIds()
    {
        return _appIds;
    }

    public void setAppIds(List<Integer> appIds)
    {
        _appIds = appIds;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
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

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

    @JsonProperty(value="expectedStartDate")
    public Date getExpectedEventStartDateTime() {
        return _expectedEventStartDateTime;
    }

    @JsonProperty(value="expectedStartDate")
    public void setExpectedEventStartDateTime(Date expectedEventStartDateTime) {
        _expectedEventStartDateTime = expectedEventStartDateTime;
    }
    
    @JsonProperty(value="expectedStopDate")
    public void setExpectedEventStopDateTime(Date expectedEventStopDateTime) {
        _expectedEventStopDateTime = expectedEventStopDateTime;
    }

    @JsonProperty(value="expectedStopDate")
    public Date getExpectedEventStopDateTime() {
        return _expectedEventStopDateTime;
    }

    @JsonIgnore
    public Date getCreatedDate()
    {
        return _createdDate;
    }

    @JsonIgnore
    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public void setKeyword(String keyword) {
        _keyword = keyword;
    }

    public String getKeyword() {
        return _keyword;
    }

    @JsonProperty(value="isPrivate")
    public void setPrivate(boolean _private)
    {
        this._private = _private;
    }

    @JsonProperty(value="isPrivate")
    public boolean isPrivate()
    {
        return _private;
    }

    public void setActualStartDate(Date actualStartDate)
    {
        _actualStartDate = actualStartDate;
    }

    public Date getActualStartDate()
    {
        return _actualStartDate;
    }

    public void setActualStopDate(Date actualStopDate)
    {
        _actualStopDate = actualStopDate;
    }

    public Date getActualStopDate()
    {
        return _actualStopDate;
    }

    public void setSponsor(String sponsor)
    {
        _sponsor = sponsor;
    }

    public String getSponsor()
    {
        return _sponsor;
    }

    @JsonIgnore
    public void setCoupon(String coupon)
    {
        _coupon = coupon;
    }

    @JsonIgnore
    public String getCoupon()
    {
        return _coupon;
    }

    public void setLeagueId(int leagueId)
    {
        _leagueId = leagueId;
    }

    public int getLeagueId()
    {
        return _leagueId;
    }

    public void setHighlighted(boolean highlighted)
    {
        _highlighted = highlighted;
    }

    public boolean isHighlighted()
    {
        return _highlighted;
    }

    public void clearTeams()
    {
        _teams = new ArrayList<Team>();
    }
    
    public void addTeam(Team team)
    {
        _teams.add(team);
    }
    
    public List<Team> getTeams()
    {
        return _teams;
    }
    
    public void setTeams(List<Team> teams) {
        _teams = teams;
    }

    public void setNumPlayers(int numPlayers)
    {
        _numPlayers = numPlayers;
    }

    public int getNumPlayers()
    {
        return _numPlayers;
    }

    public List<Question> getQuestions() {
        return _questions;
    }

    public void setQuestions(List<Question> polls) {
        _questions = polls;
    }

    public void setMinimumGamePurse(double minimumGamePurse)
    {
        _minimumGamePurse = minimumGamePurse;
    }

    public double getMinimumGamePurse()
    {
        return _minimumGamePurse;
    }

    public String getScoringData()
    {
        return _scoringData;
    }

    public void setScoringData(String scoringData)
    {
        _scoringData = scoringData;
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

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        _lastUpdated = lastUpdated;
    }

    public Integer getFiftyPctCampaignId()
    {
        return _fiftyPctCampaignId;
    }

    public void setFiftyPctCampaignId(Integer fiftyPctCampaignId)
    {
        _fiftyPctCampaignId = fiftyPctCampaignId;
    }

    public List<EventMediaRef> getEventMedia() {
        return _eventMedia;
    }

    public void setEventMedia(List<EventMediaRef> eventMedia) {
        _eventMedia = eventMedia;
    }

    public List<ClockFreezeSponsor> getClockFreezeSponsors()
    {
        return _clockFreezeSponsors;
    }

    public void setClockFreezeSponsors(List<ClockFreezeSponsor> clockFreezeSponsors)
    {
        _clockFreezeSponsors = clockFreezeSponsors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event)
            return _eventId == ((Event)obj)._eventId;
        return false;
    }
}
