package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

public class Match
implements Serializable
{
	public static enum MATCH_STATUS { NEW, OPEN, WAITING_FOR_NEXT_QUESTION, WAITING_FOR_TIEBREAKER_QUESTION, PROCESSING, CLOSED, CANCELLED }

	public static enum MATCH_DETERMINATION { WINNER, NO_WINNER, TIE, UNKNOWN }

	private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameEngine;
    private String _engineType;
    private String _gameId;
    private String _roundId;
    private MATCH_STATUS _matchStatus;
    private Date _matchStatusSetAt;
    private Long _wonSubscriberId;
    private int _minimumActivityToWinCount;
    private Integer _maximumActivityCount;
    private Integer _actualActivityCount;
    private Date _sendNextQuestionAt; //if not null, when (timestamp) to send the next question. null=send immediately
    private MATCH_DETERMINATION _determination;
    private Date _startDate;
    private Date _completeDate;
    private Date _createDate;

    public Match() {}

    public Match(String gameId, String roundId, String gameEngine, String engineType, int minimumActivityToWinCount, Integer maximumActivityCount)
    {
        _id = UUID.randomUUID().toString();
        _matchStatus = MATCH_STATUS.NEW;
        _matchStatusSetAt = new Date();
        _gameEngine = gameEngine;
        _engineType = engineType;
        _gameId = gameId;
        _roundId = roundId;
        _startDate = new Date();
        _createDate = new Date();
        _determination = MATCH_DETERMINATION.UNKNOWN;
        _minimumActivityToWinCount = minimumActivityToWinCount;
        _maximumActivityCount = maximumActivityCount;
    }

    public String getId(){
        return this._id;
    }
    public void setId(String id){
        this._id = id;
    }

    public String getGameEngine()
    {
        return _gameEngine;
    }
    public void setGameEngine(String gameEngine)
    {
        _gameEngine = gameEngine;
    }

	public String getEngineType()
    {
        return _engineType;
    }

    public void setEngineType(String engineType)
    {
        _engineType = engineType;
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

    public MATCH_STATUS getMatchStatus()
    {
        return _matchStatus;
    }
    public void setMatchStatus(MATCH_STATUS matchStatus)
    {
        _matchStatus = matchStatus;
    }

    public Date getMatchStatusSetAt()
    {
        return _matchStatusSetAt;
    }

    public void setMatchStatusSetAt(Date matchStatusSetAt)
    {
        _matchStatusSetAt = matchStatusSetAt;
    }

    public Long getWonSubscriberId() {
		return _wonSubscriberId;
	}
	public void setWonSubscriberId(Long wonSubscriberId) {
		this._wonSubscriberId = wonSubscriberId;
	}

	public int getMinimumActivityToWinCount() {
		return _minimumActivityToWinCount;
	}

	public void setMinimumActivityToWinCount(int minimumActivityToWinCount) {
		this._minimumActivityToWinCount = minimumActivityToWinCount;
	}

	public Integer getMaximumActivityCount() {
		return _maximumActivityCount;
	}

	public void setMaximumActivityCount(Integer maximumActivityCount) {
		this._maximumActivityCount = maximumActivityCount;
	}

	public Integer getActualActivityCount() {
		return _actualActivityCount;
	}
	public void setActualActivityCount(Integer actualActivityCount) {
		this._actualActivityCount = actualActivityCount;
	}

    public Date getSendNextQuestionAt()
    {
        return _sendNextQuestionAt;
    }

    public void setSendNextQuestionAt(Date sendNextQuestionAt)
    {
        _sendNextQuestionAt = sendNextQuestionAt;
    }

    public MATCH_DETERMINATION getDetermination()
    {
        return _determination;
    }

    public void setDetermination(MATCH_DETERMINATION determination)
    {
        _determination = determination;
    }

	public Date getStartDate() {
		return _startDate;
	}
	public void setStartDate(Date startDate) {
		this._startDate = startDate;
	}

	public Date getCompleteDate() {
		return _completeDate;
	}
	public void setCompleteDate(Date completeDate) {
		this._completeDate = completeDate;
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
        if (obj == null || !(obj instanceof Match))
            return false;
        else
            return _id.equals( ((Match)obj).getId() );
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
                "id: {0}, status: {1}, setAt: {2,date,yyyy-MM-dd hh:mm:ss.SSS}, determination: {3}",
                _id, _matchStatus, _matchStatusSetAt, _determination);
    }
}
