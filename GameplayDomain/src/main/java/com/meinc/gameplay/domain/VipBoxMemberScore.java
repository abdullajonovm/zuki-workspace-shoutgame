package com.meinc.gameplay.domain;

public class VipBoxMemberScore {
    private int vipboxMemberId;
    private int eventId;
    private int questionId;
    private int scoreDelta;
    
    public int getVipboxMemberId() {
        return vipboxMemberId;
    }

    public void setVipboxMemberId(int vipboxMemberId) {
        this.vipboxMemberId = vipboxMemberId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(int scoreDelta) {
        this.scoreDelta = scoreDelta;
    }
}
