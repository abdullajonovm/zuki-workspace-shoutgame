package com.meinc.identity.exception;

import java.io.Serializable;

public class DeviceNotFoundException 
extends Exception
implements Serializable
{
    private static final long serialVersionUID = 855122447877312176L;

    public DeviceNotFoundException() {
    }

    public DeviceNotFoundException(String arg0) {
        super(arg0);
    }

    public DeviceNotFoundException(Throwable arg0) {
        super(arg0);
    }

    public DeviceNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
