package com.meinc.gameplay.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JSONQuestionScore implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8896403826936922079L;
    @JsonIgnore
    private QuestionScore _score;
    
    public JSONQuestionScore() {
        
    }
    
    public JSONQuestionScore(QuestionScore score) {
        _score = score;
    }
    
    public int getQuestionId() {
        return _score.getQuestion().getQuestionId();
    }
    
    public int getPoints() {
        return _score.getScore();
    }
}
