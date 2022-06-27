package com.meinc.gameplay.exception;

public class InvalidRoleException extends RuntimeException
{
    private static final long serialVersionUID = -9203929785335823662L;

    public InvalidRoleException()
    {
        super("The subscriber role is invalid.");
    }
    
    public InvalidRoleException(String message)
    {
        super(message);
    }
}
