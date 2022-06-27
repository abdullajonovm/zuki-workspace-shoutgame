package com.meinc.notification.domain;

import java.io.Serializable;

import com.meinc.mrsoa.service.ServiceEndpoint;

public class NotificationCallback 
implements Serializable 
{
    private static final long serialVersionUID = -7232374428512315125L;
    private int _id;
    private ServiceEndpoint _endpoint;
    private String _methodName;
    private String _type;
    
    public NotificationCallback()
    {
    }
    
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

    public String getType() {
        return _type;
    }
    
    public void setType(String type) {
        _type = type;
    }

}
