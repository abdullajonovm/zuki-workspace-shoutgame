package com.meinc.ergo.exception;

public class NoteNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public NoteNotFoundException()
    {
        super();
    }
    
    public NoteNotFoundException(String msg)
    {
        super(msg);
    }

}
