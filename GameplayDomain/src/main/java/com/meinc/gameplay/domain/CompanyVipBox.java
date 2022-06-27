package com.meinc.gameplay.domain;

import java.io.Serializable;

public class CompanyVipBox implements Serializable {
    

    private static final long serialVersionUID = 506053439187048635L;
    private int _id;
    private String _companyName;
    private int _companySubscriberId;
    private int _vipBoxId;

    public String getCompanyName() {
        return _companyName;
    }

    public void setCompanyName(String companyName) {
        _companyName = companyName;
    }

    public int getCompanySubscriberId() {
        return _companySubscriberId;
    }

    public void setCompanySubscriberId(int companySubscriberId) {
        _companySubscriberId = companySubscriberId;
    }

    public int getVipBoxId() {
        return _vipBoxId;
    }

    public void setVipBoxId(int vipBoxId) {
        _vipBoxId = vipBoxId;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    
}
