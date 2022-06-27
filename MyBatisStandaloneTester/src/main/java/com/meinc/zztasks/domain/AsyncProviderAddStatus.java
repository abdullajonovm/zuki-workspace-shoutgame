package com.meinc.zztasks.domain;

import java.text.MessageFormat;

public class AsyncProviderAddStatus
{
    private String providerId;
    private String status;
    public String getProviderId()
    {
        return providerId;
    }
    public void setProviderId(String providerId)
    {
        this.providerId = providerId;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    @Override
    public String toString()
    {
        return MessageFormat.format("{0}: {1}", providerId, status);
    }
}
