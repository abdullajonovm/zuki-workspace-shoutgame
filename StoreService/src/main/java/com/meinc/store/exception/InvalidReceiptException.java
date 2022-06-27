package com.meinc.store.exception;

public class InvalidReceiptException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidReceiptException() {
        super();
    }

    public InvalidReceiptException(String msg, Throwable e) {
        super(msg, e);
    }

    public InvalidReceiptException(String msg) {
        super(msg);
    }

    public InvalidReceiptException(Throwable msg) {
        super(msg);
    }

}
