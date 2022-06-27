package com.meinc.gameplay.exception;

public class InvalidPhoneNumberException extends RuntimeException
{
    private static final long serialVersionUID = -9203929785335823662L;

    public InvalidPhoneNumberException()
    {
        super("The subscriber phone # is invalid.");
    }
    
    public InvalidPhoneNumberException(String message)
    {
        super(message);
    }
}
