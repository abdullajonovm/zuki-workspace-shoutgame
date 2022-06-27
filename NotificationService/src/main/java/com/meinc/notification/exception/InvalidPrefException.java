package com.meinc.notification.exception;

import java.io.Serializable;

public class InvalidPrefException extends Exception implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6468516161077274363L;

    public InvalidPrefException() {
        // TODO Auto-generated constructor stub
    }

    public InvalidPrefException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidPrefException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidPrefException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

}
