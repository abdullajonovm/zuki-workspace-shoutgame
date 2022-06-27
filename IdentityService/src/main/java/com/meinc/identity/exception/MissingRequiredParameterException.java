package com.meinc.identity.exception;

import java.io.Serializable;

public class MissingRequiredParameterException
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312179L;

    public MissingRequiredParameterException() {
    }

    public MissingRequiredParameterException(String arg0) {
        super(arg0);
    }

    public MissingRequiredParameterException(Throwable arg0) {
        super(arg0);
    }

    public MissingRequiredParameterException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
