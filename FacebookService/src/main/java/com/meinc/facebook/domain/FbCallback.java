package com.meinc.facebook.domain;

import java.io.Serializable;

import com.meinc.mrsoa.service.ServiceEndpoint;

public class FbCallback implements Serializable {
    private static final long serialVersionUID = 6795016834619988915L;
    private int _id;
    private ServiceEndpoint _endpoint;
    private String _methodName;

    public void setId(int id)
    {
        _id = id;
    }

    public int getId()
    {
        return _id;
    }
    
    public ServiceEndpoint getEndpoint() {
        return _endpoint;
    }
    
    public void setEndpoint(ServiceEndpoint callbackEndpoint) {
        _endpoint = callbackEndpoint;
    }
    
    public String getMethodName() {
        return _methodName;
    }

    public void setMethodName(String methodName) {
        _methodName = methodName;
    }

}
