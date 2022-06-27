package com.meinc.ergo.exception;

public class TaskNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public TaskNotFoundException()
    {
        super();
    }
    
    public TaskNotFoundException(String msg)
    {
        super(msg);
    }

}
