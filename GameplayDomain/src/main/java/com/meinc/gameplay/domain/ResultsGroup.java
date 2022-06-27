package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

/**
 * A grouping of event results (1 on 1 winners, grand prize winners, question winners, contest winners, etc..)
 */
public class ResultsGroup 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static enum ResultsGroupType {CONTEST, CASH, PARENT_GROUP}

    /** the group name (ex: Leaderboard Winners) */
    private List<LocalizedValue> _name;
    
    private List<LocalizedValue> _description;
    
    private ResultsGroupType _type;
    
    /** only present if type == CONTEST */
    private Integer _contestId;
    
    /** may be null */
    private Integer _numWinners;
    
    /** may be null if type == PARENT_GROUP */
    private List<SubscriberResult> _results;
    
    /** may be null if type != PARENT_GROUP */
    private List<ResultsGroup> _subGroups;

    public List<LocalizedValue> getName()
    {
        return _name;
    }

    public void setName(List<LocalizedValue> name)
    {
        _name = name;
    }

    public List<LocalizedValue> getDescription()
    {
        return _description;
    }

    public void setDescription(List<LocalizedValue> description)
    {
        _description = description;
    }

    public ResultsGroupType getType()
    {
        return _type;
    }

    public void setType(ResultsGroupType type)
    {
        _type = type;
    }

    public Integer getContestId()
    {
        return _contestId;
    }

    public void setContestId(Integer contestId)
    {
        _contestId = contestId;
    }

    public Integer getNumWinners()
    {
        return _numWinners;
    }

    public void setNumWinners(Integer numWinners)
    {
        _numWinners = numWinners;
    }

    public List<SubscriberResult> getResults()
    {
        return _results;
    }

    public void setResults(List<SubscriberResult> results)
    {
        _results = results;
    }

    public List<ResultsGroup> getSubGroups()
    {
        return _subGroups;
    }

    public void setSubGroups(List<ResultsGroup> subGroups)
    {
        _subGroups = subGroups;
    }
    
    @Override
    public String toString()
    {
        return toString(0);
    }
    
    private String toString(int level)
    {
        StringBuilder bufIndent = new StringBuilder();
        for (int i=0; i<level; i++)
            bufIndent.append("    ");
        String indent = bufIndent.toString();
        
        StringBuilder buf = new StringBuilder();

        buf.append(indent).append("type: ").append(_type);
        buf.append(indent).append(", name: ").append(_name);
        buf.append(indent).append(", description: ").append(_description);
        if (_contestId != null) buf.append(indent).append(", contestId: ").append(_contestId);
        if (_numWinners != null) buf.append(indent).append(", numWinners: ").append(_numWinners);
        
        if (_results != null && _results.size() > 0) {
            buf.append("\n").append(indent).append("RESULTS:");
            for (SubscriberResult result : _results) {
                buf.append("\n\t").append(indent).append(result);
            }
        }
        
        if (_subGroups != null && _subGroups.size() > 0) {
            buf.append("\n").append(indent).append("SUBGROUPS:");
            for (ResultsGroup subGroup : _subGroups) {
                buf.append("\n").append(subGroup.toString(level+1));
            }
        }
        
        return buf.toString();
    }
    
}
