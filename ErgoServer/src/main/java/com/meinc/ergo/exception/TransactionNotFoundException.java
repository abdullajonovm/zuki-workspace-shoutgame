package com.meinc.ergo.exception;

public class TransactionNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;

    public TransactionNotFoundException()
    {
        super();
    }
    
    public TransactionNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public TransactionNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
