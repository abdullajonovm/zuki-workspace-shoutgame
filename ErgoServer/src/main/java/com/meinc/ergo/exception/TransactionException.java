package com.meinc.ergo.exception;

public class TransactionException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public TransactionException(Throwable cause)
    {
        super(cause);
    }

    public TransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
