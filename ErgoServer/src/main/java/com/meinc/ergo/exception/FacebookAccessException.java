package com.meinc.ergo.exception;

public class FacebookAccessException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public FacebookAccessException()
    {
        super();
    }
    
    public FacebookAccessException(String msg)
    {
        super(msg);
    }

}
