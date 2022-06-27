package com.meinc.identity.domain;

import java.io.Serializable;

public class SubscriberProfile implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2777807577524952551L;

    private String _nickname;
    private long _subscriberId;
    private String _countryCode;
    private String _facebookId;
    private String _photoUrl;

    public String getNickname() {
        return _nickname;
    }

    public void setNickname(String nickname) {
        _nickname = nickname;
    }

    public long getSubscriberId() {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        _subscriberId = subscriberId;
    }

    public String getCountryCode() {
        return _countryCode;
    }

    public void setCountryCode(String countryCode) {
        _countryCode = countryCode;
    }

    public String getFacebookId() {
        return _facebookId;
    }

    public void setFacebookId(String facebookId) {
        _facebookId = facebookId;
    }

    public String getPhotoUrl() {
        return _photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        _photoUrl = photoUrl;
    }

}
