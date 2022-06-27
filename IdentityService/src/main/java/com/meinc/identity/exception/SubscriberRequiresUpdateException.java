package com.meinc.identity.exception;

import java.io.Serializable;

public class SubscriberRequiresUpdateException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public SubscriberRequiresUpdateException() {
    }

    public SubscriberRequiresUpdateException(String arg0) {
        super(arg0);
    }

    public SubscriberRequiresUpdateException(Throwable arg0) {
        super(arg0);
    }

    public SubscriberRequiresUpdateException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
