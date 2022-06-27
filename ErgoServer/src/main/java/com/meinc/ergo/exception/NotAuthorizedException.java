package com.meinc.ergo.exception;

public class NotAuthorizedException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public NotAuthorizedException()
    {
        super();
    }
    
    public NotAuthorizedException(String msg)
    {
        super(msg);
    }

}
