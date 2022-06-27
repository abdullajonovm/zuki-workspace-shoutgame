package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class CouponBatch
implements Serializable
{
    private int _batchId;
    private String _batchName;
    private double _amount;
    private Date _createDate;
    private Date _expireDate; //optional

    public int getBatchId()
    {
        return _batchId;
    }
    public void setBatchId(int batchId)
    {
        _batchId = batchId;
    }
    public String getBatchName()
    {
        return _batchName;
    }
    public void setBatchName(String batchName)
    {
        _batchName = batchName;
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
}
