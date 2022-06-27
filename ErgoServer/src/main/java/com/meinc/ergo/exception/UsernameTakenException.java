package com.meinc.ergo.exception;

public class UsernameTakenException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public UsernameTakenException()
    {
        super();
    }
    
    public UsernameTakenException(String msg)
    {
        super(msg);
    }

}
