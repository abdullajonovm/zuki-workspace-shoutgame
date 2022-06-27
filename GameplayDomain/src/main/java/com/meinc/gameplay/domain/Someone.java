package com.meinc.gameplay.domain;

/**
 * A person who is not currently part of the Shout system but is known to someone who is part of the system.
 */
public class Someone 
extends BaseDomainObject
{
    private static final long serialVersionUID = -6890848758193400061L;
    public static enum EXTERNAL_SOMEONE_TYPE 
    {
        FACEBOOK_ID, SUBSCRIBER_ID, NICKNAME, CELL_NUMBER, EMAIL
    }
    
    private int _externalSomeoneId;
    private EXTERNAL_SOMEONE_TYPE _type;
    private String _value;

    public Someone()
    {
        super();
    }

    public int getExternalSomeoneId()
    {
        return _externalSomeoneId;
    }

    public void setExternalSomeoneId(int externalSomeoneId)
    {
        _externalSomeoneId = externalSomeoneId;
    }

    public EXTERNAL_SOMEONE_TYPE getType()
    {
        return _type;
    }

    public void setType(EXTERNAL_SOMEONE_TYPE type)
    {
        _type = type;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(_externalSomeoneId).append(", ");
        buf.append("type: ").append(_type).append(", ");
        buf.append("value: ").append(_value);
        
        return buf.toString();
    }
}
