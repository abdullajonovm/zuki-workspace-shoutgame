package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import tv.shout.sc.domain.Match.MATCH_DETERMINATION;
import tv.shout.sc.domain.Match.MATCH_STATUS;

//a question shown in a match
@SuppressWarnings("serial")
public class MatchQuestion
implements Serializable
{
    private String _id;
    private String _gameId;
    private String _roundId;
    private String _matchId;
    private String _questionId;
    private String _questionValue; //ala jeopardy: $100, or 25points

    // Other question or match data to help processing?

    private MATCH_STATUS _matchQuestionStatus;
    private Long _wonSubscriberId; // will be null until question is scored, will still be null if no one won.
    private MATCH_DETERMINATION _determination; // will be null until question is scored
    private Date _createDate;
    private Date _completedDate;

    public MatchQuestion() {}

    public MatchQuestion(String gameId, String roundId, String matchId, String questionId, String questionValue)
    {
        _id = UUID.randomUUID().toString();
        _gameId = gameId;
        _roundId = roundId;
        _matchId = matchId;
        _questionId = questionId;
        _questionValue = questionValue;
        _matchQuestionStatus = MATCH_STATUS.OPEN;
        _determination = MATCH_DETERMINATION.UNKNOWN;
        _createDate = new Date();
    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getGameId()
    {
        return _gameId;
    }

    public void setGameId(String gameId)
    {
        _gameId = gameId;
    }

    public String getRoundId()
    {
        return _roundId;
    }

    public void setRoundId(String roundId)
    {
        _roundId = roundId;
    }

    public String getMatchId()
    {
        return _matchId;
    }

    public void setMatchId(String matchId)
    {
        _matchId = matchId;
    }

    public String getQuestionId()
    {
        return _questionId;
    }

    public void setQuestionId(String questionId)
    {
        _questionId = questionId;
    }

    public String getQuestionValue()
    {
        return _questionValue;
    }

    public void setQuestionValue(String questionValue)
    {
        _questionValue = questionValue;
    }

    public MATCH_STATUS getMatchQuestionStatus()
    {
        return _matchQuestionStatus;
    }

    public void setMatchQuestionStatus(MATCH_STATUS matchQuestionStatus)
    {
        _matchQuestionStatus = matchQuestionStatus;
    }

    public Long getWonSubscriberId()
    {
        return _wonSubscriberId;
    }

    public void setWonSubscriberId(Long wonSubscriberId)
    {
        _wonSubscriberId = wonSubscriberId;
    }

    public MATCH_DETERMINATION getDetermination()
    {
        return _determination;
    }

    public void setDetermination(MATCH_DETERMINATION determination)
    {
        _determination = determination;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    public Date getCompletedDate()
    {
        return _completedDate;
    }

    public void setCompletedDate(Date completedDate)
    {
        _completedDate = completedDate;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MatchQuestion))
            return false;
        else
            return _id.equals( ((MatchQuestion)obj).getId() );
    }
}
