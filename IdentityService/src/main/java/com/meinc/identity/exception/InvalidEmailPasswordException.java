package com.meinc.identity.exception;

import java.io.Serializable;

public class InvalidEmailPasswordException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public InvalidEmailPasswordException() {
    }

    public InvalidEmailPasswordException(String arg0) {
        super(arg0);
    }

    public InvalidEmailPasswordException(Throwable arg0) {
        super(arg0);
    }

    public InvalidEmailPasswordException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
