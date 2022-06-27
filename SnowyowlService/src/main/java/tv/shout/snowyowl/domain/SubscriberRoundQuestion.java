package tv.shout.snowyowl.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SubscriberRoundQuestion
implements Serializable
{
    private long _subscriberId;
    private String _roundId;
    private String _questionId;
    private int _order;
    private boolean _seen;

    public SubscriberRoundQuestion()
    {

    }

    public SubscriberRoundQuestion(long subscriberId, String roundId, String questionId, int order, boolean seen)
    {
        _subscriberId = subscriberId;
        _roundId = roundId;
        _questionId = questionId;
        _order = order;
        _seen = seen;
    }

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public String getRoundId()
    {
        return _roundId;
    }
    public void setRoundId(String roundId)
    {
        _roundId = roundId;
    }
    public String getQuestionId()
    {
        return _questionId;
    }
    public void setQuestionId(String questionId)
    {
        _questionId = questionId;
    }
    public int getOrder()
    {
        return _order;
    }
    public void setOrder(int order)
    {
        _order = order;
    }
    public boolean isSeen()
    {
        return _seen;
    }
    public void setSeen(boolean seen)
    {
        _seen = seen;
    }


}
