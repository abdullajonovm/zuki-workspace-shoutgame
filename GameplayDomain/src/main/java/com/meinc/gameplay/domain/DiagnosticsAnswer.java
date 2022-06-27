package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.meinc.gameplay.domain.SubscriberCC1on1Result.DateJacksonJsonSerializer;

public class DiagnosticsAnswer 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static enum POWERUP_TYPE {CLOCK_FREEZE, FAN_CHECK, VOTE2, MULLIGAN, THROWDOWN, SAFETY_NET};
    public static enum POWERUP_USAGE_STATUS {RESERVED, CONSUMED};

    private boolean _correct;
    private int _powerupUsageId;
    private long _relativeAnswerTimeMs;
    private Date _answerDate;
    private POWERUP_TYPE _powerupType;
    private POWERUP_USAGE_STATUS _powerupUsageStatus;
    private int _numClockFreezesUsed;
    private int _numThrowdownsFromMe;
    private int _numThrowdownsFromOpponent;
    
    public boolean isCorrect()
    {
        return _correct;
    }
    public void setCorrect(boolean correct)
    {
        _correct = correct;
    }
    
    @JsonIgnore
    public int getPowerupUsageId()
    {
        return _powerupUsageId;
    }
    public void setPowerupUsageId(int powerupUsageId)
    {
        _powerupUsageId = powerupUsageId;
    }
    public long getRelativeAnswerTimeMs()
    {
        return _relativeAnswerTimeMs;
    }
    public void setRelativeAnswerTimeMs(long relativeAnswerTimeMs)
    {
        _relativeAnswerTimeMs = relativeAnswerTimeMs;
    }
    
    @JsonSerialize(using=DateJacksonJsonSerializer.class)
    public Date getAnswerDate()
    {
        return _answerDate;
    }
    public void setAnswerDate(Date answerDate)
    {
        _answerDate = answerDate;
    }
    public POWERUP_TYPE getPowerupType()
    {
        return _powerupType;
    }
    public void setPowerupType(POWERUP_TYPE powerupType)
    {
        _powerupType = powerupType;
    }
    
    @JsonIgnore
    public POWERUP_USAGE_STATUS getPowerupUsageStatus()
    {
        return _powerupUsageStatus;
    }
    public void setPowerupUsageStatus(POWERUP_USAGE_STATUS powerupUsageStatus)
    {
        _powerupUsageStatus = powerupUsageStatus;
    }
    
    public int getNumClockFreezesUsed()
    {
        return _numClockFreezesUsed;
    }
    public void setNumClockFreezesUsed(int numClockFreezesUsed)
    {
        _numClockFreezesUsed = numClockFreezesUsed;
    }
    
    public int getNumThrowdownsFromMe()
    {
        return _numThrowdownsFromMe;
    }
    public void setNumThrowdownsFromMe(int numThrowdownsFromMe)
    {
        _numThrowdownsFromMe = numThrowdownsFromMe;
    }
    public int getNumThrowdownsFromOpponent()
    {
        return _numThrowdownsFromOpponent;
    }
    public void setNumThrowdownsFromOpponent(int numThrowdownsFromOpponent)
    {
        _numThrowdownsFromOpponent = numThrowdownsFromOpponent;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

//        private Date _answerDate;
        buf.append("correct: ").append(_correct);
        buf.append(", answerTime: ").append(_relativeAnswerTimeMs);
        if (_powerupUsageId != 0) {
            buf.append(", powerup: ").append(_powerupType);
            buf.append(", status: ").append(_powerupUsageStatus);
        }
        if (_numClockFreezesUsed > 0)
            buf.append(", #clockFreezes: ").append(_numClockFreezesUsed);
        if (_numThrowdownsFromMe > 0)
            buf.append(", #throwdowns initiated by me: ").append(_numThrowdownsFromMe);
        if (_numThrowdownsFromOpponent > 0)
            buf.append(", #throwdowns initiated by opponent: ").append(_numThrowdownsFromOpponent);

        return buf.toString();
    }
}
