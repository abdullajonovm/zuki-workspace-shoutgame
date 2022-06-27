package com.meinc.http.domain;

import java.io.Serializable;

public class NotAuthorizedException extends Exception implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5509752892744551601L;

    public NotAuthorizedException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public NotAuthorizedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public NotAuthorizedException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public NotAuthorizedException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
