package com.meinc.ergo.exception;

public class InvalidParamException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public InvalidParamException()
    {
        super();
    }
    
    public InvalidParamException(String msg)
    {
        super(msg);
    }

    public InvalidParamException(String msg, Throwable err) {
        super(msg, err);
    }

    public InvalidParamException(Throwable err) {
        super(err);
    }

}
