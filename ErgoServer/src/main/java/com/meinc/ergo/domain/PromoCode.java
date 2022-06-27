package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.Date;

public class PromoCode 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final char[] CHARS = new char[]{'A','B','C','D','E','F','G','H','J','K','M','N','P','Q','R','S','T','U','V','W','X','Y','Z','2','3','4','5','6','7','8','9'};
    public static enum STATUS {NEW, ASSIGNED, DISABLED}
    
    private int codeId;
    private String code;
    private int batchId;
    private STATUS status;
    private Integer subscriberId;
    private Date dateUsed;
    private Integer receiptId;
    
    public int getCodeId()
    {
        return codeId;
    }

    public void setCodeId(int codeId)
    {
        this.codeId = codeId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public int getBatchId()
    {
        return batchId;
    }

    public void setBatchId(int batchId)
    {
        this.batchId = batchId;
    }

    public STATUS getStatus()
    {
        return status;
    }

    public void setStatus(STATUS status)
    {
        this.status = status;
    }

    public Integer getSubscriberId()
    {
        return subscriberId;
    }

    public void setSubscriberId(Integer subscriberId)
    {
        this.subscriberId = subscriberId;
    }

    public Date getDateUsed()
    {
        return dateUsed;
    }

    public void setDateUsed(Date dateUsed)
    {
        this.dateUsed = dateUsed;
    }

    public Integer getReceiptId()
    {
        return receiptId;
    }

    public void setReceiptId(Integer receiptId)
    {
        this.receiptId = receiptId;
    }

    public static String generateCode()
    {
        StringBuilder buf = new StringBuilder();
        
        for (int i=0; i<16; i++) {
            buf.append(CHARS[(int)(Math.random() * CHARS.length)]);
        }
        
        return buf.toString();
    }

}
