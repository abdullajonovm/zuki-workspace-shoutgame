package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;

//this record is created before the question is shown to the user, and then updated after they answer.
@SuppressWarnings("serial")
public class SubscriberQuestionAnswer
implements Serializable
{
    public enum ANSWER_DETERMINATION {
        UNKNOWN,                       //not yet scored
        WON_TIME, LOST_TIME,           //both got it correct, one was faster
        WON_CORRECT, LOST_INCORRECT,   //one got it correct, one got it incorrect
        WON_TIMEOUT, LOST_TIMEOUT,     //one got it correct, one timed out
        LOST_ALL_TIMEOUT               //both timed out
    }

    private String _id;
    private String _gameId;
    private String _roundId;
    private String _matchId;
    private String _questionId;
    private String _matchQuestionId;
    private long _subscriberId;
    private String _selectedAnswerId; //null=not yet answered
    private String _questionDecryptKey;
    private Date _questionPresentedTimestamp; //when the server sent the decrypt key (i.e. this data)
    private Long _durationMilliseconds; //how long it took them to answer
    private ANSWER_DETERMINATION _determination;
    private boolean _won; //did the subscriber win (WON_TIME, WON_CORRECT, WON_TIMEOUT all mean the subscriber won)
    private Date _createDate;

    public SubscriberQuestionAnswer() {}

    public SubscriberQuestionAnswer(long sId, Date shownTimestamp, Long answerDurationMs, ANSWER_DETERMINATION outcome, boolean won)
    {
        _subscriberId = sId;
        _questionPresentedTimestamp = shownTimestamp;
        _durationMilliseconds = answerDurationMs;
        _determination = outcome;
        _won = won;
        _determination = ANSWER_DETERMINATION.UNKNOWN;
    }

    public SubscriberQuestionAnswer(String gameId, String roundId, String matchId, String questionId, String matchQuestionId, long subscriberId, String questionDecryptKey)
    {
        _id = UUID.randomUUID().toString();
        _gameId = gameId;
        _roundId = roundId;
        _matchId = matchId;
        _questionId = questionId;
        _matchQuestionId = matchQuestionId;
        _subscriberId = subscriberId;
        _createDate = new Date();
        _questionDecryptKey = questionDecryptKey;
        _determination = ANSWER_DETERMINATION.UNKNOWN;
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

    public String getMatchQuestionId()
    {
        return _matchQuestionId;
    }

    public void setMatchQuestionId(String matchQuestionId)
    {
        _matchQuestionId = matchQuestionId;
    }

    public long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getSelectedAnswerId()
    {
        return _selectedAnswerId;
    }

    public void setSelectedAnswerId(String selectedAnswerId)
    {
        _selectedAnswerId = selectedAnswerId;
    }

    public String getQuestionDecryptKey()
    {
        return _questionDecryptKey;
    }

    public void setQuestionDecryptKey(String questionDecryptKey)
    {
        _questionDecryptKey = questionDecryptKey;
    }

    public Date getQuestionPresentedTimestamp()
    {
        return _questionPresentedTimestamp;
    }

    public void setQuestionPresentedTimestamp(Date questionPresentedTimestamp)
    {
        _questionPresentedTimestamp = questionPresentedTimestamp;
    }

    public Long getDurationMilliseconds()
    {
        return _durationMilliseconds;
    }

    public void setDurationMilliseconds(Long durationMilliseconds)
    {
        _durationMilliseconds = durationMilliseconds;
    }

    public ANSWER_DETERMINATION getDetermination()
    {
        return _determination;
    }

    public void setDetermination(ANSWER_DETERMINATION determination)
    {
        _determination = determination;
    }

    public boolean isWon()
    {
        return _won;
    }

    public void setWon(boolean won)
    {
        _won = won;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("sId: {0}, determination: {1}, sqaId: {2}", _subscriberId, _determination, _id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof SubscriberQuestionAnswer))
            return false;
        else
            return _id.equals( ((SubscriberQuestionAnswer)obj).getId() );
    }
}
