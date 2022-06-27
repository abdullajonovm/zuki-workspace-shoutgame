package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class SubscriberQuestionAnswer
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int _questionId;
    private List<Integer> _answerIds;
    
    public SubscriberQuestionAnswer()
    {
    }

    public void setQuestionId(int questionId)
    {
        _questionId = questionId;
    }

    public int getQuestionId()
    {
        return _questionId;
    }

    public void setAnswersId(List<Integer> answerIds)
    {
        _answerIds = answerIds;
    }

    public List<Integer> getAnswerIds()
    {
        return _answerIds;
    }
}
