package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;

public class SubscriberStats
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum STATS_TYPE { GAMES_PLAYED, BRACKET_ROUNDS_PLAYED, POOL_ROUNDS_PLAYED, QUESTIONS_ANSWERED, QUESTIONS_CORRECT, CUMULATIVE_QUESTION_SCORE, AFFILIATE_PLAN_ID }

    private long _subscriberId;
    private int _gamesPlayed;
    private int _bracketRoundsPlayed;
    private int _poolRoundsPlayed;
    private int _questionsAnswered;
    private int _questionsCorrect;
    private int _cumulativeQuestionScore;
    private int _affiliatePlanId;

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public int getGamesPlayed()
    {
        return _gamesPlayed;
    }
    public void setGamesPlayed(int gamesPlayed)
    {
        _gamesPlayed = gamesPlayed;
    }
    public int getBracketRoundsPlayed()
    {
        return _bracketRoundsPlayed;
    }
    public void setBracketRoundsPlayed(int bracketRoundsPlayed)
    {
        _bracketRoundsPlayed = bracketRoundsPlayed;
    }
    public int getPoolRoundsPlayed()
    {
        return _poolRoundsPlayed;
    }
    public void setPoolRoundsPlayed(int poolRoundsPlayed)
    {
        _poolRoundsPlayed = poolRoundsPlayed;
    }
    public int getQuestionsAnswered()
    {
        return _questionsAnswered;
    }
    public void setQuestionsAnswered(int questionsAnswered)
    {
        _questionsAnswered = questionsAnswered;
    }
    public int getQuestionsCorrect()
    {
        return _questionsCorrect;
    }
    public void setQuestionsCorrect(int questionsCorrect)
    {
        _questionsCorrect = questionsCorrect;
    }
    public int getCumulativeQuestionScore()
    {
        return _cumulativeQuestionScore;
    }
    public void setCumulativeQuestionScore(int cumulativeQuestionScore)
    {
        _cumulativeQuestionScore = cumulativeQuestionScore;
    }

    public int getAffiliatePlanId()
    {
        return _affiliatePlanId;
    }
    public void setAffiliatePlanId(int affiliatePlanId)
    {
        _affiliatePlanId = affiliatePlanId;
    }
    @Override
    public String toString()
    {
        return MessageFormat.format(
            "sId: {0,number,#}, gamesPlayed: {1}",
            _subscriberId, _gamesPlayed);
    }
}
