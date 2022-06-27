package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

public class ChampionsChallenge1On1Result implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Log logger = LogFactory.getLog(ChampionsChallenge1On1Result.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();

    public enum Result { WON_1ON1, LOST_1ON1, TIED_1ON1 }
    public enum Action { OFFERED_PRIZE, REDEEMED_PRIZE, ADVANCED_LEVEL }
    
    private int cc1on1Id;
    private int subscriberId;
    private int subscriberScore;
    private Result result;
    private Action wonAction;
    private Integer wonCouponInstanceId;
    private Integer nextLevelId;
    private Date resultDate;
    
    private Integer ccId;
    
    public int getCc1on1Id() {
        return cc1on1Id;
    }

    public void setCc1on1Id(int cc1on1Id) {
        this.cc1on1Id = cc1on1Id;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public int getSubscriberScore() {
        return subscriberScore;
    }

    public void setSubscriberScore(int subscriberScore) {
        this.subscriberScore = subscriberScore;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Action getWonAction() {
        return wonAction;
    }

    public void setWonAction(Action wonAction) {
        this.wonAction = wonAction;
    }

    public Integer getWonCouponInstanceId() {
        return wonCouponInstanceId;
    }

    public void setWonCouponInstanceId(Integer wonCouponInstanceId) {
        this.wonCouponInstanceId = wonCouponInstanceId;
    }

    public Integer getNextLevelId() {
        return nextLevelId;
    }

    public void setNextLevelId(Integer nextLevelId) {
        this.nextLevelId = nextLevelId;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }
    
    @Override
    public String toString() {
        try {
            return jsonMapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return String.format("{'cc_1on1_id'=%d, 'sub_id'=%d, 'score'=%d, 'result'='%s', 'wonAction'='%s'}".replaceAll("'","\""),
                                 cc1on1Id, subscriberId, subscriberScore, result, wonAction);
        }
    }

    public Integer getCcId() {
        return ccId;
    }

    public void setCcId(Integer ccId) {
        this.ccId = ccId;
    }
}
