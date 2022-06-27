package com.meinc.push.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

public class SubscriberToken
implements Serializable
{
    private static final long serialVersionUID = 3738139829707137739L;

    private int _tokenId;
    private long _subscriberId;
    private String _deviceToken;
    private String _appBundleId;
    private String _deviceUuid;
    private String _deviceType;
    private Date _lastRegistration;
    private boolean _deviceActive;

    public SubscriberToken()
    {
    }

    public SubscriberToken(String deviceToken)
    {
        _deviceToken = deviceToken;
    }

    public int getTokenId() {
        return _tokenId;
    }

    public void setTokenId(int tokenId) {
        _tokenId = tokenId;
    }

    public long getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        _subscriberId = subscriberId;
    }

    public String getDeviceToken() {
        return _deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        _deviceToken = deviceToken;
    }

    public String getAppBundleId() {
		return _appBundleId;
	}

	public void setAppBundleId(String appBundleId) {
		_appBundleId = appBundleId;
	}

	public void setDeviceUuid(String deviceUuid) {
        _deviceUuid = deviceUuid;
    }

    public String getDeviceUuid() {
        return _deviceUuid;
    }

    public String getDeviceType() {
        return _deviceType;
    }

    public void setDeviceType(String deviceType) {
        _deviceType = deviceType;
    }

    public Date getLastRegistration() {
        return _lastRegistration;
    }

    public void setLastRegistration(Date lastRegistration) {
        _lastRegistration = lastRegistration;
    }

    public boolean isDeviceActive() {
        return _deviceActive;
    }

    public void setDeviceActive(boolean deviceActive) {
        _deviceActive = deviceActive;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("bundleId : {0}, sId: {1}, type: {2}, active: {3}, uuid: {4}, token: {5}",
            _appBundleId,
            _subscriberId,
            _deviceType,
            _deviceActive,
            _deviceUuid != null && _deviceUuid.length() > 10 ? _deviceUuid.subSequence(0, 10) + "..." : _deviceUuid,
            _deviceToken != null && _deviceToken.length() > 10 ? _deviceToken.subSequence(0, 10) + "..." : _deviceToken
        );
    }

}
