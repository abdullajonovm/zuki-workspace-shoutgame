package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

public class MatchQueue
implements Serializable
{
    /* If a player is in this table then they had to have
     *   been previously filtered by the game, round filters
     *   (language, country, app)
     * */
	private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameId;
    private String _roundId;
    private String _roundPlayerId;
    private long _subscriberId;
    private boolean _cancelled;
    private Date _enqueueTimestamp;
    private Date _dequeueTimestamp;
//    private boolean _demoStore;

    @SuppressWarnings("unused")
    private MatchQueue() {}

    public MatchQueue(String id, String gameId, String roundId, String roundPlayerId, long subscriberId/*, boolean demoStore*/)
    {
        _id = id;
        _gameId = gameId;
        _roundId = roundId;
        _roundPlayerId = roundPlayerId;
        _subscriberId = subscriberId;
        _enqueueTimestamp = new Date();
//        setDemoStore(demoStore);
    }

    public String getId()
    {
        return this._id;
    }
    public void setId(String id)
    {
        this._id = id;
    }

	public String getGameId()
    {
		return _gameId;
	}
	public void setGameId(String gameId)
	{
		this._gameId = gameId;
	}

	public String getRoundId() {
		return _roundId;
	}
	public void setRoundId(String roundId) {
		this._roundId = roundId;
	}

	public String getRoundPlayerId() {
		return _roundPlayerId;
	}
	public void setRoundPlayerId(String roundPlayerId) {
		this._roundPlayerId = roundPlayerId;
	}

    public long getSubscriberId() {
		return _subscriberId;
	}
	public void setSubscriberId(long subscriberId) {
		this._subscriberId = subscriberId;
	}

    public boolean isCancelled() {
		return _cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this._cancelled = cancelled;
	}

	public Date getEnqueueTimestamp() {
		return _enqueueTimestamp;
	}
	public void setEnqueueTimestamp(Date enqueueTimestamp) {
		this._enqueueTimestamp = enqueueTimestamp;
	}

	public Date getDequeueTimestamp() {
		return _dequeueTimestamp;
	}
	public void setDequeueTimestamp(Date dequeueTimestamp) {
		this._dequeueTimestamp = dequeueTimestamp;
	}

//	public boolean isDemoStore()
//    {
//        return _demoStore;
//    }
//    public void setDemoStore(boolean demoStore)
//    {
//        _demoStore = demoStore;
//    }

    @Override
	public String toString()
	{
	    return MessageFormat.format(
	            "rId: {0}, sId: {1,number,#}, enqueued: {2}, dequeued: {3}, cancelled: {4}",
	            _roundId, _subscriberId, _enqueueTimestamp, _dequeueTimestamp, _cancelled);
	}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MatchQueue))
            return false;
        else
            return _id.equals( ((MatchQueue)obj).getId() );
    }
}
