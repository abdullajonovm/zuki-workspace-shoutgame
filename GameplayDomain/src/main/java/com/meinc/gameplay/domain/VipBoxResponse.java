package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class VipBoxResponse implements Serializable {
    
    private static final long serialVersionUID = -7842071554231406947L;
    private int _vipBoxId;
    private List<String> _phoneNumbers;
    private Boolean _facebookAuthenticationNeeded;
    
    public VipBoxResponse() {
        
    }
    
    public VipBoxResponse(int vipBoxId, List<String> phoneNumbers) {
        _vipBoxId = vipBoxId;
        _phoneNumbers = phoneNumbers;
    }
    
    public int getVipBoxId() {
        return _vipBoxId;
    }
    
    public void setVipBoxId(int vipBoxId) {
        _vipBoxId = vipBoxId;
    }
    
    public List<String> getPhoneNumbers() {
        return _phoneNumbers;
    }
    
    public void setPhoneNumbers(List<String> phoneNumbers) {
        _phoneNumbers = phoneNumbers;
    }

    public Boolean getFacebookAuthenticationNeeded() {
        if (_facebookAuthenticationNeeded == null) {
            return false;
        }
        return _facebookAuthenticationNeeded;
    }

    public void setFacebookAuthenticationNeeded(Boolean facebookAuthenticationNeeded) {
        _facebookAuthenticationNeeded = facebookAuthenticationNeeded;
    }
    
}
