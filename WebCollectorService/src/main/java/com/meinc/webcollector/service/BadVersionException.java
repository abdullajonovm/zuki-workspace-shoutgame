package com.meinc.webcollector.service;

public class BadVersionException extends Exception {
    private static final long serialVersionUID = 1L;

    public BadVersionException() {
        super();
    }

    public BadVersionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BadVersionException(String arg0) {
        super(arg0);
    }

    public BadVersionException(Throwable arg0) {
        super(arg0);
    }
}
