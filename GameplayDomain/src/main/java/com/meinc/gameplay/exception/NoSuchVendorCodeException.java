package com.meinc.gameplay.exception;

public class NoSuchVendorCodeException extends RuntimeException
{
    private static final long serialVersionUID = -9203929785335823662L;

    public NoSuchVendorCodeException()
    {
        super();
    }
    
    public NoSuchVendorCodeException(String message)
    {
        super(message);
    }
}
