package com.meinc.gameplay.domain;

/**
 * This represents a relationship between an event and a vip box.
 * These will be automatically created by the gameplay service
 * for vip boxes marked "autoCreate" otherwise they will have 
 * to be explicitly created.
 *  
 * @author bxgrant
 */
public class EventVipBox extends BaseDomainObject
{
    private static final long serialVersionUID = -76094169056321183L;

    /** The unique ID for this pairing of eventId and vipBoxId */
    private int _eventVipBoxId;
    
    /** The event in question. */
    private int _eventId;
    
    /** The vip box in question */
    private int _vipBoxId;
    
    /** The unique ID that we can use in a hash tag to represent a shout out in twitter. */
    private String _shoutOutId;
    
    /**
     * If location checkin is required and open membership is true, text messagers will "checkin"
     * to the vip box for a given event by texting this code in.  The result will be that
     * the player is added to the vip box, if not already added, and authorized to play for 
     * this event via this text messaging command:
     * 
     * checkin a34r2b
     */
    private String _textCheckinCode;
    
    public EventVipBox()
    {
    }

    public int getEventVipBoxId()
    {
        return _eventVipBoxId;
    }

    public void setEventVipBoxId(int eventVipBoxId)
    {
        _eventVipBoxId = eventVipBoxId;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
    }

    public int getVipBoxId()
    {
        return _vipBoxId;
    }

    public void setVipBoxId(int vipBoxId)
    {
        _vipBoxId = vipBoxId;
    }

    public String getShoutOutId()
    {
        return _shoutOutId;
    }

    public void setShoutOutId(String shoutOutId)
    {
        _shoutOutId = shoutOutId;
    }

    public String getTextCheckinCode()
    {
        return _textCheckinCode;
    }

    public void setTextCheckinCode(String textCheckinCode)
    {
        _textCheckinCode = textCheckinCode;
    }
}
