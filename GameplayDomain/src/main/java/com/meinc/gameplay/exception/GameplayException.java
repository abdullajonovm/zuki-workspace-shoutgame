package com.meinc.gameplay.exception;

public class GameplayException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public GameplayException()
    {
        super();
    }
    
    public GameplayException(String msg)
    {
        super(msg);
    }
    
    public GameplayException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
