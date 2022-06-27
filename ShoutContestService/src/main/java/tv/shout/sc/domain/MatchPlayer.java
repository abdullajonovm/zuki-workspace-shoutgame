package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MatchPlayer
implements Serializable
{
    public enum MATCH_PLAYER_DETERMINATION {
        UNKNOWN,    // Not yet scored
        WON,        // Won the match
        LOST,       // Lost the match
        SAVED,      // lost the match, but was saved and gets to continue
        CANCELLED   //the game was cancelled
    }

	private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameId;
    private String _roundId;
    private String _matchId;
    private String _roundPlayerId;
    private long _subscriberId;
    private MATCH_PLAYER_DETERMINATION _determination;
    private Double _score;
    private Date _createDate;

    public MatchPlayer() {}

    public MatchPlayer(String gameId, String roundId, String matchId, String roundPlayerId, long subscriberId)
    {
        _id = UUID.randomUUID().toString();
        _gameId = gameId;
        _roundId = roundId;
        _matchId = matchId;
        _roundPlayerId = roundPlayerId;
        _subscriberId = subscriberId;
        _determination = MATCH_PLAYER_DETERMINATION.UNKNOWN;
        _createDate = new Date();
    }

    public String getId(){
        return this._id;
    }
    public void setId(String id){
        this._id = id;
    }

	public String getGameId() {
		return _gameId;
	}
	public void setGameId(String gameId) {
		this._gameId = gameId;
	}

	public String getRoundId() {
		return _roundId;
	}
	public void setRoundId(String roundId) {
		this._roundId = roundId;
	}

	public String getMatchId() {
		return _matchId;
	}
	public void setMatchId(String matchId) {
		this._matchId = matchId;
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

	public MATCH_PLAYER_DETERMINATION getDetermination() {
		return _determination;
	}
	public void setDetermination(MATCH_PLAYER_DETERMINATION determination) {
		this._determination = determination;
	}

	public Double getScore() {
		return _score;
	}
	public void setScore(Double score) {
		this._score = score;
	}

	public Date getCreateDate() {
		return _createDate;
	}
	public void setCreateDate(Date createDate) {
		this._createDate = createDate;
	}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MatchPlayer))
            return false;
        else
            return _id.equals( ((MatchPlayer)obj).getId() );
    }
}
