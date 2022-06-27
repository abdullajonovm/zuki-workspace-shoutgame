package com.meinc.identity.domain;

import java.io.Serializable;

public class ForeignHostIdentityInfo
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long _subscriberId;
    private String _foreignHostSubscriberId;
    private String _foreignHostAppId;
    private int _contextId;

    public long getSubscriberId()    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)    {
        _subscriberId = subscriberId;
    }

    public String getForeignHostSubscriberId()    {
        return _foreignHostSubscriberId;
    }

    public void setFacebookId(String value)    {
        _foreignHostSubscriberId = value;
    }

    public String getForeignHostAppId() {
        return _foreignHostAppId;
    }

    public void setFacebookAppId(String value) {
        _foreignHostAppId = value;
    }

    public int getContextId() {
        return _contextId;
    }

    public void setContextId(int contextId) {
        _contextId = contextId;
    }
}