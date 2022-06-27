package com.meinc.ergo.exception;

import com.meinc.ergo.domain.Provider;
import com.meinc.ergo.domain.Provider.TYPE;

public class UnsupportedVersionException 
extends Exception
{
    private static final long serialVersionUID = 1L;
    private TYPE _type = Provider.TYPE.ERGO;
    
    public UnsupportedVersionException()
    {
        super();
    }
    
    public UnsupportedVersionException(Provider.TYPE type)
    {
        super();
        _type = type;
    }
    
    public UnsupportedVersionException(String msg)
    {
        super(msg);
    }
    
    public Provider.TYPE getType()
    {
        return _type;
    }

}
