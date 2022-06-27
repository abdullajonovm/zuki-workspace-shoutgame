package com.meinc.store.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a 3rd party view of a customer. Allows our system to interface with theirs.
 */
public class CustomerProfile
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long _subscriberId;

    /** the 3rd parties customer id that maps to our subscriber id */
    private String _customerId;

    /**
     * The list of credit cards the 3rd party has on file for this subscriber. Note that this instance of the CreditCardInfo
     * object will only contain partial information (such as the last part of the card#, possibly the card type, and the expiration date).
     */
    private List<CreditCardInfo> _creditCardsOnFile;

    public long getSubscriberId()
    {
        return _subscriberId;
    }

    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getCustomerId()
    {
        return _customerId;
    }

    public void setCustomerId(String customerId)
    {
        _customerId = customerId;
    }

    public List<CreditCardInfo> getCreditCardsOnFile()
    {
        return _creditCardsOnFile;
    }

    public void setCreditCardsOnFile(List<CreditCardInfo> creditCardsOnFile)
    {
        _creditCardsOnFile = creditCardsOnFile;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("sId: ").append(_subscriberId);
        buf.append(", customerId: ").append(_customerId);
        if (_creditCardsOnFile != null) {
            buf.append(", CreditCards:");
            _creditCardsOnFile.forEach(cc -> {
                buf.append("\n\ttype: ").append(cc.getCardType());
                buf.append(", card#: ").append(cc.getNumber());
                buf.append(", expDate: ").append(cc.getExpDate());
                buf.append(", externalRefId: ").append(cc.getExternalRefId());
            });
        }

        return buf.toString();
    }
}
