package com.meinc.store.domain;

import java.io.Serializable;
import java.util.Date;

public class CouponCode 
implements Serializable
{
    private static final long serialVersionUID = 1;
    
    private int _couponId;
    private String _code;
    private String _itemUuid;
    private Date _createDate;
    private Date _redeemDate;
    private Date _cancelDate;
    
    public int getCouponId() {
        return _couponId;
    }
    public void setCouponId(int couponId) {
        _couponId = couponId;
    }
    public String getCode()
    {
        return _code;
    }
    public void setCode(String code)
    {
        _code = code;
    }
    public String getItemUuid()
    {
        return _itemUuid;
    }
    public void setItemUuid(String itemUuid)
    {
        _itemUuid = itemUuid;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getRedeemDate()
    {
        return _redeemDate;
    }
    public void setRedeemDate(Date redeemDate)
    {
        _redeemDate = redeemDate;
    }
    public Date getCancelDate()
    {
        return _cancelDate;
    }
    public void setCancelDate(Date cancelDate)
    {
        _cancelDate = cancelDate;
    }
}
