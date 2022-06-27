package com.meinc.identity.exception;

import java.io.Serializable;

public class InvalidEmailException
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public InvalidEmailException() {
    }

    public InvalidEmailException(String arg0) {
        super(arg0);
    }

    public InvalidEmailException(Throwable arg0) {
        super(arg0);
    }

    public InvalidEmailException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
