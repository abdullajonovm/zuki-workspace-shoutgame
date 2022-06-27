package com.meinc.urlshorten.exception;

public class UrlShortenerException extends Exception {
    private static final long serialVersionUID = 1L;

    public UrlShortenerException() {
        super();
    }

    public UrlShortenerException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UrlShortenerException(String arg0) {
        super(arg0);
    }

    public UrlShortenerException(Throwable arg0) {
        super(arg0);
    }
}
