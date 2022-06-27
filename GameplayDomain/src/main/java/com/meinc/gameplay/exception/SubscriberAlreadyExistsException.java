package com.meinc.gameplay.exception;

public class SubscriberAlreadyExistsException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public SubscriberAlreadyExistsException()
    {
        super();
    }
    
    public SubscriberAlreadyExistsException(String message)
    {
        super(message);
    }
}
