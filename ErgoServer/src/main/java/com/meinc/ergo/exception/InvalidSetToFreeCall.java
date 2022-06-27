package com.meinc.ergo.exception;

public class InvalidSetToFreeCall 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public InvalidSetToFreeCall()
    {
        super();
    }
    
    public InvalidSetToFreeCall(String msg)
    {
        super(msg);
    }

}
