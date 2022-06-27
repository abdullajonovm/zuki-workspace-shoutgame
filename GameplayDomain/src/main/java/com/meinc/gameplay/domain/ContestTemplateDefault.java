package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

import com.meinc.gameplay.domain.Contest.CONTEST_TYPE;
import com.meinc.gameplay.domain.Contest.CONTEST_VIPBOX_TYPE;

public class ContestTemplateDefault implements Serializable
{
    private static final long serialVersionUID = -1673630524502450318L;
    
    private int _contestTemplateDefaultId;
    private int _contestTemplateId;
    private CONTEST_TYPE _type;
    private CONTEST_VIPBOX_TYPE _vipBoxType;
    private Integer _primaryRefId;
    private Integer _vipBoxId;
    private Date _createdDate;
    private Date _lastUpdated;

    public ContestTemplateDefault()
    {
    }

    public ContestTemplateDefault(int contestTemplateId, CONTEST_TYPE type, CONTEST_VIPBOX_TYPE vipBoxType, 
                                  Integer primaryRefId, Integer vipBoxId)
    {
        _contestTemplateId = contestTemplateId;
        _type = type;
        _vipBoxType = vipBoxType;
        _primaryRefId = primaryRefId;
        _vipBoxId = vipBoxId;
    }

    public int getContestTemplateDefaultId()
    {
        return _contestTemplateDefaultId;
    }

    public void setContestTemplateDefaultId(int contestTemplateDefaultId)
    {
        _contestTemplateDefaultId = contestTemplateDefaultId;
    }
    
    public int getContestTemplateId()
    {
        return _contestTemplateId;
    }

    public void setContestTemplateId(int contestTemplateId)
    {
        _contestTemplateId = contestTemplateId;
    }

    public CONTEST_TYPE getType()
    {
        return _type;
    }

    public void setType(CONTEST_TYPE type)
    {
        _type = type;
    }

    public CONTEST_VIPBOX_TYPE getVipBoxType()
    {
        return _vipBoxType;
    }

    public void setVipBoxType(CONTEST_VIPBOX_TYPE vipBoxType)
    {
        _vipBoxType = vipBoxType;
    }
    
    public Integer getPrimaryRefId()
    {
        return _primaryRefId;
    }

    public void setPrimaryRefId(Integer primaryRefId)
    {
        _primaryRefId = primaryRefId;
    }

    public Integer getVipBoxId()
    {
        return _vipBoxId;
    }

    public void setVipBoxId(Integer vipBoxId)
    {
        _vipBoxId = vipBoxId;
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
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("Contest{contestTemplateDefaultId:").append(_contestTemplateDefaultId).append(", contestTemplateId: ").append(_contestTemplateId);
        sb.append(", name: ").append(", type: ").append(_type).append(", vipboxType: ").append(_vipBoxType).append(", primaryRefId: ").append(_primaryRefId);
        sb.append(", vipBoxId: ").append(_vipBoxId).append("}");
        
        return sb.toString();
    }
}
