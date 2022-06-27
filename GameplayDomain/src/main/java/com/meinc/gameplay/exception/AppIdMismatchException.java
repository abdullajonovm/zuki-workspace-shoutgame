package com.meinc.gameplay.exception;

import java.io.Serializable;

public class AppIdMismatchException 
extends Exception implements Serializable
{
    private static final long serialVersionUID = 1L;

    public AppIdMismatchException()
    {
        super();
    }
    
    public AppIdMismatchException(String message)
    {
        super(message);
    }
}
