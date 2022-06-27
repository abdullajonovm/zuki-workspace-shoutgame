package com.meinc.ergo.exception;

public class PasswordTooWeakException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PasswordTooWeakException()
    {
        super();
    }
    
    public PasswordTooWeakException(String msg)
    {
        super(msg);
    }

}
