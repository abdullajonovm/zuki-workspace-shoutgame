package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class EventMediaRef implements Serializable
{
    private static final long serialVersionUID = 6215313008765336391L;
    public static final int classId = 1003;

    private int _eventId;
    private String _photoRefUuid;
    private String _photoUrl;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public EventMediaRef()
    {
    }
    
    public EventMediaRef(int eventId, String photoRefUuid)
    {
        _eventId = eventId;
        _photoRefUuid = photoRefUuid;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public String getPhotoRefUuid()
    {
        return _photoRefUuid;
    }

    public void setPhotoRefUuid(String photoRefUuid)
    {
        _photoRefUuid = photoRefUuid;
    }
    
    public String getPhotoUrl()
    {
        return _photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        _photoUrl = photoUrl;
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

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        sb.append("eventId: ");
        sb.append(_eventId);
        sb.append(", photoRefUuid: ");
        sb.append(_photoRefUuid == null ? "null" : _photoRefUuid);
        sb.append("}");
        
        return sb.toString();
    }
}
