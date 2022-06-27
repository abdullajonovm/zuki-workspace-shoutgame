package com.meinc.notification.exception;

import java.io.Serializable;

public class InvalidMessageException extends Exception implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6921084013882006638L;

    public InvalidMessageException() {
        super("Notification message is invalid (cannot be null, and must be <= 140 chars)");
        // TODO Auto-generated constructor stub
    }

    public InvalidMessageException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public InvalidMessageException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidMessageException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
