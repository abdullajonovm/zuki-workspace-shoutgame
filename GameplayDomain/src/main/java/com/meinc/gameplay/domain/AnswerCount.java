package com.meinc.gameplay.domain;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class AnswerCount implements Serializable
{
    private static final long serialVersionUID = 8564537907132698272L;
    
    private int _questionId;
    private int _answerId;
    private int _numResponses;
    private double _percentage;
    
    public AnswerCount()
    {
    }

    @JsonIgnore
    public int getQuestionId() {
        return _questionId;
    }

    public void setQuestionId(int questionId) {
        _questionId = questionId;
    }

    public void setAnswerId(int answerId)
    {
        _answerId = answerId;
    }
    
    @JsonProperty(value="answerId")
    public int getAnswerId()
    {
        return _answerId;
    }

    public void setNumResponses(int numResponses)
    {
        _numResponses = numResponses;
    }

    @JsonIgnore
    public int getNumResponses()
    {
        return _numResponses;
    }
    
    @JsonProperty(value="percentChoseThisAnswer")    
    public double getPercentage() {
        return _percentage;
    }

    public void setPercentage(double percentage) {
        _percentage = percentage;
    }
    
    @Override
    public String toString() {
        return String.format("%d[%d] %.14g", _answerId, _numResponses, _percentage * 100);
    }
}
