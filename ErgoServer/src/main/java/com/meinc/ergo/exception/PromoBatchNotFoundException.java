package com.meinc.ergo.exception;

public class PromoBatchNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoBatchNotFoundException()
    {
        super();
    }
    
    public PromoBatchNotFoundException(String msg)
    {
        super(msg);
    }

}
