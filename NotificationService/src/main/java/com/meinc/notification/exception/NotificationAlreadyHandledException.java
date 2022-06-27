package com.meinc.notification.exception;

import java.io.Serializable;

public class NotificationAlreadyHandledException extends Exception implements
        Serializable
{

    private static final long serialVersionUID = -3126275337489420045L;

    public NotificationAlreadyHandledException() {
        super();
    }

    public NotificationAlreadyHandledException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public NotificationAlreadyHandledException(String arg0) {
        super(arg0);
    }

    public NotificationAlreadyHandledException(Throwable arg0) {
        super(arg0);
    }

}
