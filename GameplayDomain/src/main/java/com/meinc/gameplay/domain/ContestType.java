package com.meinc.gameplay.domain;

import java.io.Serializable;

public class ContestType implements Serializable
{
    private static final long serialVersionUID = -8785049608273596081L;
    
    private String _name;
    private Integer _scoringRuleId;
    private Integer _payoutRuleId;
    
    public ContestType()
    {
    }
    
    public ContestType(String name, Integer scoringRuleId, Integer payoutRuleId)
    {
        _name = name;
        _scoringRuleId = scoringRuleId;
        _payoutRuleId = payoutRuleId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public Integer getScoringRuleId()
    {
        return _scoringRuleId;
    }

    public void setScoringRuleId(Integer scoringRuleId)
    {
        _scoringRuleId = scoringRuleId;
    }

    public Integer getPayoutRuleId()
    {
        return _payoutRuleId;
    }

    public void setPayoutRuleId(Integer payoutRuleId)
    {
        _payoutRuleId = payoutRuleId;
    }
}
