package com.meinc.ergo.exception;

public class ServerThrottledException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ServerThrottledException()
    {
        super();
    }
    
    public ServerThrottledException(String msg)
    {
        super(msg);
    }

}
