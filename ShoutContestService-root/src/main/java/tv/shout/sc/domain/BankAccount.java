package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;

public class BankAccount
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _id;
    private long _subscriberId;
    private String _bankName;
    private String _checkingAccountName;
    private String _routingNumber;
    private String _accountNumber;
    private Date _createDate;
    private Date _updateDate;

    public String getId()
    {
        return _id;
    }
    public void setId(String id)
    {
        _id = id;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public String getBankName()
    {
        return _bankName;
    }
    public void setBankName(String bankName)
    {
        _bankName = bankName;
    }
    public String getCheckingAccountName()
    {
        return _checkingAccountName;
    }
    public void setCheckingAccountName(String checkingAccountName)
    {
        _checkingAccountName = checkingAccountName;
    }
    public String getRoutingNumber()
    {
        return _routingNumber;
    }
    public void setRoutingNumber(String routingNumber)
    {
        _routingNumber = routingNumber;
    }
    public String getAccountNumber()
    {
        return _accountNumber;
    }
    public void setAccountNumber(String accountNumber)
    {
        _accountNumber = accountNumber;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getUpdateDate()
    {
        return _updateDate;
    }
    public void setUpdateDate(Date updateDate)
    {
        _updateDate = updateDate;
    }

}
