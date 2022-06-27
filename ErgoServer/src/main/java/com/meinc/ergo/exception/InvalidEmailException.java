package com.meinc.ergo.exception;

public class InvalidEmailException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public InvalidEmailException()
    {
        super();
    }
    
    public InvalidEmailException(String msg)
    {
        super(msg);
    }

}
