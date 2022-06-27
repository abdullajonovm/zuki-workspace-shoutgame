package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.Date;

public class WorkbenchCode implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1918301054373123012L;
    private Integer _workbenchCodeId;
    private Integer _workbenchDealId;
    private String _code;
    private Date _createDate;
    private Date _lastUpdated;

    public WorkbenchCode clone(String nameSuffix)
    {
        WorkbenchCode clone = new WorkbenchCode();
        clone.setWorkbenchDealId(_workbenchDealId);
        clone.setCode(_code);
        clone.setCreateDate(_createDate);
        clone.setLastUpdated(_lastUpdated);
        return clone;
    }

    public Integer getWorkbenchCodeId()
    {
        return _workbenchCodeId;
    }

    public void setWorkbenchCodeId(Integer workbenchCodeId)
    {
        this._workbenchCodeId = workbenchCodeId;
    }
    
    public Integer getWorkbenchDealId()
    {
        return _workbenchDealId;
    }

    public void setWorkbenchDealId(Integer workbenchDealId)
    {
        this._workbenchDealId = workbenchDealId;
    }
    
    public String getCode()
    {
        return _code;
    }
    
    public void setCode(String code)
    {
        _code = code;
    }
    
    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this._createDate = createDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        this._lastUpdated = lastUpdated;
    }

}