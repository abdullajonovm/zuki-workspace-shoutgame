package com.meinc.identity.domain;

import java.io.Serializable;

public class FacebookIdentityInfo
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long _subscriberId;
    private String _facebookId;
    private String _facebookAppId;
    private int _contextId;

    public long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getFacebookId()
    {
        return _facebookId;
    }

    public void setFacebookId(String facebookId)
    {
        _facebookId = facebookId;
    }

    public String getFacebookAppId() {
        return _facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        _facebookAppId = facebookAppId;
    }

    public int getContextId() {
        return _contextId;
    }

    public void setContextId(int contextId) {
        _contextId = contextId;
    }
}
