package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Contains a question.  If the subscriber has answered the question, it will also contain the answer id.
 */
public class QuestionWithSubscriberAnswer 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Question _question;
    private List<Integer> _answerIds;
    private Integer _eventId;
    private QuestionMediaRef _beforeMedia;
    private QuestionMediaRef _afterMedia;
    
    public QuestionWithSubscriberAnswer()
    {
    }

    public Question getQuestion()
    {
        return _question;
    }

    public void setQuestion(Question question)
    {
        _question = question;
    }

    public List<Integer> getAnswerIds()
    {
        return _answerIds;
    }

    public void setAnswerIds(List<Integer> answerIds)
    {
        _answerIds = answerIds;
    }

    public Integer getEventId() {
        return _eventId;
    }

    public void setEventId(Integer eventId)
    {
        _eventId = eventId;
    }

    public QuestionMediaRef getBeforeMedia()
    {
        return _beforeMedia;
    }

    public void setBeforeMedia(QuestionMediaRef beforeMedia)
    {
        _beforeMedia = beforeMedia;
    }

    public QuestionMediaRef getAfterMedia()
    {
        return _afterMedia;
    }

    public void setAfterMedia(QuestionMediaRef afterMedia)
    {
        _afterMedia = afterMedia;
    }

}
