package com.meinc.gameplay.exception;

import java.io.Serializable;


public class InvalidThrowdownException extends Exception implements
        Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 5916409425922018638L;

    public InvalidThrowdownException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public InvalidThrowdownException(String message, Throwable causedBy) {
        super(message, causedBy);
        // TODO Auto-generated constructor stub
    }

    public InvalidThrowdownException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public InvalidThrowdownException(Throwable causedBy) {
        super(causedBy);
        // TODO Auto-generated constructor stub
    }

}
