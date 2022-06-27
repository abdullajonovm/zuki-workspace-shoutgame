package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.meinc.ergo.util.JsonDateSerializer;

public abstract class BaseDomainObject
implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private int id;

    @JsonIgnore
    protected String uuid;

    private Date createDate;
    private Date lastUpdate;

    @JsonIgnore
    private Date deleteDate;

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public abstract String getUuid(); //made abstract so subclasses can override the jsonized name
    public void setUuid(String id) {this.uuid = id;}

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date date) { createDate = date; }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Date date) { lastUpdate = date; }

    public Date getDeleteDate() { return deleteDate; }
    public void setDeleteDate(Date date) { deleteDate = date; }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(", createDate: ").append(createDate);
        buf.append(", lastUpdate: ").append(lastUpdate);
        buf.append(", deleteDate: ").append(deleteDate);

        return buf.toString();
    }

}
