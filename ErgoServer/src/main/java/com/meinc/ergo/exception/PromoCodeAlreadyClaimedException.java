package com.meinc.ergo.exception;

public class PromoCodeAlreadyClaimedException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public PromoCodeAlreadyClaimedException()
    {
        super();
    }
    
    public PromoCodeAlreadyClaimedException(String msg)
    {
        super(msg);
    }

}
