package com.meinc.gameplay.exception;

public class TeamNotFoundException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public TeamNotFoundException()
    {
        super();
    }
    
    public TeamNotFoundException(String message)
    {
        super(message);
    }
}
