package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ScoringRuleType implements Serializable
{
    private static final long serialVersionUID = 7294967328029698087L;
    
    private int _scoringRuleTypeId;
    private String _name;
    private String _description;
    private String _configJsonSchema;
    private boolean _active;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public ScoringRuleType()
    {
    }
    
    public ScoringRuleType(String name, String description, String configJsonSchema)
    {
        _name = name;
        _description = description;
        _configJsonSchema = configJsonSchema;
    }

    public int getScoringRuleTypeId()
    {
        return _scoringRuleTypeId;
    }

    public void setScoringRuleTypeId(int scoringRuleTypeId)
    {
        _scoringRuleTypeId = scoringRuleTypeId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public String getConfigJsonSchema()
    {
        return _configJsonSchema;
    }

    public void setConfigJsonSchema(String configJsonSchema)
    {
        _configJsonSchema = configJsonSchema;
    }
    
    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
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
        
        sb.append("ScoringRuleType{ruleTypeId: ").append(_scoringRuleTypeId).append(", name: ").append(_name);
        sb.append(", description").append(_description).append(", active: ").append(_active).append("}");
        
        return sb.toString();
    }
}
