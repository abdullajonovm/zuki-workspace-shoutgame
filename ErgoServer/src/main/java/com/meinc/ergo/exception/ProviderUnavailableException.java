package com.meinc.ergo.exception;

public class ProviderUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProviderUnavailableException() {
        super();
    }

    public ProviderUnavailableException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ProviderUnavailableException(String arg0) {
        super(arg0);
    }

    public ProviderUnavailableException(Throwable arg0) {
        super(arg0);
    }

}
