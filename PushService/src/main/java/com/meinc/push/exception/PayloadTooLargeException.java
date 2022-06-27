package com.meinc.push.exception;

public class PayloadTooLargeException 
extends Exception 
{
    private static final long serialVersionUID = -5187142913118958251L;

    public PayloadTooLargeException() {
        super("The payload for the push notification is too large");
    }

    public PayloadTooLargeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PayloadTooLargeException(String arg0) {
        super(arg0);
    }

    public PayloadTooLargeException(Throwable arg0) {
        super(arg0);
    }

}
