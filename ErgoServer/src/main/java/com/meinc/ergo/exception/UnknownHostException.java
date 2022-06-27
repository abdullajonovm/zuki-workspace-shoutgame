package com.meinc.ergo.exception;

public class UnknownHostException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public UnknownHostException()
    {
        super();
    }
    
    public UnknownHostException(String msg)
    {
        super(msg);
    }

}
