package com.meinc.identity.exception;

import java.io.Serializable;

public class InvalidSubscriberUpdateException extends Exception implements Serializable {

    private static final long serialVersionUID = 6197201065315774268L;

    public InvalidSubscriberUpdateException() {
        // TODO Auto-generated constructor stub
    }

    public InvalidSubscriberUpdateException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidSubscriberUpdateException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidSubscriberUpdateException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

}
