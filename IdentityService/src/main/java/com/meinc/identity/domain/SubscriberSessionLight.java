package com.meinc.identity.domain;

import java.io.Serializable;

public class SubscriberSessionLight
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String primaryId;
    private String encryptKey;
    private String deviceId;
    private Long subscriberId;
    private int _contextId;
    private String sessionKey;
    private boolean active;

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public int getContextId()
    {
        return _contextId;
    }

    public void setContextId(int contextId)
    {
        _contextId = contextId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
