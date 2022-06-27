package com.meinc.gameplay.domain;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QuestionScore 
implements Serializable, Cloneable
{
    private static final Log log = LogFactory.getLog(QuestionScore.class);
    private static final long serialVersionUID = 1L;

    private Question _question;
    private int _score; //the score they received for the answer
    private int _subscriberAnswer; //what did the subscriber answer? (may or may not be the correct answer; it's just what they picked); will be 0 if they didn't answer
    
    public QuestionScore()
    {
    }

    public void setQuestion(Question question)
    {
        _question = question;
    }

    public Question getQuestion()
    {
        return _question;
    }

    public void setScore(int score)
    {
        _score = score;
    }

    public int getScore()
    {
        return _score;
    }

    public void setSubscriberAnswer(int answerId)
    {
        _subscriberAnswer = answerId;
    }
    
    public int getSubscriberAnswer()
    {
        return _subscriberAnswer;
    }
    
    @Override
    public String toString() {
        return String.format("<%s,%d,%d>", _question, _subscriberAnswer, _score);
    }
    
    @Override
    public QuestionScore clone() {
        try {
            return (QuestionScore) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
}
