package com.meinc.gameplay.domain;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class SubscriberCC1on1Result 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static enum STATUS {DID_NOT_PLAY, ODD_MAN_OUT, PLAYED_EVENT};
    public static enum RESULT {WON_1ON1, LOST_1ON1, TIED_1ON1};
    public static enum RESULT_ACTION {OFFERED_PRIZE, REDEEMED_PRIZE, ADVANCED_LEVEL};

    private int _eventId;
    private String _eventName;
    private STATUS _status;
    private Integer _currentLevelId;
    private Integer _currentLevelNum;
    private Integer _nextLevelId;
    
    private Integer _nextLevelNum;
    private RESULT _result;
    private RESULT_ACTION _resultAction;
    private boolean _usedSafetyNet;
    private Date _resultDate;
    private List<DiagnosticsQuestion> _questions;
    
    public int getEventId()
    {
        return _eventId;
    }
    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }
    public String getEventName()
    {
        return _eventName;
    }
    public void setEventName(String eventName)
    {
        _eventName = eventName;
    }
    public STATUS getStatus()
    {
        return _status;
    }
    public void setStatus(STATUS status)
    {
        _status = status;
    }
    
    @JsonIgnore
    public Integer getCurrentLevelId()
    {
        return _currentLevelId;
    }
    public void setCurrentLevelId(Integer currentLevelId)
    {
        _currentLevelId = currentLevelId;
    }
    public Integer getCurrentLevelNum()
    {
        return _currentLevelNum;
    }
    public void setCurrentLevelNum(Integer currentLevelNum)
    {
        _currentLevelNum = currentLevelNum;
    }
    
    @JsonIgnore
    public Integer getNextLevelId()
    {
        return _nextLevelId;
    }
    public void setNextLevelId(Integer nextLevelId)
    {
        _nextLevelId = nextLevelId;
    }
    public Integer getNextLevelNum()
    {
        return _nextLevelNum;
    }
    public void setNextLevelNum(Integer nextLevelNum)
    {
        _nextLevelNum = nextLevelNum;
    }
    public RESULT getResult()
    {
        return _result;
    }
    public void setResult(RESULT result)
    {
        _result = result;
    }
    public RESULT_ACTION getResultAction()
    {
        return _resultAction;
    }
    public void setResultAction(RESULT_ACTION resultAction)
    {
        _resultAction = resultAction;
    }
    public boolean isUsedSafetyNet()
    {
        return _usedSafetyNet;
    }
    public void setUsedSafetyNet(boolean usedSafetyNet)
    {
        _usedSafetyNet = usedSafetyNet;
    }
    
    @JsonSerialize(using=DateJacksonJsonSerializer.class)
    public Date getResultDate()
    {
        return _resultDate;
    }
    public void setResultDate(Date resultDate)
    {
        _resultDate = resultDate;
    }
    
    public List<DiagnosticsQuestion> getQuestions()
    {
        return _questions;
    }
    public void setQuestions(List<DiagnosticsQuestion> questions)
    {
        _questions = questions;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        SimpleDateFormat SDF = new SimpleDateFormat("EE dd MMM");

        buf.append("eId: ").append(_eventId);
        buf.append(" (").append(_eventName).append(")");
        buf.append(", status: ").append(_status);
        buf.append(", curLvl: ").append(_currentLevelNum);
        if (_status == STATUS.PLAYED_EVENT) {
            buf.append(", result: ").append(_result);
            if (_result != RESULT.LOST_1ON1) {
                buf.append(", action: ").append(_resultAction);
            }
            buf.append(", usedSafetyNet: ").append(_usedSafetyNet);
        }
        buf.append(", nextLvl: ").append(_nextLevelNum);
        buf.append(", date: ").append(SDF.format(_resultDate));
        
        if (getQuestions() != null) {
            buf.append("\n\tQUESTIONS:");
            for (DiagnosticsQuestion question : getQuestions()) {
                buf.append("\n\t").append(question);
            }
        }

        return buf.toString();
    }
    
    static class DateJacksonJsonSerializer extends JsonSerializer<Date>
    {
        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
        throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(DiagnosticsData.dateToIso8601(date));
        }
    }
}
