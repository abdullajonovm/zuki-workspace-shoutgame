package com.meinc.ergo.exception;

public class StaleDataTryBackException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    private Object staleData;
    
    public StaleDataTryBackException(Object staleData)
    {
        super();
        this.staleData = staleData;
    }
    
    public StaleDataTryBackException(String msg, Object staleData)
    {
        super(msg);
        this.staleData = staleData;
    }

    public Object getStaleData()
    {
        return staleData;
    }

    public void setStaleData(Object staleData)
    {
        this.staleData = staleData;
    }

}
