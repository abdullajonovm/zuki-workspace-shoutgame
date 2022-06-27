package com.meinc.gameplay.exception;

public class UserNotFoundException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public UserNotFoundException()
    {
        super();
    }
    
    public UserNotFoundException(String message)
    {
        super(message);
    }
}
