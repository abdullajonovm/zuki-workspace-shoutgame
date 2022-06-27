package com.meinc.gameplay.domain;

import java.io.Serializable;

public class EventMedia implements Serializable
{
    private static final long serialVersionUID = 6215313008765336391L;

    private int _eventId;
    private String _photoUrl;
    
    public EventMedia()
    {
    }
    
    public EventMedia(int eventId, String photoUrl)
    {
        _eventId = eventId;
        _photoUrl = photoUrl;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public String getPhotoUrl()
    {
        return _photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        _photoUrl = photoUrl;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        sb.append("eventId: ");
        sb.append(_eventId);
        sb.append(", photoUrl: ");
        sb.append(_photoUrl == null ? "null" : _photoUrl);
        sb.append("}");
        
        return sb.toString();
    }
}
