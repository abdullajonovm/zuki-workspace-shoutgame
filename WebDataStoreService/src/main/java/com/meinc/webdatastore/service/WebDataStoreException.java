package com.meinc.webdatastore.service;

import static com.meinc.webdatastore.service.WebDataStoreException.Type.UNKNOWN;

import java.util.ArrayList;
import java.util.List;

import com.meinc.webdatastore.domain.RepeatWebDataStoreObject;
import com.meinc.webdatastore.domain.RepeatWebDataStoreObjects;

public class WebDataStoreException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public static enum Type {
        UNKNOWN,
        /** The target object does not exist */
        MISSING,
        /** The target object already exists */
        DUPLICATE,
    }
    
    private Type type = UNKNOWN;
    private int httpStatus;
    private RepeatWebDataStoreObjects repeatObjects;

    public WebDataStoreException() {
        super();
    }

    public WebDataStoreException(String arg0, Throwable arg1) {
        super(new WebDataStoreWrapperException(arg0, arg1));
    }

    public WebDataStoreException(String arg0, Exception arg1, int httpStatus) {
        this(arg0, arg1);
        this.httpStatus = httpStatus;
    }

    public WebDataStoreException(String arg0) {
        super(arg0);
    }

    public WebDataStoreException(String arg0, int httpStatus) {
        this(arg0);
        this.httpStatus = httpStatus;
    }

    public WebDataStoreException(Throwable arg0) {
        super(new WebDataStoreWrapperException(arg0));
    }

    public WebDataStoreException(int httpStatus) {
        super("HTTP status " + httpStatus);
        this.httpStatus = httpStatus;
    }

    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public RepeatWebDataStoreObjects getRepeatObjects() {
        return repeatObjects;
    }
    
    public synchronized void addRepeatObject(RepeatWebDataStoreObject repeatObject) {
        if (this.repeatObjects == null)
            this.repeatObjects = new RepeatWebDataStoreObjects();
        this.repeatObjects.addRepeatObject(repeatObject);
    }
    
    @Override
    public String toString() {
        return (httpStatus != 0 ? "("+httpStatus+") " : "") + getMessage();
    }
}

class WebDataStoreWrapperException extends Exception {
    private static final long serialVersionUID = 1L;

    public WebDataStoreWrapperException(Throwable arg0) {
        super(arg0.getClass().getName() + ": " + arg0.getMessage());
        setStackTrace(arg0.getStackTrace());
    }

    public WebDataStoreWrapperException(String arg0, Throwable arg1) {
        super(arg0 + "\" - " + arg1.getMessage());
        setStackTrace(arg1.getStackTrace());
    }
}
