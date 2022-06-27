package com.shawker.queue;

import java.io.Serializable;

public class AsyncError
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private String providerUuid;
    private FailedOperation failedOperation;

    public AsyncError(int subscriberId, String providerUuid, FailedOperation failedOperation)
    {
        this.subscriberId = subscriberId;
        this.providerUuid = providerUuid;
        this.failedOperation = failedOperation;
    }

    public int getSubscriberId()
    {
        return subscriberId;
    }

    public String getProviderUuid()
    {
        return providerUuid;
    }

    public FailedOperation getFailedOperation()
    {
        return failedOperation;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("sId: ").append(subscriberId).append(", ");
        buf.append("pId: ").append(providerUuid).append(", ");
        buf.append("failedOperation: ").append(failedOperation);

        return buf.toString();
    }

}
