package com.meinc.ergo.exception;

public class CategoriesUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public CategoriesUnavailableException() {
        super();
    }

    public CategoriesUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoriesUnavailableException(String message) {
        super(message);
    }

    public CategoriesUnavailableException(Throwable cause) {
        super(cause);
    }
    
}
