package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The purpose of this class is to find the default configuration to use when automatically
 * creating a contest based on a default contest template.
 * 
 * I have an event that has no contests.  I want to get the default config for it...
 * 
 * eventId: 10
 * channelId: 421
 * leagueId: 24
 * 
 * Example:
 * 
 * First, see if there is a specific config for this event: scopeType = 'EVENT', primaryRefId = 10.
 * Since there is, just use it.
 * 
 * Then, see if there is a specific config for the event's channel: scopeType = 'CHANNEL', primaryRefId = 421.
 * There isn't, so see if there is a general channel config: scopeType = 'CHANNEL', primaryRefId = null.
 * There is, so then we merge the config from the event and the channel together with duplicated named attrs
 * in the event config taking precedent in the channel's config.
 * 
 * Then, see if there is a specific config for the event's league: scopeType = 'LEAGUE', primaryRefId = 24.
 * There isn't, so we look for a generic league.... etc.
 */
public class ContestDefaultConfig implements Serializable
{
    private static final long serialVersionUID = -224024825609526656L;

    public static enum SCOPE_TYPE
    {
        EVENT,
        CHANNEL,
        LEAGUE, 
        GLOBAL,
        EVENT_VIPBOX,
        VIPBOX
    };
    
    private int _contestDefaultConfigId;
    private SCOPE_TYPE _scopeType;
    private String _config;
    private Integer _primaryRefId;
    private Integer _vipBoxId;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestDefaultConfig()
    {
    }
    
    public ContestDefaultConfig(SCOPE_TYPE scopeType, String config, Integer primaryRefId, Integer vipBoxId)
    {
        _scopeType = scopeType;
        _config = config;
        _primaryRefId = primaryRefId;
        _vipBoxId = vipBoxId;
    }

    public int getContestDefaultConfigId()
    {
        return _contestDefaultConfigId;
    }

    public void setContestDefaultConfigId(int contestDefaultConfigId)
    {
        _contestDefaultConfigId = contestDefaultConfigId;
    }

    public SCOPE_TYPE getScopeType()
    {
        return _scopeType;
    }

    public void setScopeType(SCOPE_TYPE scopeType)
    {
        _scopeType = scopeType;
    }

    public String getConfig()
    {
        return _config;
    }

    public void setConfig(String config)
    {
        _config = config;
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

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("ContestDefaultConfig{contestDefaultConfigId:").append(_contestDefaultConfigId).append(", scopeType: ").append(_scopeType);
        sb.append(", primaryRefId: ").append(_primaryRefId).append(", vipBoxId: ").append(_vipBoxId).append(", config: ").append(_config).append("}");
        
        return sb.toString();
    }
}
