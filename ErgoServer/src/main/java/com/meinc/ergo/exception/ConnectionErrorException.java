package com.meinc.ergo.exception;

public class ConnectionErrorException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ConnectionErrorException()
    {
        super();
    }
    
    public ConnectionErrorException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ConnectionErrorException(Throwable arg0) {
        super(arg0);
    }

    public ConnectionErrorException(String msg)
    {
        super(msg);
    }
}
