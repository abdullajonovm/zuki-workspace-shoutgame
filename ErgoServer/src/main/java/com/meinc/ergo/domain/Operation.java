package com.meinc.ergo.domain;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.meinc.ergo.util.OperationDeserializer;

@JsonDeserialize(using=OperationDeserializer.class)
public class Operation
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum OPERATION_TYPE {ADD, UPDATE, DELETE}
    public static enum OBJECT_TYPE {ROLE, NOTE, TASK}
    public static enum CASCADE_TYPE {REASSIGN, DELETE}
    
    private OPERATION_TYPE operationType;
    private OBJECT_TYPE objectType;
    private CASCADE_TYPE cascadeType;
    private BaseEntityObject object;
    
    /** a unique id passed in by the client that will be passed back in a list of success/fail ops if the import completes unsuccessfully so that the client knows what needs to be re-submitted */
    private String operationId;
    
    public OPERATION_TYPE getOperationType()
    {
        return operationType;
    }
    public void setOperationType(OPERATION_TYPE operationType)
    {
        this.operationType = operationType;
    }
    public OBJECT_TYPE getObjectType()
    {
        return objectType;
    }
    public void setObjectType(OBJECT_TYPE objectType)
    {
        this.objectType = objectType;
    }
    public CASCADE_TYPE getCascadeType()
    {
        return cascadeType;
    }
    public void setCascadeType(CASCADE_TYPE cascadeType)
    {
        this.cascadeType = cascadeType;
    }
    public BaseEntityObject getObject()
    {
        return object;
    }
    public void setObject(BaseEntityObject object)
    {
        this.object = object;
    }
    
    public String getOperationId()
    {
        return operationId;
    }
    public void setOperationId(String operationId)
    {
        this.operationId = operationId;
    }
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("operationId: ").append(operationId).append(" - ");
        buf.append(operationType).append(" ").append(objectType);
        if (cascadeType != null) {
            buf.append(" (").append(cascadeType).append(" )");
        }
        buf.append(": ").append(object);

        return buf.toString();
    }
    
}
