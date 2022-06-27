package com.meinc.push.exception;

public class PushClientException 
extends Exception
{
    private static final long serialVersionUID = 1L;

    private String _type;
    
    public PushClientException()
    {
        super();
    }
    
    public PushClientException(String msg)
    {
        super(msg);
    }
    
    public PushClientException(String type, String msg)
    {
        super(msg);
        _type = type;
    }

    public String getType()
    {
        return _type;
    }
}
