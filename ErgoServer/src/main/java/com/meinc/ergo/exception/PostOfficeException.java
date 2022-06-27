package com.meinc.ergo.exception;

public class PostOfficeException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PostOfficeException()
    {
        super();
    }
    
    public PostOfficeException(String msg)
    {
        super(msg);
    }

}
