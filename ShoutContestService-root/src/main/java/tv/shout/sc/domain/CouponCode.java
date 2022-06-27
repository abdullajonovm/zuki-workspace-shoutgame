package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class CouponCode
implements Serializable
{
    private int _couponId;
    private String _couponCode;
    private int _batchId;
    private double _amount;
    private Date _createDate;
    private Date _expireDate;
    private boolean _cancelled;
    private Date _cancelledDate;
    private long _redeemedBySubscriberId;
    private Date _redeemedDate;

    public int getCouponId()
    {
        return _couponId;
    }
    public void setCouponId(int couponId)
    {
        _couponId = couponId;
    }
    public String getCouponCode()
    {
        return _couponCode;
    }
    public void setCouponCode(String couponCode)
    {
        _couponCode = couponCode;
    }
    public int getBatchId()
    {
        return _batchId;
    }
    public void setBatchId(int batchId)
    {
        _batchId = batchId;
    }
    public double getAmount()
    {
        return _amount;
    }
    public void setAmount(double amount)
    {
        _amount = amount;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getExpireDate()
    {
        return _expireDate;
    }
    public void setExpireDate(Date expireDate)
    {
        _expireDate = expireDate;
    }
    public boolean isCancelled()
    {
        return _cancelled;
    }
    public void setCancelled(boolean cancelled)
    {
        _cancelled = cancelled;
    }
    public Date getCancelledDate()
    {
        return _cancelledDate;
    }
    public void setCancelledDate(Date cancelledDate)
    {
        _cancelledDate = cancelledDate;
    }
    public long getRedeemedBySubscriberId()
    {
        return _redeemedBySubscriberId;
    }
    public void setRedeemedBySubscriberId(long redeemedBySubscriberId)
    {
        _redeemedBySubscriberId = redeemedBySubscriberId;
    }
    public Date getRedeemedDate()
    {
        return _redeemedDate;
    }
    public void setRedeemedDate(Date redeemedDate)
    {
        _redeemedDate = redeemedDate;
    }

}
