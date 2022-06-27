package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class PayoutRuleType implements Serializable
{
    private static final long serialVersionUID = -5070342080588774969L;
    
    private int _payoutRuleTypeId;
    private String _name;
    private String _description;
    private String _configJsonSchema;
    private boolean _active;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public PayoutRuleType()
    {
    }
    
    public PayoutRuleType(String name, String description, String configJsonSchema)
    {
        _name = name;
        _description = description;
        _configJsonSchema = configJsonSchema;
    }

    public int getPayoutRuleTypeId()
    {
        return _payoutRuleTypeId;
    }

    public void setPayoutRuleTypeId(int payoutRuleTypeId)
    {
        _payoutRuleTypeId = payoutRuleTypeId;
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
        
        sb.append("PayoutRuleType{ruleTypeId: ").append(_payoutRuleTypeId).append(", name: ").append(_name);
        sb.append(", description").append(_description).append(", active: ").append(_active).append("}");
        
        return sb.toString();
    }
}
