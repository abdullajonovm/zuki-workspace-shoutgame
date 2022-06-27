package com.meinc.store.domain;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class SubscriberEntitlement implements Serializable, Cloneable
{
    private static final long serialVersionUID = -4138621810331035184L;

    private int _subscriberEntitlementId;
    private String _uuid;
    private long _subscriberId;
    private int _entitlementId;
    private String _entitlementUuid;
    private Receipt _receipt;
    private Date _deleteDate;
    private Date _reservedDate;
    private Date _consumedDate;
    private int _contextId;

    public SubscriberEntitlement()
    {
    }

    public SubscriberEntitlement(String uuid, long subscriberId, int entitlementId)
    {
        _uuid = uuid;
        _subscriberId = subscriberId;
        _entitlementId = entitlementId;
    }

    public SubscriberEntitlement(String uuid, long subscriberId, int entitlementId, int contextId) {
        _uuid = uuid;
        _subscriberId = subscriberId;
        _entitlementId = entitlementId;
        _contextId = contextId;
    }

    public SubscriberEntitlement(String uuid, long subscriberId, int entitlementId, String entitlementUuid, int contextId) {
        _uuid = uuid;
        _subscriberId = subscriberId;
        _entitlementId = entitlementId;
        _entitlementUuid = entitlementUuid;
        _contextId = contextId;
    }

    @JsonIgnore
    public int getSubscriberEntitlementId()
    {
        return _subscriberEntitlementId;
    }

    public void setSubscriberEntitlementId(int subscriberEntitlementId)
    {
        _subscriberEntitlementId = subscriberEntitlementId;
    }

    @JsonProperty("subscriberEntitlementId")
    public String getUuid()
    {
        return _uuid;
    }

    public void setUuid(String uuid)
    {
        _uuid = uuid;
    }

    @JsonIgnore
    public long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    @JsonIgnore
    public int getEntitlementId()
    {
        return _entitlementId;
    }

    public void setEntitlementId(int entitlementId)
    {
        _entitlementId = entitlementId;
    }

    @JsonProperty("entitlementId")
    public String getEntitlementUuid() {
        return _entitlementUuid;
    }

    public void setEntitlementUuid(String entitlementUuid) {
        _entitlementUuid = entitlementUuid;
    }

    public Receipt getReceipt() {
        return _receipt;
    }

    public void setReceipt(Receipt receipt) {
        _receipt = receipt;
    }

    @JsonIgnore
    public Date getDeleteDate() {
        return _deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        _deleteDate = deleteDate;
    }

    public Date getReservedDate()
    {
        return _reservedDate;
    }

    public void setReservedDate(Date reservedDate)
    {
        _reservedDate = reservedDate;
    }

    public Date getConsumedDate()
    {
        return _consumedDate;
    }

    public void setConsumedDate(Date consumedDate)
    {
        _consumedDate = consumedDate;
    }

    public int getContextId()
    {
        return _contextId;
    }

    public void setContextId(int contextId)
    {
        _contextId = contextId;
    }

    @Override
    public String toString() {
        return String.format("SubscriberEntitlement{uuid: %s, subscriberId: %d, entitlementId: %d, entitlementUuid: %s, deleted: %s, reserved: %s, consumed: %s, contextId: %d}",
                _uuid, _subscriberId, _entitlementId, _entitlementUuid, _deleteDate, _reservedDate, _consumedDate, _contextId);
    }

    @Override
    public SubscriberEntitlement clone() {
        try {
            return (SubscriberEntitlement) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
