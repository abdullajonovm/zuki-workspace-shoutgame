package com.meinc.webcollector.service;

public class ForbiddenAccessException extends Exception {
    private static final long serialVersionUID = 1L;

    public ForbiddenAccessException() {
        super();
    }

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
