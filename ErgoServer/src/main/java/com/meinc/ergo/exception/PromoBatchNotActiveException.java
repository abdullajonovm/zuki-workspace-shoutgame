package com.meinc.ergo.exception;

public class PromoBatchNotActiveException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoBatchNotActiveException()
    {
        super();
    }
    
    public PromoBatchNotActiveException(String msg)
    {
        super(msg);
    }

}
