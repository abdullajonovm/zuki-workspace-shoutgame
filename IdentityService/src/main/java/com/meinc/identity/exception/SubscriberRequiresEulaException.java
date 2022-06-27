package com.meinc.identity.exception;

import java.io.Serializable;

public class SubscriberRequiresEulaException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public SubscriberRequiresEulaException() {
    }

    public SubscriberRequiresEulaException(String arg0) {
        super(arg0);
    }

    public SubscriberRequiresEulaException(Throwable arg0) {
        super(arg0);
    }

    public SubscriberRequiresEulaException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
