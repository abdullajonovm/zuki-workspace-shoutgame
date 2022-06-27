package com.meinc.gameplay.media.domain;

import java.io.Serializable;
import java.util.Date;

public class Tag implements Serializable
{
    private static final long serialVersionUID = -6252758563041125718L;

    public static enum TAG_TYPE {
        MEDIA, SANDBOX_QUESTION, SHOUT_TRENDING_QUESTION
    }
    
    private int _tagId;
    
    private TAG_TYPE _tagType = TAG_TYPE.MEDIA; //default is MEDIA for backwards compatibility
    
    /** the context will depend on the type. it might be null (MEDIA, SANDBOX_QUESTION), or it might be a subscriberId (SHOUT_TRENDING_QUESTION) */
    private Integer _tagContextId;
    
    private String _tag;
    
    private Date _createdDate;
    private Date _lastUpdated;
    
    public Tag()
    {
    }

    public Tag(String tag)
    {
        _tag = tag;
    }

    public int getTagId()
    {
        return _tagId;
    }

    public void setTagId(int tagId)
    {
        _tagId = tagId;
    }

    public TAG_TYPE getTagType()
    {
        return _tagType;
    }

    public void setTagType(TAG_TYPE tagType)
    {
        _tagType = tagType;
    }

    public Integer getTagContextId()
    {
        return _tagContextId;
    }

    public void setTagContextId(Integer tagContextId)
    {
        _tagContextId = tagContextId;
    }

    public String getTag()
    {
        return _tag;
    }

    public void setTag(String tag)
    {
        _tag = tag;
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
