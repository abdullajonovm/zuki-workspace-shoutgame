package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Metadata
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1009;
    private int _id;
    private String _name;
    private int _type;
    private int _level;
    private int _order;
    private boolean _active;
    private List<Metadata> _children;
    
    public Metadata()
    {
        _children = new ArrayList<Metadata>();
    }

    @JsonProperty(value="metadataId")
    public void setId(int id)
    {
        _id = id;
    }

    @JsonProperty(value="metadataId")
    public int getId()
    {
        return _id;
    }

    @JsonProperty(value="value")
    public void setName(String name)
    {
        _name = name;
    }

    @JsonProperty(value="value")
    public String getName()
    {
        return _name;
    }

    @JsonIgnore
    public void setLevel(int level)
    {
        _level = level;
    }

    @JsonIgnore
    public int getLevel()
    {
        return _level;
    }

    @JsonIgnore
    public List<Metadata> getChildren()
    {
        return _children;
    }

    @JsonIgnore
    public void setChildren(List<Metadata> children)
    {
        _children = children;
    }

    @JsonProperty(value="metadataTypeId")
    public void setType(int type)
    {
        _type = type;
    }

    @JsonProperty(value="metadataTypeId")
    public int getType()
    {
        return _type;
    }
    
    @JsonIgnore
    public void setActive(boolean active)
    {
        _active = active;
    }

    @JsonIgnore
    public boolean isActive()
    {
        return _active;
    }
    
    @JsonIgnore
    public int getOrder()
    {
        return _order;
    }

    @JsonIgnore
    public void setOrder(int order)
    {
        _order = order;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        for (int i=0; i<_level; i++)
            buf.append("    ");
        buf.append(_name);
        
        for (Metadata child : _children) {
            buf.append("\n").append(child.toString());
        }
        
        return buf.toString();
    }
}
