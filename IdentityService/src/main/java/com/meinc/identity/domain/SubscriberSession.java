package com.meinc.identity.domain;

import java.io.Serializable;
import java.util.Date;

public class SubscriberSession
implements Serializable
{
    public static final int classId = 1;
    private static final long serialVersionUID = -2008214037645322793L;

    private long _subscriberId;
    private int _contextId;
    private String _deviceId;
    private String _sessionKey;
    private String _deviceModel;
    private String _deviceName;
    private String _deviceVersion;
    private String _osName;
    private String _osType;
    private String _appId;
    private String _appVersion;
    private Date _addedDate;
    private Date _lastAuthenticatedDate;

    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public int getContextId()
    {
        return _contextId;
    }
    public void setContextId(int contextId)
    {
        _contextId = contextId;
    }
    public String getDeviceId()
    {
        return _deviceId;
    }
    public void setDeviceId(String deviceId)
    {
        _deviceId = deviceId;
    }
    public String getSessionKey()
    {
        return _sessionKey;
    }
    public void setSessionKey(String sessionKey)
    {
        _sessionKey = sessionKey;
    }
    public String getDeviceModel()
    {
        return _deviceModel;
    }
    public void setDeviceModel(String deviceModel)
    {
        _deviceModel = deviceModel;
    }
    public String getDeviceName()
    {
        return _deviceName;
    }
    public void setDeviceName(String deviceName)
    {
        _deviceName = deviceName;
    }
    public String getDeviceVersion()
    {
        return _deviceVersion;
    }
    public void setDeviceVersion(String deviceVersion)
    {
        _deviceVersion = deviceVersion;
    }
    public String getOsName()
    {
        return _osName;
    }
    public void setOsName(String osName)
    {
        _osName = osName;
    }
    public String getOsType()
    {
        return _osType;
    }
    public void setOsType(String osType)
    {
        _osType = osType;
    }
    public String getAppId()
    {
        return _appId;
    }
    public void setAppId(String appId)
    {
        _appId = appId;
    }
    public String getAppVersion()
    {
        return _appVersion;
    }
    public void setAppVersion(String appVersion)
    {
        _appVersion = appVersion;
    }
    public Date getAddedDate()
    {
        return _addedDate;
    }
    public void setAddedDate(Date addedDate)
    {
        _addedDate = addedDate;
    }
    public Date getLastAuthenticatedDate()
    {
        return _lastAuthenticatedDate;
    }
    public void setLastAuthenticatedDate(Date lastAuthenticatedDate)
    {
        _lastAuthenticatedDate = lastAuthenticatedDate;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("subscriberId: ").append(_subscriberId);
        buf.append(", contextId: ").append(_contextId);
        buf.append(", deviceId: ").append(_deviceId);
        buf.append(" (model: ").append(_deviceModel);
        buf.append(", name: ").append(_deviceName);
        buf.append(", version: ").append(_deviceVersion);
        buf.append("), os: (").append(_osName);
        buf.append(", type: ").append(_osType);
        buf.append("), app: (").append(_appId);
        buf.append(", version: ").append(_appVersion);
        buf.append("), session: ").append(_sessionKey);
        buf.append(", added: ").append(_addedDate);
        buf.append(", last auth: ").append(_lastAuthenticatedDate);

        return buf.toString();
    }
}
