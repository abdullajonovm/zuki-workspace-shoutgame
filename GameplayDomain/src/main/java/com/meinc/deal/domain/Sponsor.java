package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.gameplay.domain.Localized;

public class Sponsor implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6235261426711725360L;
    private int _id;
    private String _name;
    private String _nameUuid;
    private List<Localized> _nameLocalized;
    
    @JsonProperty(value="sponsorId")
    public int getId() {
        return _id;
    }
    
    @JsonProperty(value="sponsorId")
    public void setId(int id) {
        _id = id;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        _name = name;
    }

    public String getNameUuid()
    {
        return _nameUuid;
    }

    public void setNameUuid(String nameUuid)
    {
        _nameUuid = nameUuid;
    }

    public List<Localized> getNameLocalized()
    {
        return _nameLocalized;
    }

    public void setNameLocalized(List<Localized> nameLocalized)
    {
        _nameLocalized = nameLocalized;
    }
}
