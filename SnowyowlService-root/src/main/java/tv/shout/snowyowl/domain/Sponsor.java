package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;

public class Sponsor
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long _subscriberId;
    private boolean _busyFlag;
    private String _gameId;
    private int _sponsorCashPoolId;
    private Date _lastUsedDate;

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public boolean isBusyFlag()
    {
        return _busyFlag;
    }
    public void setBusyFlag(boolean busyFlag)
    {
        _busyFlag = busyFlag;
    }
    public String getGameId()
    {
        return _gameId;
    }
    public void setGameId(String gameId)
    {
        _gameId = gameId;
    }
    public int getSponsorCashPoolId()
    {
        return _sponsorCashPoolId;
    }
    public void setSponsorCashPoolId(int sponsorCashPoolId)
    {
        _sponsorCashPoolId = sponsorCashPoolId;
    }
    public Date getLastUsedDate()
    {
        return _lastUsedDate;
    }
    public void setLastUsedDate(Date lastUsedDate)
    {
        _lastUsedDate = lastUsedDate;
    }
}
