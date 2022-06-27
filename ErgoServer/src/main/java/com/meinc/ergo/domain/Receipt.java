package com.meinc.ergo.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.meinc.ergo.util.JsonDateSerializer;

public class Receipt
extends BaseDomainObject
{
    private static final long serialVersionUID = 1L;

    private String billedToName;
    private int ccLastFour;
    private String total; //The total of the receipt including currency symbol.
    private List<ReceiptItem> items;
    private Date transactionDate;

    @JsonProperty("receiptId")
    @Override
    public String getUuid()
    {
        return uuid;
    }

    public String getBilledToName()
    {
        return billedToName;
    }
    public void setBilledToName(String billedToName)
    {
        this.billedToName = billedToName;
    }
    public int getCcLastFour()
    {
        return ccLastFour;
    }
    public void setCcLastFour(int ccLastFour)
    {
        this.ccLastFour = ccLastFour;
    }
    public String getTotal()
    {
        return total;
    }
    public void setTotal(String total)
    {
        this.total = total;
    }
    public List<ReceiptItem> getItems()
    {
        return items;
    }
    public void setItems(List<ReceiptItem> items)
    {
        this.items = items;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getTransactionDate()
    {
        return transactionDate;
    }
    public void setTransactionDate(Date transactionDate)
    {
        this.transactionDate = transactionDate;
    }
}
