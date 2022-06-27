package com.meinc.identity.exception;

import java.io.Serializable;

public class InvalidSessionException
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public InvalidSessionException() {
    }

    public InvalidSessionException(String arg0) {
        super(arg0);
    }

    public InvalidSessionException(Throwable arg0) {
        super(arg0);
    }

    public InvalidSessionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
