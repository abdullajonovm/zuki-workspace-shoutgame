package com.meinc.ergo.exception;

public class ExcelCreationException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ExcelCreationException()
    {
        super();
    }
    
    public ExcelCreationException(String msg)
    {
        super(msg);
    }

}
