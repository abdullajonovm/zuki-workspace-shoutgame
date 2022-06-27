package com.meinc.gameplay.exception;

public class AuthorizationException 
extends Exception
{
    private static final long serialVersionUID = 1L;

    public AuthorizationException()
    {
        super();
    }
    
    public AuthorizationException(String message)
    {
        super(message);
    }
}
