package com.meinc.gameplay.exception;

public class QuestionNotFoundException 
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public QuestionNotFoundException()
    {
        super();
    }
    
    public QuestionNotFoundException(String message)
    {
        super(message);
    }
}
