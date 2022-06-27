package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.List;

public class Account 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum PROVIDER {ERGO, EXCHANGE, GOOGLE, ICLOUD};
    public static enum TYPE {STANDARD, PREMIUM};
    public static enum BILLING_PERIOD {MONTHLY, ANNUALLY};
    public static enum STATUS {VALID, PASTDUE, SUSPENDED, CANCELLED};
    
    private PROVIDER provider;
    private TYPE type;
    private BILLING_PERIOD billingPeriod;
    private STATUS status;
    private List<Receipt> receipts;
    
    public TYPE getType()
    {
        return type;
    }
    public void setType(TYPE type)
    {
        this.type = type;
    }
    public BILLING_PERIOD getBillingPeriod()
    {
        return billingPeriod;
    }
    public void setBillingPeriod(BILLING_PERIOD billingPeriod)
    {
        this.billingPeriod = billingPeriod;
    }
    public STATUS getStatus()
    {
        return status;
    }
    public void setStatus(STATUS status)
    {
        this.status = status;
    }
    public List<Receipt> getReceipts()
    {
        return receipts;
    }
    public void setReceipts(List<Receipt> receipts)
    {
        this.receipts = receipts;
    }
    public PROVIDER getProvider()
    {
        return provider;
    }
    public void setProvider(PROVIDER provider)
    {
        this.provider = provider;
    }
}
