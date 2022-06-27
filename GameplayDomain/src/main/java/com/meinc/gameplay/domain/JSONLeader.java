package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JSONLeader implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -920120172719348443L;
    @JsonIgnore
    private Leader _leader;
    private List<JSONQuestionScore> _scores;

    public JSONLeader() {
    }

    public JSONLeader(Leader leader) {
        _leader = leader;
    }

    public long getSubscriberId() {
        return _leader.getSubscriber().getSubscriberId();
    }

    public String getName() {
        return _leader.getSubscriber().getNickname();
    }

    public int getRank() {
        return _leader.getCurrentRank();
    }

    public int getRankDelta() {
        return _leader.getPreviousRank() - _leader.getCurrentRank();
    }

    public int getTotalScore() {
        return _leader.getTotalScore();
    }

    public List<JSONQuestionScore> getQuestionScores() {
        if (_scores == null) {
            List<QuestionScore> scores = _leader.getScores();
            if (scores != null && scores.size() > 0) {
                _scores = new ArrayList<JSONQuestionScore>(scores.size());
                for (QuestionScore s : scores) {
                    _scores.add(new JSONQuestionScore(s));
                }
            }
            else {
                _scores = new ArrayList<JSONQuestionScore>();
            }
        }
        return _scores;
    }
}
