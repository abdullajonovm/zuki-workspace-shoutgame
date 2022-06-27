package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.Date;

public class PromoBatch 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static enum STATUS {NEW, ACTIVE, DISABLED, COMPLETE}
    public static enum TYPE {PRO, PREMIUM}

    private int batchId;
    private String name;
    private Date dateCreated;
    private int creatorId;
    private Date lastUpdate;
    private STATUS status;
    private TYPE type;
    private int numCodes;
    private Date unclaimedExpireDate;
    
    public int getBatchId()
    {
        return batchId;
    }
    public void setBatchId(int batchId)
    {
        this.batchId = batchId;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public Date getDateCreated()
    {
        return dateCreated;
    }
    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated;
    }
    public int getCreatorId()
    {
        return creatorId;
    }
    public void setCreatorId(int creatorId)
    {
        this.creatorId = creatorId;
    }
    public Date getLastUpdate()
    {
        return lastUpdate;
    }
    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }
    public STATUS getStatus()
    {
        return status;
    }
    public void setStatus(STATUS status)
    {
        this.status = status;
    }
    public TYPE getType()
    {
        return type;
    }
    public void setType(TYPE type)
    {
        this.type = type;
    }
    public int getNumCodes()
    {
        return numCodes;
    }
    public void setNumCodes(int numCodes)
    {
        this.numCodes = numCodes;
    }
    public Date getUnclaimedExpireDate()
    {
        return unclaimedExpireDate;
    }
    public void setUnclaimedExpireDate(Date unclaimedExpireDate)
    {
        this.unclaimedExpireDate = unclaimedExpireDate;
    }
    
}
