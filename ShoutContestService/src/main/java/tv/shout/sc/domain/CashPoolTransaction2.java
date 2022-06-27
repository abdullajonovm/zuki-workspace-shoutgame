package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Date;

public class CashPoolTransaction2
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum TYPE {
        BONUS,                  //a bonus given to the subscriber. the description field details what the bonus was
        PURCHASE,               //the subscriber purchased credits using real money
        JOINED_ROUND,           //the subscriber spent credits to join a round
        PURCHASED_EXTRA_LIFE,   //the subscriber spent credits to purchase extra lifes
        ABANDONED_ROUND,        //the subscriber was refunded credits after abandoning a round (before they played it - unable to abandon after play begins)
        PAYOUT,                 //the subscriber was awarded money into their account from winning
        PAYOUT_REFERRAL,        //the subscriber was awarded money into their account from a referral of theirs who won
        PAID,                   //the subscriber withdrew funds and was paid real money
    }

    private long _cashpoolTransactionId;
    private long _subscriberId;
    private double _amount;
    private TYPE _type;
    private String _description;

    private double _currentPoolAmount; //how much is currently in the pool after this amount/type is taken into account
    private double _currentBonusAmount; //how much is currently in the bonus pool after this amount/type is taken into account
    private Double _usedPoolAmount; //if type == JOINED_ROUND, this holds how much of the money was deducted from the pool value
    private Double _usedBonusAmount; //if type == JOINED_ROUND, this holds how much of the money was deducted from the bonus value

    private Integer _receiptId;
    private String _contextUuid;
    private Date _transactionDate;

    public long getCashpoolTransactionId()
    {
        return _cashpoolTransactionId;
    }
    public void setCashpoolTransactionId(long cashpoolTransactionId)
    {
        _cashpoolTransactionId = cashpoolTransactionId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
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
    public TYPE getType()
    {
        return _type;
    }
    public void setType(TYPE type)
    {
        _type = type;
    }
    public String getDescription()
    {
        return _description;
    }
    public void setDescription(String description)
    {
        _description = description;
    }
    public double getCurrentPoolAmount()
    {
        return _currentPoolAmount;
    }
    public void setCurrentPoolAmount(double currentPoolAmount)
    {
        _currentPoolAmount = currentPoolAmount;
    }
    public double getCurrentBonusAmount()
    {
        return _currentBonusAmount;
    }
    public void setCurrentBonusAmount(double currentBonusAmount)
    {
        _currentBonusAmount = currentBonusAmount;
    }
    public Double getUsedPoolAmount()
    {
        return _usedPoolAmount;
    }
    public void setUsedPoolAmount(Double usedPoolAmount)
    {
        _usedPoolAmount = usedPoolAmount;
    }
    public Double getUsedBonusAmount()
    {
        return _usedBonusAmount;
    }
    public void setUsedBonusAmount(Double usedBonusAmount)
    {
        _usedBonusAmount = usedBonusAmount;
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
    public Date getTransactionDate()
    {
        return _transactionDate;
    }
    public void setTransactionDate(Date transactionDate)
    {
        _transactionDate = transactionDate;
    }

}
