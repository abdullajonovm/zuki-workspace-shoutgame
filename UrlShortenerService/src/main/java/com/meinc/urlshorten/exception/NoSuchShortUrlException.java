package com.meinc.urlshorten.exception;

public class NoSuchShortUrlException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoSuchShortUrlException() {
        super();
    }

    public NoSuchShortUrlException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public NoSuchShortUrlException(String arg0) {
        super(arg0);
    }

    public NoSuchShortUrlException(Throwable arg0) {
        super(arg0);
    }
}
