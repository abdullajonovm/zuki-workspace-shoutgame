package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class Throwdown implements Serializable
{
    private static final long serialVersionUID = 535521782139692505L;

    public static enum THROWDOWN_STATUS
    {
        NEW, // Newly created throwdown
        CANCELLED, // Creator cancelled throwdown or was cancelled after a period of time
        REJECTED, // toSubscriber rejected invite
        ACCEPTED, // toSubscriber accepted invite
        OPEN, // The throwdown is in progress and thus the questionId has been
              // set to the question of the throwdown
        CLOSED // The throwdown question is closed and scored
    };

    private Integer _throwdownId;

    /** Subscriber who created the throwdown. */
    private Integer _fromSubscriberId;

    /* If set, the subscriber who was invited to throwdown. */
    private Integer _toSubscriberId;

    /** The event the throwdown should be in. */
    private Integer _eventId;
    
    /** The vip box the throwndown is in */
    private Integer _vipBoxId;

    /** The throwdown question; won't be set until status is OPEN. */
    private Integer _questionId;

    /** The status of the throwdown. @see THROWDOWN_STATUS */
    private THROWDOWN_STATUS _status = THROWDOWN_STATUS.NEW;

    /** The number of points to use. */
    private Integer _points;

    /** The message to send to the toSubscriber */
    private String _message;

    /** The timestamp when the push responses were sent to both subscribers. */
    private Date _responseDate;

    /**
     * The timestampe when then calculation was performed and applied to
     * subscribers' scores.
     */
    private Date _resultDate;

    /** When the throwdown was created. */
    private Date _createdDate;

    /** Auto-updated by SQL trigger. */
    private Date _lastUpdated;

    public Throwdown()
    {
    }
    
    public Throwdown(Integer fromSubscriberId, Integer toSubscriberId, Integer eventId, Integer vipBoxId, Integer points, String message)
    {
        _fromSubscriberId = fromSubscriberId;
        _toSubscriberId = toSubscriberId;
        _eventId = eventId;
        _vipBoxId = vipBoxId;
        _points = points;
        _message = message;
        
    }

    public Integer getThrowdownId()
    {
        return _throwdownId;
    }

    public void setThrowdownId(Integer throwdownId)
    {
        _throwdownId = throwdownId;
    }

    public Integer getFromSubscriberId()
    {
        return _fromSubscriberId;
    }

    public void setFromSubscriberId(Integer fromSubscriberId)
    {
        _fromSubscriberId = fromSubscriberId;
    }

    public Integer getToSubscriberId()
    {
        return _toSubscriberId;
    }

    public void setToSubscriberId(Integer toSubscriberId)
    {
        _toSubscriberId = toSubscriberId;
    }

    public Integer getEventId()
    {
        return _eventId;
    }

    public void setEventId(Integer eventId)
    {
        _eventId = eventId;
    }

    public Integer getQuestionId()
    {
        return _questionId;
    }

    public void setQuestionId(Integer questionId)
    {
        _questionId = questionId;
    }

    public THROWDOWN_STATUS getStatus()
    {
        return _status;
    }

    public void setStatus(THROWDOWN_STATUS status)
    {
        _status = status;
    }

    public Integer getPoints()
    {
        return _points;
    }

    public void setPoints(Integer points)
    {
        _points = points;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public Date getResponseDate()
    {
        return _responseDate;
    }

    public void setResponseDate(Date responseDate)
    {
        _responseDate = responseDate;
    }

    public Date getResultDate()
    {
        return _resultDate;
    }

    public void setResultDate(Date resultDate)
    {
        _resultDate = resultDate;
    }

    public Date getCreatedDate()
    {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }

    public Integer getVipBoxId() {
        return _vipBoxId;
    }

    public void setVipBoxId(Integer vipBoxId) {
        _vipBoxId = vipBoxId;
    }

}
