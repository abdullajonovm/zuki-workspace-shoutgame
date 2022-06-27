package com.meinc.store.exception;

public class AlreadyReservedException extends Exception
{
    private static final long serialVersionUID = 4857791021458550069L;

    public AlreadyReservedException()
    {
    }

    public AlreadyReservedException(String message)
    {
        super(message);
    }

    public AlreadyReservedException(Throwable cause)
    {
        super(cause);
    }

    public AlreadyReservedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
