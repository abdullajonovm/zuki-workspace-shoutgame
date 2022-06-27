package com.meinc.push.exception;

public class PushNetworkIoException 
extends Exception 
{
    private static final long serialVersionUID = -9093610990642726667L;

    public PushNetworkIoException() {
        super("The payload is invalid");
    }

    public PushNetworkIoException(String arg0) {
        super(arg0);
    }

    public PushNetworkIoException(Throwable arg0) {
        super(arg0);
    }

    public PushNetworkIoException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
