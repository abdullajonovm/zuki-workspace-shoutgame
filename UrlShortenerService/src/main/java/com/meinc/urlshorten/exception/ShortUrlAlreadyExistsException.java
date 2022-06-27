package com.meinc.urlshorten.exception;

public class ShortUrlAlreadyExistsException extends Exception {
    private static final long serialVersionUID = 1L;

    public ShortUrlAlreadyExistsException() {
        super();
    }

    public ShortUrlAlreadyExistsException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ShortUrlAlreadyExistsException(String arg0) {
        super(arg0);
    }

    public ShortUrlAlreadyExistsException(Throwable arg0) {
        super(arg0);
    }
}
