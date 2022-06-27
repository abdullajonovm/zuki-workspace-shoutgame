package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class QuestionAnswerPercentageResult implements Serializable {
    
    private static final long serialVersionUID = 1541465998236154231L;
    private int _eventId;
    private int _questionId;
    private int _correctAnswerId;
    private String _fanCheckSubscriberEntitlementId;
    private List<AnswerCount> _answerPercents;
    private boolean _success;

    public int getEventId() {
        return _eventId;
    }

    public void setEventId(int eventId) {
        _eventId = eventId;
    }

    public int getQuestionId() {
        return _questionId;
    }

    public void setQuestionId(int questionId) {
        _questionId = questionId;
    }

    public int getCorrectAnswerId() {
        return _correctAnswerId;
    }

    public void setCorrectAnswerId(int correctAnswerId) {
        _correctAnswerId = correctAnswerId;
    }

    public String getFanCheckSubscriberEntitlementId() {
        return _fanCheckSubscriberEntitlementId;
    }

    public void setFanCheckSubscriberEntitlementId(
            String fanCheckSubscriberEntitlementId) {
        _fanCheckSubscriberEntitlementId = fanCheckSubscriberEntitlementId;
    }

    public List<AnswerCount> getAnswerPercents() {
        return _answerPercents;
    }

    public void setAnswerPercents(List<AnswerCount> answerPercents) {
        _answerPercents = answerPercents;
    }

    public boolean isSuccess() {
        return _success;
    }

    public void setSuccess(boolean success) {
        _success = success;
    }
    
}
