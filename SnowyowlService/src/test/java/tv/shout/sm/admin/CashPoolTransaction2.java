package tv.shout.sm.admin;

import java.text.MessageFormat;
import java.util.Date;

public class CashPoolTransaction2
{
    public enum TYPE {
        BONUS,              //a bonus given to the subscriber. the description field details what the bonus was
        PURCHASE,           //the subscriber purchased credits using real money
        JOINED_ROUND,       //the subscriber spent credits to join a round
        ABANDONED_ROUND,    //the subscriber was refunded credits after abandoning a round (before they played it - unable to abandon after play begins)
        PAYOUT,             //the subscriber was awarded money into their account
        PAID,               //the subscriber withdrew funds and was paid real money
    }

    private long _cashpoolTransactionId;
    private int _subscriberId;
    private double _amount;
    private TYPE _type; //description -> type
    private String _description; //externalRefId -> description

    private double _currentPoolAmount; //how much is currently in the pool after this amount/type is taken into account
    private double _currentBonusAmount; //how much is currently in the bonus pool after this amount/type is taken into account
    private Double _usedPoolAmount; //if type == JOINED_ROUND, this holds how much of the money was deducted from the pool value
    private Double _usedBonusAmount; //if type == JOINED_ROUND, this holds how much of the money was deducted from the bonus value

    private Integer _receiptId;
    private String _contextUuid;
    private Date _transactionDate;

    private CashPoolTransaction2() {}

    public long getCashpoolTransactionId()
    {
        return _cashpoolTransactionId;
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }

    public double getAmount()
    {
        return _amount;
    }

    public TYPE getType()
    {
        return _type;
    }

    public String getDescription()
    {
        return _description;
    }

    public double getCurrentPoolAmount()
    {
        return _currentPoolAmount;
    }

    public double getCurrentBonusAmount()
    {
        return _currentBonusAmount;
    }

    public Double getUsedPoolAmount()
    {
        return _usedPoolAmount;
    }

    public Double getUsedBonusAmount()
    {
        return _usedBonusAmount;
    }

    public Integer getReceiptId()
    {
        return _receiptId;
    }

    public String getContextUuid()
    {
        return _contextUuid;
    }

    public Date getTransactionDate()
    {
        return _transactionDate;
    }

    public static CashPoolTransaction2 fromCashPoolTransaction(CashPoolTransaction2 previousCpt2, CashPoolTransaction cpt)
    {
        CashPoolTransaction2 cpt2 = new CashPoolTransaction2();

        //typo fix in existing data
        String typeS = cpt.getDescription();
        if (typeS.equals("PURHASE")) {
            typeS = "PURCHASE";
        }

        //several of the fields copy straight across
        cpt2._cashpoolTransactionId = cpt.getCashpoolTransactionId();
        cpt2._subscriberId = cpt.getSubscriberId();
        cpt2._amount = cpt.getAmount();
        cpt2._type = CashPoolTransaction2.TYPE.valueOf(typeS);
        cpt2._description = cpt.getExternalRefId();
        cpt2._receiptId = cpt.getReceiptId();
        cpt2._contextUuid = cpt.getContextUuid();
        cpt2._transactionDate = cpt.getTransactionDate();

        if (previousCpt2 == null) {
            previousCpt2 = new CashPoolTransaction2();
        }

        switch (cpt2._type)
        {
            case BONUS: {
                cpt2._currentPoolAmount = previousCpt2._currentPoolAmount;
                cpt2._currentBonusAmount = previousCpt2._currentBonusAmount + cpt2._amount;
            }
            break;

            case PURCHASE:
            case PAYOUT:
            case PAID:
                cpt2._currentPoolAmount = previousCpt2._currentPoolAmount + cpt2._amount;
                cpt2._currentBonusAmount = previousCpt2._currentBonusAmount;
            break;

            case JOINED_ROUND: {
                cpt2._currentPoolAmount = previousCpt2._currentPoolAmount;
                cpt2._currentBonusAmount = previousCpt2._currentBonusAmount;

                //deduct from bonus first if there's enough
                double totalToDeduct = -(cpt2._amount);
                double amountTakenFromBonus = 0D;
                double amountTakenFromPool = 0D;

                if (cpt2._currentBonusAmount > 0 && cpt2._currentBonusAmount >= totalToDeduct) {
                    //take full amount from bonus
                    cpt2._currentBonusAmount -= totalToDeduct;
                    amountTakenFromBonus = totalToDeduct;
                    totalToDeduct = 0D;

                } else if (cpt2._currentBonusAmount > 0) {
                    //take as much as possible from bonus
                    amountTakenFromBonus = cpt2._currentBonusAmount;
                    cpt2._currentBonusAmount = 0D;
                    totalToDeduct -= amountTakenFromBonus;
                }

                if (totalToDeduct > 0) {
                    //take remaining from pool
                    amountTakenFromPool = totalToDeduct;
                    cpt2._currentPoolAmount -= amountTakenFromPool;
                    //totalToDeduct = 0D;
                }

                cpt2._usedBonusAmount = amountTakenFromBonus;
                cpt2._usedPoolAmount = amountTakenFromPool;
            }
            break;

            case ABANDONED_ROUND: {
                cpt2._currentPoolAmount = previousCpt2._currentPoolAmount;
                cpt2._currentBonusAmount = previousCpt2._currentBonusAmount;

                cpt2._currentPoolAmount += (previousCpt2._usedPoolAmount == null ? 0D : previousCpt2._usedPoolAmount);
                cpt2._currentBonusAmount += (previousCpt2._usedBonusAmount == null ? 0D : previousCpt2._usedBonusAmount);
            }
            break;
        }

        return cpt2;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "{0,number,currency}\t\t{1}\t\t{2}{3,number,currency}\t\t{4,number,currency}\t{5,number,currency}\t{6}\t{7}",
            _amount, _type,
            _type == TYPE.JOINED_ROUND || _type == TYPE.ABANDONED_ROUND ? "" : "\t",
            _currentPoolAmount, _currentBonusAmount, _currentPoolAmount + _currentBonusAmount,
            _usedBonusAmount == null ? "-" : _usedBonusAmount, _usedPoolAmount == null ? "-" : _usedPoolAmount);
    }
}
