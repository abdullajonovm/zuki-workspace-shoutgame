package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Top level object returned when fetching event results
 */
public class EventResults 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _eventId;
    private List<LocalizedValue> _eventName;
    private Date _eventDate;
    private List<ResultsGroup> _results = new ArrayList<ResultsGroup>();
    
    @JsonIgnore
    private boolean _notYetCalculated = false;

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public List<LocalizedValue> getEventName()
    {
        return _eventName;
    }

    public void setEventName(List<LocalizedValue> eventName)
    {
        _eventName = eventName;
    }

    public Date getEventDate()
    {
        return _eventDate;
    }

    public void setEventDate(Date eventDate)
    {
        _eventDate = eventDate;
    }

    public List<ResultsGroup> getResults()
    {
        return _results;
    }

    public void addResult(ResultsGroup result)
    {
        _results.add(result);
    }
    
    public boolean isNotYetCalculated()
    {
        return _notYetCalculated;
    }

    public void setNotYetCalculated(boolean notYetCalculated)
    {
        _notYetCalculated = notYetCalculated;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("eventId: ").append(_eventId);
        buf.append(", eventDate: ").append(_eventDate);
        buf.append(", eventName: ").append(_eventName);
        buf.append("\nRESULTS:");
        for (ResultsGroup group : _results) {
            buf.append("\n\n").append(group);
        }
        
        return buf.toString();
    }
}
