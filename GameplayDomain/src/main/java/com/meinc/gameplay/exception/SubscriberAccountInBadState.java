package com.meinc.gameplay.exception;

public class SubscriberAccountInBadState extends RuntimeException
{
    private static final long serialVersionUID = -485674219394358404L;

    public SubscriberAccountInBadState()
    {
        super("The subscriber's account is in an inconsistent state.");
    }
}
