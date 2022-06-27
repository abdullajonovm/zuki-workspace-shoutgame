package com.meinc.notification.exception;

import java.io.Serializable;

public class NotificationDeletedException extends Exception implements
        Serializable
{

    private static final long serialVersionUID = -3323702546120983014L;

    public NotificationDeletedException() {
        super();
    }

    public NotificationDeletedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationDeletedException(String message) {
        super(message);
    }

    public NotificationDeletedException(Throwable cause) {
        super(cause);
    }

}
