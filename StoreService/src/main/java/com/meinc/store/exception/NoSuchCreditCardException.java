package com.meinc.store.exception;

public class NoSuchCreditCardException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoSuchCreditCardException() {
    }

    public NoSuchCreditCardException(String arg0) {
        super(arg0);
    }

    public NoSuchCreditCardException(Throwable arg0) {
        super(arg0);
    }

    public NoSuchCreditCardException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
