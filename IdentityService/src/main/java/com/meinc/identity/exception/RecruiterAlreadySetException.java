package com.meinc.identity.exception;

public class RecruiterAlreadySetException 
extends Exception 
{
    private static final long serialVersionUID = 4446043610903728851L;

    public RecruiterAlreadySetException() {
        super("the given nickname is already in use, please use another");
    }

    public RecruiterAlreadySetException(String arg0) {
        super(arg0);
    }

    public RecruiterAlreadySetException(Throwable arg0) {
        super(arg0);
    }

    public RecruiterAlreadySetException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
