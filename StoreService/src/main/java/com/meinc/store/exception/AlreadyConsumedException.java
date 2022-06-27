package com.meinc.store.exception;

public class AlreadyConsumedException extends Exception
{
    private static final long serialVersionUID = 4857791021458550069L;

    public AlreadyConsumedException()
    {
    }

    public AlreadyConsumedException(String message)
    {
        super(message);
    }

    public AlreadyConsumedException(Throwable cause)
    {
        super(cause);
    }

    public AlreadyConsumedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
