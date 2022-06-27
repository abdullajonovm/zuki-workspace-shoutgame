package com.meinc.ergo.exception;

public class CannotRemoveDefaultProviderException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public CannotRemoveDefaultProviderException()
    {
        super();
    }
    
    public CannotRemoveDefaultProviderException(String msg)
    {
        super(msg);
    }

}
