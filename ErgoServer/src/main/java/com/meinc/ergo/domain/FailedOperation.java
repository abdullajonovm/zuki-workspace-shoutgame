package com.meinc.ergo.domain;

import java.io.Serializable;

public class FailedOperation
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum TYPE {ROLE, NOTE, TASK, IMPORT, OFFLINE_SYNC, PROVIDER}
    public static enum OPERATION {ADD, GET, UPDATE, DELETE, GETALL, IMPORT, OFFLINE_SYNC}

    private String objectId;
    private TYPE type;
    private OPERATION operation;
    private String errorType;

    public FailedOperation(String objectId, TYPE type, OPERATION operation)
    {
        this.objectId = objectId;
        this.type = type;
        this.operation = operation;
    }
    
    public String getObjectId()
    {
        return objectId;
    }
    
    public TYPE getType()
    {
        return type;
    }

    public OPERATION getOperation()
    {
        return operation;
    }
    
    public String getErrorType()
    {
        return errorType;
    }

    public void setErrorType(String errorType)
    {
        this.errorType = errorType;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("objectId: ").append(objectId).append(", ");
        buf.append("type: ").append(type).append(", ");
        buf.append("operation: ").append(operation).append(", ");
        buf.append("errorType: ").append(errorType);

        return buf.toString();
    }
}
