package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;


public class LeaderboardUpdate implements Serializable
{
    private static final long serialVersionUID = -3596160681047581713L;
    private Date _lastLeaderBoardUpdate;
    private int _eventId;
    private EventLeaderboardData _leaderBoard;
    private boolean _isComplete;
    private boolean _isUpdating;
    
    public Date getLastLeaderBoardUpdate() {
        return _lastLeaderBoardUpdate;
    }
    public void setLastLeaderBoardUpdate(Date lastLeaderBoardUpdate) {
        _lastLeaderBoardUpdate = lastLeaderBoardUpdate;
    }
    public int getEventId() {
        return _eventId;
    }
    public void setEventId(int eventId) {
        _eventId = eventId;
    }
    public EventLeaderboardData getLeaderBoard() {
        return _leaderBoard;
    }
    public void setLeaderBoard(EventLeaderboardData leaderBoard) {
        _leaderBoard = leaderBoard;
    }
    public void setComplete(boolean complete) {
        _isComplete = complete;
    }
    public boolean isComplete() {
        return _isComplete;
    }
    public boolean isUpdating() {
        return _isUpdating;
    }
    public void setUpdating(boolean isUpdating) {
        _isUpdating = isUpdating;
    }
    @Override
    public String toString() {
        return String.format("LeaderboardUpdate for event %d: isComplete=%b, isUpdating=%b, lastUpdate=%s, leaderboard=%s",
                _eventId, _isComplete, _isUpdating, _lastLeaderBoardUpdate, _leaderBoard);
    }
}
