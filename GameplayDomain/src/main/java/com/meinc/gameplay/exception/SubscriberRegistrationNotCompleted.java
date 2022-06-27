package com.meinc.gameplay.exception;

public class SubscriberRegistrationNotCompleted extends RuntimeException
{
    private static final long serialVersionUID = -4105969666972168649L;

    public SubscriberRegistrationNotCompleted()
    {
        super("The subscriber hasn't completed registration yet.");
    }
}
