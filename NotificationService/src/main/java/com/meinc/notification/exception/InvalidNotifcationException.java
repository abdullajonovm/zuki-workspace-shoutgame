package com.meinc.notification.exception;

import java.io.Serializable;

public class InvalidNotifcationException extends Exception implements
        Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6232768049651268424L;

    public InvalidNotifcationException() {
        super("invalid notification");
        // TODO Auto-generated constructor stub
    }

    public InvalidNotifcationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public InvalidNotifcationException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public InvalidNotifcationException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
