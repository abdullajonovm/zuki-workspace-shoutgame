package com.meinc.ergo.exception;

public class NoDataTryBackException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public NoDataTryBackException()
    {
        super();
    }
    
    public NoDataTryBackException(String msg)
    {
        super(msg);
    }

}
