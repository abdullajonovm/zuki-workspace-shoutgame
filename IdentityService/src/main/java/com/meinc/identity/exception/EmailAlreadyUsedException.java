package com.meinc.identity.exception;

import java.io.Serializable;

public class EmailAlreadyUsedException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public EmailAlreadyUsedException() {
    }

    public EmailAlreadyUsedException(String arg0) {
        super(arg0);
    }

    public EmailAlreadyUsedException(Throwable arg0) {
        super(arg0);
    }

    public EmailAlreadyUsedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
