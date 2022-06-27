package com.meinc.identity.exception;

public class NicknameInvalidException 
extends Exception 
{
    private static final long serialVersionUID = 4269677563400813462L;

    public NicknameInvalidException() {
        super("nickname contains invalid characters and/or phrases");
    }

    public NicknameInvalidException(String message) {
        super(message);
    }

    public NicknameInvalidException(Throwable cause) {
        super(cause);
    }

    public NicknameInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
