package tv.shout.sm.admin;

import java.util.Date;

public class CashPoolTransaction
{
    private long _cashpoolTransactionId;
    private int _subscriberId;
    private double _amount;
    private String _description;
    private Integer _receiptId;
    private String _contextUuid;
    private String _externalRefId;
    private Date _transactionDate;

    public long getCashpoolTransactionId()
    {
        return _cashpoolTransactionId;
    }
    public void setCashpoolTransactionId(long cashpoolTransactionId)
    {
        _cashpoolTransactionId = cashpoolTransactionId;
    }
    public int getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public double getAmount()
    {
        return _amount;
    }
    public void setAmount(double amount)
    {
        _amount = amount;
    }
    public String getDescription()
    {
        return _description;
    }
    public void setDescription(String description)
    {
        _description = description;
    }
    public Integer getReceiptId()
    {
        return _receiptId;
    }
    public void setReceiptId(Integer receiptId)
    {
        _receiptId = receiptId;
    }
    public String getContextUuid()
    {
        return _contextUuid;
    }
    public void setContextUuid(String contextUuid)
    {
        _contextUuid = contextUuid;
    }
    public String getExternalRefId()
    {
        return _externalRefId;
    }
    public void setExternalRefId(String externalRefId)
    {
        _externalRefId = externalRefId;
    }
    public Date getTransactionDate()
    {
        return _transactionDate;
    }
    public void setTransactionDate(Date transactionDate)
    {
        _transactionDate = transactionDate;
    }
}
