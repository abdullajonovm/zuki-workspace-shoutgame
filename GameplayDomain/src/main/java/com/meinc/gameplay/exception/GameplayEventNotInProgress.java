package com.meinc.gameplay.exception;

public class GameplayEventNotInProgress 
extends RuntimeException
{
    private static final long serialVersionUID = 5494770397339687912L;

    public GameplayEventNotInProgress()
    {
        super("This gameplay event is not yet in progress.");
    }

    public GameplayEventNotInProgress(String msg)
    {
        super(msg);
    }
}
