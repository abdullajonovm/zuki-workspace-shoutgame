package com.meinc.ergo.exception;

public class SubscriberNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public SubscriberNotFoundException()
    {
        super();
    }
    
    public SubscriberNotFoundException(String msg)
    {
        super(msg);
    }

}
