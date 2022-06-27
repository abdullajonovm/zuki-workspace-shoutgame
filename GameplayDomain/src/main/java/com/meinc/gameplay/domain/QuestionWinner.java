package com.meinc.gameplay.domain;

import java.io.Serializable;

public class QuestionWinner 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public int subscriberId;
    public String nickname;
    public int contestId;
    public int questionId;
    public WinnerCoupon coupon;
    
    public QuestionWinner()
    {
    }
    
    public QuestionWinner(int subscriberId, int contestId, int questionId)
    {
        this.subscriberId = subscriberId;
        this.contestId = contestId;
        this.questionId = questionId;
    }
    
}
