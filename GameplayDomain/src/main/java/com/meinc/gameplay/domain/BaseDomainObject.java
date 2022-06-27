package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Base domain object for all new tables that include the
 * "fab 4."
 * 
 * @author bxgrant
 */
public class BaseDomainObject implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Id of subscriber who created this object. */
    private int _creatorId;

    /** Date created in persistence layer.  */
    private Date _createDate;

    /** Id of subscrber who last updated this object. */
    private int _updatorId;
   
    /** Last update of the object. */
    private Date _lastUpdate;

    public BaseDomainObject()
    {
    }

    public int getCreatorId()
    {
        return _creatorId;
    }

    public void setCreatorId(int creatorId)
    {
        _creatorId = creatorId;
    }

    public Date getCreateDate()
    {
        return _createDate;
    }

    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }

    public int getUpdatorId()
    {
        return _updatorId;
    }

    public void setUpdatorId(int updatorId)
    {
        _updatorId = updatorId;
    }

    public Date getLastUpdate()
    {
        return _lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate)
    {
        _lastUpdate = lastUpdate;
    }
}
