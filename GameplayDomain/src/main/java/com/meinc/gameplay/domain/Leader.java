package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.meinc.identity.domain.Subscriber;

public class Leader 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** special value. if this is the value of totalScore, it means the subscriber is NOT playing the event (and will not have any entries in the scores list) */
    public static final int SCORE_NOT_PLAYING = -100;
    
    private Subscriber _subscriber;
    private int _currentRank;
    private int _previousRank;
    private int _totalScore;
    private List<QuestionScore> _scores;
    
    public Leader()
    {
    }
    
    public Leader clone() {
        Leader l = new Leader();
        l.setSubscriber(_subscriber);
        l.setCurrentRank(_currentRank);
        l.setPreviousRank(_previousRank);
        l.setTotalScore(_totalScore);
        ArrayList<QuestionScore> scores = new ArrayList<QuestionScore>(_scores.size());
        for (QuestionScore oldScore : _scores)
            scores.add(oldScore.clone());
        l.setScores(scores);
        return l;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        _subscriber = subscriber;
    }

    public Subscriber getSubscriber()
    {
        return _subscriber;
    }

    public void setCurrentRank(int currentRank)
    {
        _currentRank = currentRank;
    }

    public int getCurrentRank()
    {
        return _currentRank;
    }

    public void setPreviousRank(int previousRank)
    {
        _previousRank = previousRank;
    }

    public int getPreviousRank()
    {
        return _previousRank;
    }

    public void setTotalScore(int totalScore)
    {
        _totalScore = totalScore;
    }

    public int getTotalScore()
    {
        return _totalScore;
    }

    public void setScores(List<QuestionScore> scores)
    {
        _scores = scores;
    }

    public List<QuestionScore> getScores()
    {
        return _scores;
    }
    
    @Override
    public String toString() {
        return String.format("{Subscriber=%s,Score=%d,Rank=%d,Scores=%s", _subscriber, _totalScore, _currentRank, _scores);
    }
}
