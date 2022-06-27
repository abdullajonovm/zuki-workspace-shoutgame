package com.meinc.gameplay.exception;

public class NameAlreadyUsedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 4446043610903728851L;

    public NameAlreadyUsedException() {
        super("the given name is already in use, please use another");
    }

    public NameAlreadyUsedException(String arg0) {
        super(arg0);
    }

    public NameAlreadyUsedException(Throwable arg0) {
        super(arg0);
    }

    public NameAlreadyUsedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
