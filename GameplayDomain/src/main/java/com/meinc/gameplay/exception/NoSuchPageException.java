package com.meinc.gameplay.exception;

public class NoSuchPageException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public NoSuchPageException()
    {
        super();
    }
    
    public NoSuchPageException(String message)
    {
        super(message);
    }
}
