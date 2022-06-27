package com.meinc.ergo.exception;

public class AutodiscoverServiceUnavailableException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public AutodiscoverServiceUnavailableException()
    {
        super();
    }
    
    public AutodiscoverServiceUnavailableException(String msg)
    {
        super(msg);
    }

}
