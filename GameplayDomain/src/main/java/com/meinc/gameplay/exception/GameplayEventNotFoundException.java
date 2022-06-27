package com.meinc.gameplay.exception;

import java.io.Serializable;

public class GameplayEventNotFoundException extends Exception implements Serializable
{
    private static final long serialVersionUID = 2351480305714643154L;

    public GameplayEventNotFoundException()
    {
        super("Could not find the gameplay event specified.");
    }
    
    public GameplayEventNotFoundException(String msg)
    {
        super(msg);
    }
}
