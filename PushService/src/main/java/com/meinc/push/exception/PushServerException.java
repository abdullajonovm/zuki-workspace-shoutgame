package com.meinc.push.exception;

public class PushServerException 
extends Exception
{
    private static final long serialVersionUID = 1L;

    private String _type;
    
    public PushServerException()
    {
        super();
    }
    
    public PushServerException(String msg)
    {
        super(msg);
    }
    
    public PushServerException(String type, String msg)
    {
        super(msg);
        _type = type;
    }

    public String getType()
    {
        return _type;
    }
}
