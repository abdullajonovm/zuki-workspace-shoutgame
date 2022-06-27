package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ChampionsChallenge 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1000;
    public static enum STATUS {DRAFT, ACTIVE, COMPLETE}
    
    private Integer ccId;
    private String name;
    private String nameUuid;
    private List<Localized> nameLocalized;
    private STATUS status = STATUS.DRAFT; //clients will only be given PUBLISHED CC's
    private Boolean privateCc;
    private String logoSmall;
    private String logoLarge;
    private Date startDate;               //redundant from events[0] but here for ease of client use
    private Date endDate;                 //redundant from events[n] but here for ease of client use
    private List<Event> events;
    private List<ChampionsChallengeLevel> levels;
    private int appId = 1; //hardcoded default (for now) of 1 (SHOUT)
    private Date createdDate;
    private Date updatedDate;
    
    public Integer getCcId() {
        return ccId;
    }

    public void setCcId(Integer ccId) {
        this.ccId = ccId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameUuid()
    {
        return nameUuid;
    }

    public void setNameUuid(String nameUuid)
    {
        this.nameUuid = nameUuid;
    }

    public List<Localized> getNameLocalized()
    {
        return nameLocalized;
    }

    public void setNameLocalized(List<Localized> nameLocalized)
    {
        this.nameLocalized = nameLocalized;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
    
    public Boolean getPrivateCc()
    {
        return privateCc;
    }

    public void setPrivateCc(Boolean privateCc)
    {
        this.privateCc = privateCc;
    }

    public String getLogoSmall() {
        return logoSmall;
    }

    public void setLogoSmall(String logoSmall) {
        this.logoSmall = logoSmall;
    }

    public String getLogoLarge() {
        return logoLarge;
    }

    public void setLogoLarge(String logoLarge) {
        this.logoLarge = logoLarge;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<ChampionsChallengeLevel> getLevels() {
        return levels;
    }

    public void setLevels(List<ChampionsChallengeLevel> levels) {
        this.levels = levels;
    }

    public int getAppId()
    {
        return appId;
    }

    public void setAppId(int appId)
    {
        this.appId = appId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createDate) {
        this.createdDate = createDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("id: ").append(ccId);
        buf.append(", name: ").append(name);
        buf.append(", status: ").append(status);
        buf.append(", privateCc: ").append(privateCc);
        buf.append(", #events: ").append(events == null ? "null" : events.size());
        buf.append(", #levels: ").append(levels == null ? "null" : levels.size());
        
        return buf.toString();
    }
}
