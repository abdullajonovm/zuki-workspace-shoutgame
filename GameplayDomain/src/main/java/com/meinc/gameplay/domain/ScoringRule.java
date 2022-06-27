package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class ScoringRule implements Serializable
{
    private static final long serialVersionUID = 9094981820396407253L;
    
    private int _scoringRuleId;
    private int _scoringRuleTypeId;
    private String _name;
    private String _algorithmName;
    private String _description;
    private String _configJsonSchema;
    private boolean _active;
    private Date _createdDate;
    private Date _lastUpdated;

    public ScoringRule()
    {
    }

    public ScoringRule(int scoringRuleTypeId, String name, String algorithmName, String description,
                       String configJsonSchema)
    {
        _scoringRuleTypeId = scoringRuleTypeId;
        _name = name;
        _algorithmName = algorithmName;
        _description = description;
        _configJsonSchema = configJsonSchema;
    }

    public int getScoringRuleId()
    {
        return _scoringRuleId;
    }

    public void setScoringRuleId(int scoringRuleId)
    {
        _scoringRuleId = scoringRuleId;
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

    public String getAlgorithmName()
    {
        return _algorithmName;
    }

    public void setAlgorithmName(String algorithmName)
    {
        _algorithmName = algorithmName;
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
        
        sb.append("ScoringRule{scoringRuleId: ").append(_scoringRuleId).append(", scoringRuleTypeId: ").append(_scoringRuleTypeId);
        sb.append(", name: ").append(_name).append(", algorithmName: ").append(_algorithmName).append(", description: ").append(_description);
        sb.append(", configJsonSchema: ").append(_configJsonSchema).append(", active: ").append(_active).append("}");
        
        return sb.toString();
    }
}
