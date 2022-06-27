package com.meinc.ergo.exception;

public class PromoBatchInvalidUpdateException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoBatchInvalidUpdateException()
    {
        super();
    }
    
    public PromoBatchInvalidUpdateException(String msg)
    {
        super(msg);
    }

}
