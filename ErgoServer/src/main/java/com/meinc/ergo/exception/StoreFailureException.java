package com.meinc.ergo.exception;

public class StoreFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    public StoreFailureException() {
        super();
    }

    public StoreFailureException(String msg, Throwable err) {
        super(msg, err);
    }

    public StoreFailureException(String msg) {
        super(msg);
    }

    public StoreFailureException(Throwable err) {
        super(err);
    }
}
