package com.meinc.ergo.exception;

public class NotAuthenticatedException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public NotAuthenticatedException()
    {
        super();
    }
    
    public NotAuthenticatedException(String msg)
    {
        super(msg);
    }

}
