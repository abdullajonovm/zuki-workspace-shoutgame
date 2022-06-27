package com.meinc.ergo.exception;

public class PromoBatchExpiredException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoBatchExpiredException()
    {
        super();
    }
    
    public PromoBatchExpiredException(String msg)
    {
        super(msg);
    }

}
