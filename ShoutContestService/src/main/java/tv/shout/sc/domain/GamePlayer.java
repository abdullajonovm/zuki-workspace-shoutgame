package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

/**
 * <pre>
{
  "createdDate": "2016-10-11T16:19:28.371+0000",
  "determination": "AWARDED",
  "gameId": "32bad04d-e96a-4fea-b4f9-6594b58595e6",
  "id": "88686d5e-f31d-40ab-bc0f-53783503cd2d",
  "isPayoutCompleted": true,
  "lastRoundId": "whatever",
  "nextRoundId": null,
  "payoutAwardedAmount": 25,
  "payoutPaymentId": "1",
  "payoutVenue": "PAYPAL",
  "rank": 2,
  "subscriberId": 8,
  "freePlay": false
}
 * </pre>
 */
public class GamePlayer
implements Serializable
{
	public static enum GAME_PLAYER_DETERMINATION {
        INPLAY,     // actively playing the game
        SIDELINES,  // sitting out a round (but still in the game)
        ELIMINATED, // no longer eligible to play rounds (you were eliminated)
        AWARDED,    // game is over, you have been awarded your winnings (if any)
        REMOVED,    // if a player "unjoins" a game - retains history
        CANCELLED   // the game was cancelled
    }

    private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameId;
    private long _subscriberId; // (mysql)
    private boolean _freeplay;
    private Integer _rank;
//    private Double _skill;
    private String _payoutPaymentId; // Receipt Id in receipts table (mysql)
    private Double _payoutAwardedAmount;
    private String _payoutVenue;
    private boolean _payoutCompleted;
    private GAME_PLAYER_DETERMINATION _determination;
    private Integer _countdownToElimination; //how many times can the player lose in bracket rounds before being eliminated; null=no limit
    private Integer _totalLives; //how many lives the player initially had
    private String _nextRoundId;
    private String _lastRoundId;
    private Date _createDate;

    public GamePlayer() {}

    public GamePlayer(String gameId, long subscriberId)
    {
    	_id = UUID.randomUUID().toString();
		_gameId = gameId;
		_subscriberId = subscriberId;
		_createDate = new Date();
		_determination = GAME_PLAYER_DETERMINATION.INPLAY;
	}

    public String getId() {
        return this._id;
    }
    public void setId(String id) {
        this._id = id;
    }

	public String getGameId() {
		return _gameId;
	}
	public void setGameId(String gameId) {
		this._gameId = gameId;
	}

    public long getSubscriberId() {
		return _subscriberId;
	}
	public void setSubscriberId(long subscriberId) {
		this._subscriberId = subscriberId;
	}

	public boolean isFreeplay()
    {
        return _freeplay;
    }

    public void setFreeplay(boolean freeplay)
    {
        _freeplay = freeplay;
    }

    public Integer getRank() {
		return _rank;
	}
	public void setRank(Integer rank) {
		this._rank = rank;
	}

//    public Double getSkill()
//    {
//        return _skill;
//    }
//
//    public void setSkill(Double skill)
//    {
//        _skill = skill;
//    }

    public String getPayoutPaymentId() {
		return _payoutPaymentId;
	}
	public void setPayoutPaymentId(String payoutPaymentId) {
		this._payoutPaymentId = payoutPaymentId;
	}

    public String getPayoutVenue() {
		return _payoutVenue;
	}
	public void setPayoutVenue(String payoutVenue) {
		this._payoutVenue = payoutVenue;
	}

    public Double getPayoutAwardedAmount() {
		return _payoutAwardedAmount;
	}
	public void setPayoutAwardedAmount(Double payoutAwardedAmount) {
		this._payoutAwardedAmount = payoutAwardedAmount;
	}

    public boolean isPayoutCompleted() {
		return _payoutCompleted;
	}
	public void setPayoutCompleted(boolean payoutCompleted) {
		_payoutCompleted = payoutCompleted;
	}

    public GAME_PLAYER_DETERMINATION getDetermination() {
		return _determination;
	}
	public void setDetermination(GAME_PLAYER_DETERMINATION determination) {
		this._determination = determination;
	}

    public Integer getCountdownToElimination()
    {
        return _countdownToElimination;
    }

    public void setCountdownToElimination(Integer countdownToElimination)
    {
        _countdownToElimination = countdownToElimination;
    }

    public Integer getTotalLives()
    {
        return _totalLives;
    }

    public void setTotalLives(Integer totalLives)
    {
        _totalLives = totalLives;
    }

    public String getNextRoundId() {
		return _nextRoundId;
	}
	public void setNextRoundId(String nextRoundId) {
		this._nextRoundId = nextRoundId;
	}

    public String getLastRoundId() {
		return _lastRoundId;
	}
	public void setLastRoundId(String lastRoundId) {
		this._lastRoundId = lastRoundId;
	}

    public Date getCreateDate() {
		return _createDate;
	}
	public void setCreateDate(Date createDate) {
		this._createDate = createDate;
	}

	@Override
	public String toString()
	{
	    return MessageFormat.format("id: {0}, gameId: {1}, sId: {2,number,#}, freeplay: {3}, determination: {4}, payout: {5}, nextRoundId: {6}",
	            _id, _gameId, _subscriberId, _freeplay, _determination, _payoutAwardedAmount, _nextRoundId);
	}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof GamePlayer))
            return false;
        else
            return _id.equals( ((GamePlayer)obj).getId() );
    }

}
