package com.meinc.ergo.exception;

public class PromoCodeNotFoundException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoCodeNotFoundException()
    {
        super();
    }
    
    public PromoCodeNotFoundException(String msg)
    {
        super(msg);
    }

}
