package com.meinc.ergo.exception;

public class CodeExpiredException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public CodeExpiredException()
    {
        super();
    }
    
    public CodeExpiredException(String msg)
    {
        super(msg);
    }

}
