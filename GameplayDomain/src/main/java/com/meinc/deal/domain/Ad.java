package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Ad implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4675612976077603321L;
    private int _id;
    private int _sponsorId;
    private String _type;
    private List<AdInstance> _instances;
    
    @JsonProperty(value="adId")
    public int getId() {
        return _id;
    }
    
    @JsonProperty(value="adId")
    public void setId(int id) {
        _id = id;
    }
    
    public int getSponsorId() {
        return _sponsorId;
    }
    
    public void setSponsorId(int sponsorId) {
        _sponsorId = sponsorId;
    }
    
    public String getType() {
        return _type;
    }
    
    public void setType(String type) {
        _type = type;
    }
    
    public void setInstances(List<AdInstance> instances) {
        _instances = instances;
    }
    
    public List<AdInstance> getInstances() {
        return _instances;
    }
}
