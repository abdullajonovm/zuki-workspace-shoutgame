package com.meinc.push.exception;

public class PayloadInvalidException 
extends Exception 
{
    private static final long serialVersionUID = -9093610990642726667L;

    public PayloadInvalidException() {
        super("The payload is invalid");
    }

    public PayloadInvalidException(String arg0) {
        super(arg0);
    }

    public PayloadInvalidException(Throwable arg0) {
        super(arg0);
    }

    public PayloadInvalidException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
