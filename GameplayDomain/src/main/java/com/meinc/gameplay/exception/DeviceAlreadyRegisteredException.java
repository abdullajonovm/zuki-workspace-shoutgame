package com.meinc.gameplay.exception;

public class DeviceAlreadyRegisteredException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public DeviceAlreadyRegisteredException()
    {
        super();
    }
    
    public DeviceAlreadyRegisteredException(String message)
    {
        super(message);
    }
}
