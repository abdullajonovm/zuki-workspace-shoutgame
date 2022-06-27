package com.meinc.ergo.exception;

public class WebdavException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public WebdavException()
    {
        super();
    }
    
    public WebdavException(String msg)
    {
        super(msg);
    }

}
