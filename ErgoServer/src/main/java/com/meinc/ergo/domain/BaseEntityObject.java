package com.meinc.ergo.domain;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@JsonSubTypes({ @Type(value = Role.class, name = "role"), @Type(value = Note.class, name = "note"), @Type(value = Task.class, name = "task") }) 
public abstract class BaseEntityObject 
extends BaseDomainObject
{
    private static final long serialVersionUID = 1L;
    public static enum PROVIDER_TYPE {ERGO, EXCHANGE, GOOGLE, APPLE}
    
//    @JsonIgnore
    private String providerUuid = null;
    
    @JsonIgnore
    private PROVIDER_TYPE providerType = PROVIDER_TYPE.ERGO;
    
    private int order;
    
    @JsonIgnore
    private String serverId;
    
    @JsonIgnore
    private String etag;
    
    @JsonIgnore
    private Date lastServerSyncTime;
    
    @JsonIgnore
    private int subscriberId;
    
    @JsonProperty("providerId")
    public String getProviderUuid()
    {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid)
    {
        this.providerUuid = providerUuid;
    }

    public PROVIDER_TYPE getProviderType()
    {
        return providerType;
    }

    public void setProviderType(PROVIDER_TYPE providerType)
    {
        this.providerType = providerType;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }
    
    public String getServerId()
    {
        return serverId;
    }

    public void setServerId(String serverId)
    {
        this.serverId = serverId;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getLastServerSyncTime()
    {
        return lastServerSyncTime;
    }

    public void setLastServerSyncTime(Date lastServerSyncTime)
    {
        this.lastServerSyncTime = lastServerSyncTime;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(", providerType: ").append(providerType);
        buf.append(", providerUuid: ").append(providerUuid);
        if (getServerId() != null && getServerId().length() > 20) {
            buf.append(", serverId: ").append(getServerId().substring(0, 10)).append("...").append(getServerId().substring(getServerId().length()-10));
        } else {
            buf.append(", serverId: ").append(getServerId());
        }
        buf.append(", order: ").append(order);
        buf.append(", lastServerSyncTime: ").append(lastServerSyncTime);
        buf.append(super.toString());

        return buf.toString();
    }

}
