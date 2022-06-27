package com.meinc.gameplay.domain;

/**
 * Useful statistical information about an event such as 
 * how many people played, etc..
 */
public class EventUsage
{
    public static final int NO_DATA = -1;
    
    private Event _event;
    private int _totalPlayers = NO_DATA;
    private int _newPlayers = NO_DATA;
    private int _skipped = NO_DATA;
    private int _canceled = NO_DATA;
    
    public EventUsage()
    {
    }

    public Event getEvent()
    {
        return _event;
    }

    public void setEvent(Event event)
    {
        _event = event;
    }

    public int getTotalPlayers()
    {
        return _totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers)
    {
        _totalPlayers = totalPlayers;
    }

    public int getNewPlayers()
    {
        return _newPlayers;
    }

    public void setNewPlayers(int newPlayers)
    {
        _newPlayers = newPlayers;
    }

    public int getSkipped()
    {
        return _skipped;
    }

    public void setSkipped(int skipped)
    {
        _skipped = skipped;
    }

    public int getCanceled()
    {
        return _canceled;
    }

    public void setCanceled(int canceled)
    {
        _canceled = canceled;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("EVENT: ").append(_event.getName()).append(" (").append(_event.getKeyword()).append(") [").append(_event.getEventId()).append("]\n");
        buf.append("\tStart: ").append(_event.getActualStartDate()).append("\tStop: ").append(_event.getActualStopDate()).append("\n");
        buf.append("\tTotal Players: ").append(_totalPlayers == NO_DATA ? "NO DATA" : _totalPlayers).append("\n");
        buf.append("\tNew Players: ").append(_newPlayers == NO_DATA ? "NO DATA" : _newPlayers).append("\n");
        buf.append("\tSkipped: ").append(_skipped == NO_DATA ? "NO DATA" : _skipped).append("\n");
        buf.append("\tCanceled: ").append(_canceled == NO_DATA ? "NO DATA" : _canceled).append("\n");
        
        return buf.toString();
    }
    
}
