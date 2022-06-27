package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;

public class ManualRedeemRequest
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _manualRedeemRequestId;
    private long _subscriberId;
    private float _amount;
    private Date _requestDate;
    private Date _fulfilledDate;
    private Date _cancelledDate;

    public int getManualRedeemRequestId()
    {
        return _manualRedeemRequestId;
    }
    public void setManualRedeemRequestId(int manualRedeemRequestId)
    {
        _manualRedeemRequestId = manualRedeemRequestId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public float getAmount()
    {
        return _amount;
    }
    public void setAmount(float amount)
    {
        _amount = amount;
    }
    public Date getRequestDate()
    {
        return _requestDate;
    }
    public void setRequestDate(Date requestDate)
    {
        _requestDate = requestDate;
    }
    public Date getFulfilledDate()
    {
        return _fulfilledDate;
    }
    public void setFulfilledDate(Date fulfilledDate)
    {
        _fulfilledDate = fulfilledDate;
    }
    public Date getCancelledDate()
    {
        return _cancelledDate;
    }
    public void setCancelledDate(Date cancelledDate)
    {
        _cancelledDate = cancelledDate;
    }
}
