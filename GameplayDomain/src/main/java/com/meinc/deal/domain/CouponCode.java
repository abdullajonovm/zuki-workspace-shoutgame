package com.meinc.deal.domain;

import java.io.Serializable;

public class CouponCode implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8904814704169968318L;
    private int _id;
    private int _couponId;
    private String code;
    
    public int getId() {
        return _id;
    }
    
    public void setId(int id) {
        _id = id;
    }
    
    public int getCouponId() {
        return _couponId;
    }
    
    public void setCouponId(int couponId) {
        _couponId = couponId;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

}
