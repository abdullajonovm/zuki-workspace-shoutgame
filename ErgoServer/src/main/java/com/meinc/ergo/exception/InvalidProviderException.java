package com.meinc.ergo.exception;

public class InvalidProviderException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public InvalidProviderException()
    {
        super();
    }
    
    public InvalidProviderException(String msg)
    {
        super(msg);
    }

}
