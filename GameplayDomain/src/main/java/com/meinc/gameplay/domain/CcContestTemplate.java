package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Specifies a contest template intended for use on the events/celeb vipboxes of a a CC.
 */
public class CcContestTemplate implements Serializable
{
    private static final long serialVersionUID = 4789639015967895757L;
    
    private Integer _ccContestTemplateId;
    private Integer _ccId;
    private Integer _contestTemplateId;
    private Date _createdDate;
    private Date _lastUpdated;
    
    public CcContestTemplate()
    {
    }

    public Integer getCcContestTemplateId()
    {
        return _ccContestTemplateId;
    }

    public void setCcContestTemplateId(Integer ccContestTemplateId)
    {
        _ccContestTemplateId = ccContestTemplateId;
    }

    public Integer getCcId()
    {
        return _ccId;
    }

    public void setCcId(Integer ccId)
    {
        _ccId = ccId;
    }

    public Integer getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(Integer contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
    }

    public Date getCreatedDate()
    {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        _createdDate = createdDate;
    }

    public Date getLastUpdated()
    {
        return _lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        _lastUpdated = lastUpdated;
    }
}
