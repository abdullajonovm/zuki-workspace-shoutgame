package com.meinc.store.exception;

public class CreditCardPaymentFailedException extends Exception
{
    private static final long serialVersionUID = -17994396299997150L;

    public enum PaymentFailedReason
    {
        GENERAL, // Don't know why, just did
        TRANSACTION_DECLINED, // The bank declined the transaction, show message like "Your transaction was declined."
        TRANSACTION_PENDING_VOICE_AUTH, // User has to call in to complete transaction.  Show something like, "Your Transaction is pending. Contact Customer Service to complete your order."
        INVALID_CC_INFO, // Issue with credit card number or expiration date or cvv2, show something like, "Invalid credit card information. Please re-enter."
        FRAUD_REJECTED, // Fraud response, show something like "Your Transactions has been declined. Contact Customer Service."
        FRAUD_INVALID_ZIP, // Fraud response, tell them "Your billing information does not match. Please re-enter." and if fail three times then don't let them continue trying to scam a zip code
        FRAUD_RESPONSE, // Fraud response, tell them "Your Transaction is Under Review. We will notify you via e-mail if accepted."
        UNSUPPORTED_CC,      // The credit card isn't supported
        COMMUNICATION_ERROR  // Unable to communicate with paypal server
    }

    private PaymentFailedReason _reason = PaymentFailedReason.GENERAL;
    private String _internalReasonMessage;

    public CreditCardPaymentFailedException(PaymentFailedReason reason)
    {
        super("Credit card payment failed for reason: " + reason);
        _reason = reason;
    }

    public PaymentFailedReason getReason()
    {
        return _reason;
    }

    public void setReason(PaymentFailedReason reason)
    {
        _reason = reason;
    }

    public String getInternalReasonMessage()
    {
        return _internalReasonMessage;
    }

    public void setInternalReasonMessage(String internalReasonMessage)
    {
        _internalReasonMessage = internalReasonMessage;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{reason: ").append(_reason).append(", internalReasonMessage").append(_internalReasonMessage).append("}");

        return sb.toString();
    }
}
