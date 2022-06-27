package com.meinc.identity.exception;

import java.io.Serializable;

public class EmailNotVerifiedException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public EmailNotVerifiedException() {
    }

    public EmailNotVerifiedException(String arg0) {
        super(arg0);
    }

    public EmailNotVerifiedException(Throwable arg0) {
        super(arg0);
    }

    public EmailNotVerifiedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
