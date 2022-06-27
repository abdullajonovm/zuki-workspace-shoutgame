package com.meinc.ergo.exception;

public class MissingRequiredParamException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public MissingRequiredParamException()
    {
        super();
    }
    
    public MissingRequiredParamException(String msg)
    {
        super(msg);
    }

}
