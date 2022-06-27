package com.meinc.store.exception;

public class InvalidItemException extends Exception
{
    private static final long serialVersionUID = 4857791021458550069L;

    public InvalidItemException()
    {
    }

    public InvalidItemException(String message)
    {
        super(message);
    }

    public InvalidItemException(Throwable cause)
    {
        super(cause);
    }

    public InvalidItemException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
