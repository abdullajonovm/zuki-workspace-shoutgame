package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a subscriber record as shown on an event results response.
 */
public class SubscriberResult 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** the subscriberId */
    private int id; 
    
    /** the subscriber's nickname */
    private String alias;
    
    /** the subscriber's photoUrl (may be null) */
    private String photo;
    
    /** what the subscriber won (may be null) */
    private List<LocalizedValue> result;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public List<LocalizedValue> getResult()
    {
        return result;
    }

    public void setResult(List<LocalizedValue> result)
    {
        this.result = result;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(id);
        buf.append(", alias: ").append(alias);
        buf.append(", photo: ").append(photo);
        buf.append(", result: ").append(result);
        
        return buf.toString();
    }
    
}
