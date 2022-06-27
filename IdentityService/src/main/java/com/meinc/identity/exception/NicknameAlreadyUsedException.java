package com.meinc.identity.exception;

public class NicknameAlreadyUsedException 
extends Exception 
{
    private static final long serialVersionUID = 4446043610903728851L;

    public NicknameAlreadyUsedException() {
        super("the given nickname is already in use, please use another");
    }

    public NicknameAlreadyUsedException(String arg0) {
        super(arg0);
    }

    public NicknameAlreadyUsedException(Throwable arg0) {
        super(arg0);
    }

    public NicknameAlreadyUsedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
