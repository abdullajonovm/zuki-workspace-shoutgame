package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;

public class Payout
implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** what type of payout is this. example may include "CASH", "MOTORCYCLE", or "CREDS". It is up to the user of this class to assign and understand the type */
    private String _payoutType;

    /** how many of the type there will be. for example, if type is "CASH", amount would be how much, such as 100 (meaning, $100) */
    private float _payoutAmount;

    public Payout() {}

    public Payout(String type, float amount)
    {
        _payoutType = type;
        _payoutAmount = amount;
    }

    public String getPayoutType()
    {
        return _payoutType;
    }

    public void setPayoutType(String payoutType)
    {
        _payoutType = payoutType;
    }

    public float getPayoutAmount()
    {
        return _payoutAmount;
    }

    public void setPayoutAmount(float payoutAmount)
    {
        _payoutAmount = payoutAmount;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("{0},{1}", _payoutType, _payoutAmount);
    }
}
