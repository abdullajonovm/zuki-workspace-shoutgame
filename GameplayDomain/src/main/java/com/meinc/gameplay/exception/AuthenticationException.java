package com.meinc.gameplay.exception;

import java.io.Serializable;

public class AuthenticationException 
extends Exception implements Serializable
{
    private static final long serialVersionUID = 1L;

    public AuthenticationException()
    {
        super();
    }
    
    public AuthenticationException(String message)
    {
        super(message);
    }

    public AuthenticationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public AuthenticationException(Throwable arg0) {
        super(arg0);
    }

}
