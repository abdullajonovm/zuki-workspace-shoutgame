package com.meinc.ergo.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class Tag 
extends BaseDomainObject
{
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String description;
    
    @JsonProperty("tagId")
    @Override
    public String getUuid() { return uuid; }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("id: ").append(uuid);
        buf.append(", name: ").append(name);
        buf.append(", desc: ").append(description);

        return buf.toString();
    }
}
