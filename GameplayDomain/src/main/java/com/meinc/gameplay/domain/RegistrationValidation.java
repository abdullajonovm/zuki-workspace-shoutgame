package com.meinc.gameplay.domain;

import java.io.Serializable;

public class RegistrationValidation
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String _validationResult;
    private int _subscriberId = 0;

    public RegistrationValidation()
    {
    }
    
    public RegistrationValidation(String validationResult, int subscriberId)
    {
        setValidationResult(validationResult);
        setSubscriberId(subscriberId);
    }

    public void setValidationResult(String validationResult)
    {
        _validationResult = validationResult;
    }

    public String getValidationResult()
    {
        return _validationResult;
    }

    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }
}
