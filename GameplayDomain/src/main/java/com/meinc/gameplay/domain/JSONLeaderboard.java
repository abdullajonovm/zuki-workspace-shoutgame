package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JSONLeaderboard implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6990820753110829943L;
    @JsonIgnore
    private Leaderboard _leaderboard;
    private List<Question> _questions;
    private List<JSONLeader> _leaders;
    private boolean _hasQuestions;
    private boolean _hasLeaders;
    
    public JSONLeaderboard() {
        _hasQuestions = true;
        _hasLeaders = true;
    }
    
    public JSONLeaderboard(Leaderboard leaderboard) {
        _leaderboard = leaderboard;
        _hasQuestions = true;
        _hasLeaders = true;
    }

    public void setLeaderboard(Leaderboard leaderboard) {
        _leaderboard = leaderboard;
    }
    
    public int getEventId() {
        return _leaderboard.getEventId();
    }
    
    public Integer getVipBoxId() {
        return _leaderboard.getVipBoxId();
    }
    
    public Date getLastUpdated() {
        return _leaderboard.getLastUpdated();
    }
    
    public List<Question> getQuestions() {
        if (_questions == null && _hasQuestions) {
            List<Leader> leaders = _leaderboard.getLeaders().getLeaders();
            if (leaders != null && leaders.size() > 0) {
                Leader l = leaders.get(0);
                List<QuestionScore> scores = l.getScores();
                if (scores != null) {
                    _questions = new ArrayList<Question>(scores.size());
                    for (QuestionScore qs : scores) {
                        _questions.add(qs.getQuestion());
                    }
                }
                else {
                    _hasQuestions = false;
                }
            }
            else {
                _hasQuestions = false;
            }
        }
        return _questions;
    }
    
    public List<JSONLeader> getLeaders() {
        if (_leaders == null && _hasLeaders) {
            List<Leader> leaders = _leaderboard.getLeaders().getLeaders();
            if (leaders != null && leaders.size() > 0) {
                _leaders = new ArrayList<JSONLeader>(leaders.size());
                for (Leader l : leaders) {
                    _leaders.add(new JSONLeader(l));
                }
            }
            else {
                _hasLeaders = false;
            }
        }
        return _leaders;
    }
    
    public List<VipBoxScore> getVipBoxScores() {
        List<VipBoxScore> scores = _leaderboard.getLeaders().getVipBoxScores();
        if (scores == null) {
            return new ArrayList<VipBoxScore>();
        }
        return _leaderboard.getLeaders().getVipBoxScores();
    }
    
}
