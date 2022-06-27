package com.meinc.ergo.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.meinc.ergo.util.JsonDateSerializer;

public class Subscriber
extends BaseDomainObject
{
    private static final long serialVersionUID = 1L;

    public static enum PAYMENT_STATE {NONE, ACTIVE, EXPIRED}
    public static enum STATE {FREE, TRIAL, PRO, PREMIUM}

    private String email;
    private PAYMENT_STATE paymentState = PAYMENT_STATE.NONE;
    private STATE state = STATE.FREE;
    private Date stateExpirationDate;
    private String fbId;
    private String timezone;
    private boolean featureTester;
    private String affiliate;

    @Override
    @JsonProperty("subscriberId")
    public String getUuid()
    {
        return uuid;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @JsonProperty("payment_status")
    public PAYMENT_STATE getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(PAYMENT_STATE paymentState) {
        this.paymentState = paymentState;
    }

    public STATE getState()
    {
        return state;
    }

    @JsonProperty("account_level")
    public void setState(STATE state)
    {
        this.state = state;
    }

    @JsonProperty("account_level_expiration_date")
    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getStateExpirationDate() {
        return stateExpirationDate;
    }

    public void setStateExpirationDate(Date stateExpirationDate) {
        this.stateExpirationDate = stateExpirationDate;
    }

    public String getFbId()
    {
        return fbId;
    }

    public void setFbId(String fbId)
    {
        this.fbId = fbId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isFeatureTester()
    {
        return featureTester;
    }

    public void setFeatureTester(boolean featureTester)
    {
        this.featureTester = featureTester;
    }

    public String getAffiliate()
    {
        return affiliate;
    }

    public void setAffiliate(String affiliate)
    {
        this.affiliate = affiliate;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("subscriberId: ").append(getId());
        buf.append(", uuid: ").append(uuid);
        buf.append(", email: ").append(email);
        buf.append(", state: ").append(state);
        buf.append(", paymentState: ").append(paymentState);
        buf.append(", stateExpire: ").append(stateExpirationDate);
        buf.append(", fbId: ").append(fbId);
        buf.append(", tz: ").append(timezone);
        buf.append(", featureTester: ").append(featureTester);
        buf.append(", affiliate: ").append(affiliate);
        buf.append(super.toString());

        return buf.toString();
    }

}
