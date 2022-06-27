package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class PayoutRule implements Serializable
{
    private static final long serialVersionUID = 9094981820396407253L;
    
    private int _payoutRuleId;
    private int _payoutRuleTypeId;
    private String _name;
    private String _algorithmName;
    private String _description;
    private String _configJsonSchema;
    private boolean _active;
    private Date _createdDate;
    private Date _lastUpdated;

    public PayoutRule()
    {
    }

    public PayoutRule(int payoutRuleTypeId, String name, String algorithmName, String description,
                       String configJsonSchema)
    {
        _payoutRuleTypeId = payoutRuleTypeId;
        _name = name;
        _algorithmName = algorithmName;
        _description = description;
        _configJsonSchema = configJsonSchema;
    }

    public int getPayoutRuleId()
    {
        return _payoutRuleId;
    }

    public void setPayoutRuleId(int payoutRuleId)
    {
        _payoutRuleId = payoutRuleId;
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
        
        sb.append("PayoutRule{payoutRuleId: ").append(_payoutRuleId).append(", payoutRuleTypeId: ").append(_payoutRuleTypeId);
        sb.append(", name: ").append(_name).append(", algorithmName: ").append(_algorithmName).append(", description: ").append(_description);
        sb.append(", configJsonSchema: ").append(_configJsonSchema).append(", active: ").append(_active).append("}");
        
        return sb.toString();
    }
}
