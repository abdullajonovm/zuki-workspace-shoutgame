package com.meinc.identity.exception;

import java.io.Serializable;

public class SubscriberInactiveException
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public SubscriberInactiveException() {
    }

    public SubscriberInactiveException(String arg0) {
        super(arg0);
    }

    public SubscriberInactiveException(Throwable arg0) {
        super(arg0);
    }

    public SubscriberInactiveException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
