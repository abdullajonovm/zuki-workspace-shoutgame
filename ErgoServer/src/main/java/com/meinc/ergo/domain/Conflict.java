package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.meinc.ergo.util.ConflictDeserializer;

@JsonDeserialize(using=ConflictDeserializer.class)
public class Conflict
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static enum CONFLICT_TYPE {DEPENDENT_ROLE_DELETED, UPDATE, DELETE}
    public static enum OBJECT_TYPE {ROLE, NOTE, TASK}
    public static enum RESOLUTION_TYPE {AUTOMATIC, MANUAL}

    private CONFLICT_TYPE conflictType;
    private OBJECT_TYPE objectType;
    private BaseEntityObject clientObject;
    private BaseEntityObject serverObject;
    private List<String> fieldsInConflict;
    private RESOLUTION_TYPE resolutionType;
    
    public Conflict()
    {
    }
    
    public Conflict(CONFLICT_TYPE conflictType, OBJECT_TYPE objectType, RESOLUTION_TYPE resolutionType, BaseEntityObject clientObject, BaseEntityObject serverObject)
    {
        this.conflictType = conflictType;
        this.objectType = objectType;
        this.resolutionType = resolutionType;
        this.clientObject = clientObject;
        this.serverObject = serverObject;
    }
    
    public Conflict(CONFLICT_TYPE conflictType, OBJECT_TYPE objectType, RESOLUTION_TYPE resolutionType, BaseEntityObject clientObject, BaseEntityObject serverObject, List<String> fieldsInConflict)
    {
        this.conflictType = conflictType;
        this.objectType = objectType;
        this.resolutionType = resolutionType;
        this.clientObject = clientObject;
        this.serverObject = serverObject;
        this.fieldsInConflict = fieldsInConflict;
    }
    
    public CONFLICT_TYPE getConflictType()
    {
        return conflictType;
    }
    public void setConflictType(CONFLICT_TYPE conflictType)
    {
        this.conflictType = conflictType;
    }
    public OBJECT_TYPE getObjectType()
    {
        return objectType;
    }
    public void setObjectType(OBJECT_TYPE objectType)
    {
        this.objectType = objectType;
    }
    public BaseEntityObject getClientObject()
    {
        return clientObject;
    }
    public void setClientObject(BaseEntityObject clientObject)
    {
        this.clientObject = clientObject;
    }
    public BaseEntityObject getServerObject()
    {
        return serverObject;
    }
    public void setServerObject(BaseEntityObject serverObject)
    {
        this.serverObject = serverObject;
    }
    public List<String> getFieldsInConflict()
    {
        return fieldsInConflict;
    }
    public void setFieldsInConflict(List<String> fieldsInConflict)
    {
        this.fieldsInConflict = fieldsInConflict;
    }
    public RESOLUTION_TYPE getResolutionType()
    {
        return resolutionType;
    }
    public void setResolutionType(RESOLUTION_TYPE resolutionType)
    {
        this.resolutionType = resolutionType;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("type: ").append(conflictType);
        buf.append(", obj: ").append(objectType);
        buf.append(", resolution: ").append(resolutionType);
        buf.append(", fields: ").append(fieldsInConflict);

        return buf.toString();
    }
}
