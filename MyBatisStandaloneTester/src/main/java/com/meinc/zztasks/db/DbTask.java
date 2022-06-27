package com.meinc.zztasks.db;

import java.util.Date;

import com.meinc.zztasks.domain.Task;

public class DbTask
extends Task
{
    private static final long serialVersionUID = 1L;

    private String _dbStatus;
    private String _dbRecurringRRule;
    private Date _dbRecurringStartDate;
    private Boolean _dbRecurringRegenerativeFlag;

    public String getDbStatus()
    {
        return _dbStatus;
    }
    public void setDbStatus(String dbStatus)
    {
        _dbStatus = dbStatus;
    }
    public String getDbRecurringRRule()
    {
        return _dbRecurringRRule;
    }
    public void setDbRecurringRRule(String dbRecurringRRule)
    {
        _dbRecurringRRule = dbRecurringRRule;
    }
    public Date getDbRecurringStartDate()
    {
        return _dbRecurringStartDate;
    }
    public void setDbRecurringStartDate(Date dbRecurringStartDate)
    {
        _dbRecurringStartDate = dbRecurringStartDate;
    }
    public Boolean getDbRecurringRegenerativeFlag()
    {
        return _dbRecurringRegenerativeFlag;
    }
    public void setDbRecurringRegenerativeFlag(Boolean dbRecurringRegenerativeFlag)
    {
        _dbRecurringRegenerativeFlag = dbRecurringRegenerativeFlag;
    }
}
