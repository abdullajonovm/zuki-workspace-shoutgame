package com.meinc.identity.exception;

import java.io.Serializable;

public class FacebookSubscriberNotFoundException
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public FacebookSubscriberNotFoundException() {
    }

    public FacebookSubscriberNotFoundException(String arg0) {
        super(arg0);
    }

    public FacebookSubscriberNotFoundException(Throwable arg0) {
        super(arg0);
    }

    public FacebookSubscriberNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
