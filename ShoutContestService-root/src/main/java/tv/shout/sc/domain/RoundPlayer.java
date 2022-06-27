package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

public class RoundPlayer
implements Serializable
{
	public static enum ROUND_PLAYER_DETERMINATION {
        WON, LOST, SAVED, TIMEDOUT, ABANDONED, UNKNOWN, CANCELLED
    }

    private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameId;
    private String _roundId;
    private long _subscriberId;
    private Integer _playedMatchCount;
    private ROUND_PLAYER_DETERMINATION _determination;
    private String _receiptId;
    private Double _amountPaid;
    private Boolean _refunded;
    private Double _skillAnswerCorrectPct;
    private Long _skillAverageAnswerMs;
    private Integer _rank;
    private Double _skill;
    private Date _createDate;

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "sId: {0,number,#}, determination: {1}, rank: {2}",
            _subscriberId, _determination, _rank
        );
    }

    public RoundPlayer(){}

    public RoundPlayer(String gameId, String roundId, long subscriberId)
    {
		_id = UUID.randomUUID().toString();
		_gameId = gameId;
		_roundId = roundId;
		_subscriberId = subscriberId;
		_createDate = new Date();
		_determination = ROUND_PLAYER_DETERMINATION.UNKNOWN;
	}

    public String getId(){
        return _id;
    }
    public void setId(String id){
        _id = id;
    }

	public String getGameId() {
		return _gameId;
	}
	public void setGameId(String gameId) {
		_gameId = gameId;
	}

	public String getRoundId() {
		return _roundId;
	}
	public void setRoundId(String roundId) {
		_roundId = roundId;
	}

	public long getSubscriberId() {
		return _subscriberId;
	}
	public void setSubscriberId(long subscriberId) {
		_subscriberId = subscriberId;
	}

	public Integer getPlayedMatchCount() {
		return _playedMatchCount;
	}
	public void setPlayedMatchCount(Integer playedMatchCount) {
		_playedMatchCount = playedMatchCount;
	}

	public ROUND_PLAYER_DETERMINATION getDetermination() {
		return _determination;
	}
	public void setDetermination(ROUND_PLAYER_DETERMINATION determination) {
		_determination = determination;
	}

	public String getReceiptId() {
		return _receiptId;
	}
	public void setReceiptId(String receiptId) {
		_receiptId = receiptId;
	}

	public Double getAmountPaid() {
		return _amountPaid;
	}
	public void setAmountPaid(Double amountPaid) {
		_amountPaid = amountPaid;
	}

    public Boolean isRefunded() {
		return _refunded;
	}
	public void setRefunded(Boolean refunded) {
		_refunded = refunded;
	}

	public Double getSkillAnswerCorrectPct()
    {
        return _skillAnswerCorrectPct;
    }

    public void setSkillAnswerCorrectPct(Double skillAnswerCorrectPct)
    {
        _skillAnswerCorrectPct = skillAnswerCorrectPct;
    }

    public Long getSkillAverageAnswerMs()
    {
        return _skillAverageAnswerMs;
    }

    public void setSkillAverageAnswerMs(Long skillAverageAnswerMs)
    {
        _skillAverageAnswerMs = skillAverageAnswerMs;
    }

    public Integer getRank() {
		return _rank;
	}
	public void setRank(Integer rank) {
		_rank = rank;
	}

    public Double getSkill()
    {
        return _skill;
    }

    public void setSkill(Double skill)
    {
        _skill = skill;
    }

    public Date getCreateDate() {
		return _createDate;
	}
	public void setCreateDate(Date createDate) {
		_createDate = createDate;
	}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof RoundPlayer))
            return false;
        else
            return _id.equals( ((RoundPlayer)obj).getId() );
    }}
