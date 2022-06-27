package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

public class SubscriberAnswer 
implements Serializable
{
    private static final long serialVersionUID = 4229585329432218084L;

    private int subscriberAnswerId;
    private int subscriberId;
    private int eventId;
    private int questionId;
    private int answerId;
    private Integer powerupUsageId;
    private String powerupType;
    private int relativeAnswerTimeMs;
    private Double latitude;
    private Double longitude;
    private Integer geoErrorMargin;
    private String vipboxIds; //comma-delimited list
    private Date receivedDate;
    private Short receivedDateMs;
    private Date createDate;
    private Date updateDate;
    private Date deleteDate;
    private String deviceId; //which device was used to answer the question
    
    public SubscriberAnswer() {
        
    }
    
    public SubscriberAnswer(
        int subscriberId, int eventId, int questionId, int answerId, Integer powerupUsageId, int relativeAnswerTimeMs, 
        Double latitude, Double longitude, Integer geoErrorMargin,
        String vipboxIds, Date receivedDate, Date createDate, Date updateDate, Date deleteDate)
    {
        this.subscriberId = subscriberId;
        this.eventId = eventId;
        this.questionId = questionId;
        this.answerId = answerId;
        this.powerupUsageId = powerupUsageId;
        this.relativeAnswerTimeMs = relativeAnswerTimeMs;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geoErrorMargin = geoErrorMargin;
        this.vipboxIds = vipboxIds;
        this.receivedDate = receivedDate;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.deleteDate = deleteDate;
    }


    public SubscriberAnswer(int subscriberId, Double latitude, Double longitude, Integer geoErrorMargin)
    {
        this.subscriberId = subscriberId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geoErrorMargin = geoErrorMargin;
    }

    public int getSubscriberAnswerId() {
        return subscriberAnswerId;
    }

    public void setSubscriberAnswerId(int subscriberAnswerId) {
        this.subscriberAnswerId = subscriberAnswerId;
    }

    public int getSubscriberId()
    {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId)
    {
        this.subscriberId = subscriberId;
    }

    public int getEventId()
    {
        return eventId;
    }

    public void setEventId(int eventId)
    {
        this.eventId = eventId;
    }

    public int getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(int questionId)
    {
        this.questionId = questionId;
    }

    public Integer getPowerupUsageId() {
        return powerupUsageId;
    }

    public void setPowerupUsageId(Integer powerupUsageId) {
        this.powerupUsageId = powerupUsageId;
    }

    public String getPowerupType() {
        return powerupType;
    }

    public void setPowerupType(String powerupType) {
        this.powerupType = powerupType;
    }

    public int getAnswerId()
    {
        return answerId;
    }

    public void setAnswerId(int answerId)
    {
        this.answerId = answerId;
    }

    public int getRelativeAnswerTimeMs()
    {
        return relativeAnswerTimeMs;
    }

    public void setRelativeAnswerTimeMs(int relativeAnswerTimeMs)
    {
        this.relativeAnswerTimeMs = relativeAnswerTimeMs;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public Integer getGeoErrorMargin()
    {
        return geoErrorMargin;
    }

    public void setGeoErrorMargin(Integer geoErrorMargin)
    {
        this.geoErrorMargin = geoErrorMargin;
    }

    public String getVipboxIds()
    {
        return vipboxIds;
    }

    public void setVipboxIds(String vipboxIds)
    {
        this.vipboxIds = vipboxIds;
    }

    public Date getReceivedDate()
    {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate)
    {
        this.receivedDate = receivedDate;
    }
    
    public short getReceivedDateMs() {
        if (receivedDateMs != null)
            return receivedDateMs;
        if (receivedDate == null)
            return 0;
        return (short) (receivedDate.getTime() % 1000);
    }
    
    public void setReceivedDateMs(short receivedDateMs) {
        this.receivedDateMs = receivedDateMs;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return String.format("Subscriber %d voted for answer %d on question %d of event %d with powerup_usage %d of type %s",
                             getSubscriberId(), getAnswerId(), getQuestionId(), getEventId(), getPowerupUsageId(), getPowerupType());
    }
}
