package com.meinc.analytics;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AnalyticsPayload 
implements Serializable 
{
    private static final long serialVersionUID = 1L;

    public static final String KEY_ANALYTICS = "ANALYTICS";

    public static enum TYPE {
        EVENT_STARTED, EVENT_ENDED, EVENT_JOINED, EVENT_LEFT,
        QUESTION_STARTED, QUESTION_BATCH_STARTED, QUESTION_ENDED, QUESTION_SCORED, QUESTION_ANSWERED,
        VIPBOX_JOINED, VIPBOX_CREATED,
        SHOUTOUT_SENT,
        CONTEST_WON,
        SUBSCRIBER_SIGNED_UP,
        POWERUP_USED, CREDS_PURCHASED, CREDS_EXCHANGED, PASS_PURCHASED, PASS_TOPPED_OFF, CREDS_AWARDED,
        PUSH_SENDING, PUSH_SENT, PUSH_ERROR,
        POWERUP_AMOUNTS_PRE_TOPOFF, POWERUP_AMOUNTS_POST_TOPOFF
    }
    
    public static final String DEVICE_OSNAME_WEB = "web";
    
    //every payload has these
    private TYPE _type;
    private Date _timestamp;
    
    //depending on the type of payload, some of these will be set (use the static initializers) 
    private Integer _subscriberId;
    private Integer _eventId;
    private Integer _questionId;
    private String _questionIds;
    private Integer _answerId;
    private Integer _vipboxId;
    private Integer _ccId;
    private Integer _contestId;
    private String _powerupType; //maps to Powerup.POWERUP_TYPE
    private String _powerupPassType; //maps to PowerupPass.PASS_TYPE
    private Integer _powerupCredsAmount;
    private Integer _numClockFreeze;
    private Integer _numFanCheck;
    private Integer _numVote2;
    private Integer _numMulligan;
    private Integer _numThrowdown;
    private Integer _numSafetyNet;
    private String _deviceOsName;
    private String _subscriberEmail;
    private Integer _numCreds;
    private Integer _vttId;
    private String _pushProvider; //APNS, GCM
    private String _deviceId;
    private String _messageId;
    
    public AnalyticsPayload() { }

    private AnalyticsPayload(TYPE type)
    {
        _type = type;
        _timestamp = new Date();
    }
    
    public TYPE getType()
    {
        return _type;
    }

    public Date getTimestamp()
    {
        return _timestamp;
    }

    public Integer getSubscriberId()
    {
        return _subscriberId;
    }

    public Integer getEventId()
    {
        return _eventId;
    }

    public Integer getQuestionId()
    {
        return _questionId;
    }
    
    public String getQuestionIds()
    {
        return _questionIds;
    }

    public Integer getAnswerId()
    {
        return _answerId;
    }

    public Integer getVipboxId()
    {
        return _vipboxId;
    }

    public Integer getCcId()
    {
        return _ccId;
    }
    
    public Integer getContestId()
    {
        return _contestId;
    }

    public String getPowerupType()
    {
        return _powerupType;
    }

    public String getPowerupPassType()
    {
        return _powerupPassType;
    }

    public Integer getPowerupCredsAmount()
    {
        return _powerupCredsAmount;
    }

    public Integer getNumClockFreeze()
    {
        return _numClockFreeze;
    }

    public Integer getNumFanCheck()
    {
        return _numFanCheck;
    }

    public Integer getNumVote2()
    {
        return _numVote2;
    }

    public Integer getNumMulligan()
    {
        return _numMulligan;
    }

    public Integer getNumThrowdown()
    {
        return _numThrowdown;
    }

    public Integer getNumSafetyNet()
    {
        return _numSafetyNet;
    }
    
    public String getDeviceOsName()
    {
        return _deviceOsName;
    }

    public String getSubscriberEmail()
    {
        return _subscriberEmail;
    }
    
    public Integer getNumCreds()
    {
        return _numCreds;
    }
    
    public Integer getVttId()
    {
        return _vttId;
    }

    public String getPushProvider()
    {
        return _pushProvider;
    }

    public String getDeviceId()
    {
        return _deviceId;
    }

    public String getMessageId()
    {
        return _messageId;
    }

    public void setType(TYPE type) {
        _type = type;
    }

    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }

    public void setSubscriberId(Integer subscriberId) {
        _subscriberId = subscriberId;
    }

    public void setEventId(Integer eventId) {
        _eventId = eventId;
    }

    public void setQuestionId(Integer questionId) {
        _questionId = questionId;
    }

    public void setQuestionIds(String questionIds) {
        _questionIds = questionIds;
    }

    public void setAnswerId(Integer answerId) {
        _answerId = answerId;
    }

    public void setVipboxId(Integer vipboxId) {
        _vipboxId = vipboxId;
    }

    public void setCcId(Integer ccId) {
        _ccId = ccId;
    }

    public void setContestId(Integer contestId) {
        _contestId = contestId;
    }

    public void setPowerupType(String powerupType) {
        _powerupType = powerupType;
    }

    public void setPowerupPassType(String powerupPassType) {
        _powerupPassType = powerupPassType;
    }

    public void setPowerupCredsAmount(Integer powerupCredsAmount) {
        _powerupCredsAmount = powerupCredsAmount;
    }

    public void setNumClockFreeze(Integer numClockFreeze) {
        _numClockFreeze = numClockFreeze;
    }

    public void setNumFanCheck(Integer numFanCheck) {
        _numFanCheck = numFanCheck;
    }

    public void setNumVote2(Integer numVote2) {
        _numVote2 = numVote2;
    }

    public void setNumMulligan(Integer numMulligan) {
        _numMulligan = numMulligan;
    }

    public void setNumThrowdown(Integer numThrowdown) {
        _numThrowdown = numThrowdown;
    }

    public void setNumSafetyNet(Integer numSafetyNet) {
        _numSafetyNet = numSafetyNet;
    }

    public void setDeviceOsName(String deviceOsName) {
        _deviceOsName = deviceOsName;
    }

    public void setSubscriberEmail(String subscriberEmail) {
        _subscriberEmail = subscriberEmail;
    }

    public void setNumCreds(Integer numCreds) {
        _numCreds = numCreds;
    }

    public void setVttId(Integer vttId) {
        _vttId = vttId;
    }

    public void setPushProvider(String pushProvider) {
        _pushProvider = pushProvider;
    }

    public void setDeviceId(String deviceId) {
        _deviceId = deviceId;
    }

    public void setMessageId(String messageId) {
        _messageId = messageId;
    }

    public static AnalyticsPayload powerupAmountsPreTopoff(int subscriberId, int eventId, String passType, int numClockFreeze, int numFanCheck, int numVote2, int numMulligan, int numThrowdown, int numSafetyNet)
    {
        if (passType == null) throw new IllegalArgumentException("passType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.POWERUP_AMOUNTS_PRE_TOPOFF);
        ap._subscriberId = subscriberId;
        ap._eventId = eventId;
        ap._powerupPassType = passType;
        ap._numClockFreeze = numClockFreeze;
        ap._numFanCheck = numFanCheck;
        ap._numVote2 = numVote2;
        ap._numMulligan = numMulligan;
        ap._numThrowdown = numThrowdown;
        ap._numSafetyNet = numSafetyNet;
        return ap;
    }

    public static AnalyticsPayload powerupAmountsPostTopoff(int subscriberId, int eventId, String passType, int numClockFreeze, int numFanCheck, int numVote2, int numMulligan, int numThrowdown, int numSafetyNet)
    {
        if (passType == null) throw new IllegalArgumentException("passType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.POWERUP_AMOUNTS_POST_TOPOFF);
        ap._subscriberId = subscriberId;
        ap._eventId = eventId;
        ap._powerupPassType = passType;
        ap._numClockFreeze = numClockFreeze;
        ap._numFanCheck = numFanCheck;
        ap._numVote2 = numVote2;
        ap._numMulligan = numMulligan;
        ap._numThrowdown = numThrowdown;
        ap._numSafetyNet = numSafetyNet;
        return ap;
    }

    public static AnalyticsPayload passToppedOff(int subscriberId, int eventId, String passType, int numClockFreeze, int numFanCheck, int numVote2, int numMulligan, int numThrowdown, int numSafetyNet)
    {
        if (passType == null) throw new IllegalArgumentException("passType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.PASS_TOPPED_OFF);
        ap._subscriberId = subscriberId;
        ap._eventId = eventId;
        ap._powerupPassType = passType;
        ap._numClockFreeze = numClockFreeze;
        ap._numFanCheck = numFanCheck;
        ap._numVote2 = numVote2;
        ap._numMulligan = numMulligan;
        ap._numThrowdown = numThrowdown;
        ap._numSafetyNet = numSafetyNet;
        return ap;
    }

    public static AnalyticsPayload eventStarted(int eventId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.EVENT_STARTED);
        ap._eventId = eventId;
        return ap;
    }
    
    public static AnalyticsPayload eventEnded(int eventId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.EVENT_ENDED);
        ap._eventId = eventId;
        return ap;
    }
    
    public static AnalyticsPayload eventJoined(int eventId, int subscriberId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.EVENT_JOINED);
        ap._eventId = eventId;
        ap._subscriberId = subscriberId;
        return ap;
    }
    
    public static AnalyticsPayload eventLeft(int eventId, int subscriberId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.EVENT_LEFT);
        ap._eventId = eventId;
        ap._subscriberId = subscriberId;
        return ap;
    }
    
    public static AnalyticsPayload questionStarted(int eventId, int questionId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.QUESTION_STARTED);
        ap._eventId = eventId;
        ap._questionId = questionId;
        return ap;
    }
    
    public static AnalyticsPayload questionBatchStarted(int eventId, List<Integer> questionIds)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.QUESTION_BATCH_STARTED);
        ap._eventId = eventId;
        String qIds = "";
        boolean first = true;
        for (Integer questionId : questionIds)
        {
            if (first)
                first = true;
            else
                qIds += ",";
            
            qIds += questionId;
        }
        
        ap._questionIds = qIds;
        return ap;
    }
    
    public static AnalyticsPayload questionEnded(int eventId, int questionId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.QUESTION_ENDED);
        ap._eventId = eventId;
        ap._questionId = questionId;
        return ap;
    }
    
    public static AnalyticsPayload questionScored(int eventId, int questionId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.QUESTION_SCORED);
        ap._eventId = eventId;
        ap._questionId = questionId;
        return ap;
    }
    
    public static AnalyticsPayload questionAnswered(int eventId, int questionId, int subscriberId, int answerId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.QUESTION_ANSWERED);
        ap._eventId = eventId;
        ap._questionId = questionId;
        ap._subscriberId = subscriberId;
        ap._answerId = answerId;
        return ap;
    }
    
    public static AnalyticsPayload vipboxJoined(int vipboxId, int subscriberId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.VIPBOX_JOINED);
        ap._subscriberId = subscriberId;
        ap._vipboxId = vipboxId;
        return ap;
    }
    
    public static AnalyticsPayload vipboxCreated(int vipboxId, int subscriberId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.VIPBOX_CREATED);
        ap._subscriberId = subscriberId;
        ap._vipboxId = vipboxId;
        return ap;
    }
    
    public static AnalyticsPayload shoutoutSent(int eventId, int subscriberId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.SHOUTOUT_SENT);
        ap._eventId = eventId;
        ap._subscriberId = subscriberId;
        return ap;
    }
    
    public static AnalyticsPayload contestWon(int eventId, int subscriberId, Integer contestId, Integer questionId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.CONTEST_WON);
        ap._eventId = eventId;
        ap._questionId = questionId;
        ap._subscriberId = subscriberId;
        ap._contestId = contestId;
        return ap;
    }
    
    public static AnalyticsPayload subscriberSignedUp(int subscriberId, String deviceOsName, String subscriberEmail)
    {
        if (subscriberEmail == null) subscriberEmail = "";
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.SUBSCRIBER_SIGNED_UP);
        ap._subscriberId = subscriberId;
        ap._deviceOsName = deviceOsName == null ? DEVICE_OSNAME_WEB : deviceOsName;
        ap._subscriberEmail = subscriberEmail;
        return ap;
    }
    
    public static AnalyticsPayload powerupUsed(int subscriberId, Integer ccId, Integer eventId, Integer questionId, String powerupType)
    {
        if (powerupType == null) throw new IllegalArgumentException("powerupType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.POWERUP_USED);
        ap._subscriberId = subscriberId;
        ap._ccId = ccId;
        ap._eventId = eventId;
        ap._questionId = questionId;
        ap._powerupType = powerupType;
        return ap;
    }
    
    public static AnalyticsPayload credsPurchased(int subscriberId, int credsAmount)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.CREDS_PURCHASED);
        ap._subscriberId = subscriberId;
        ap._powerupCredsAmount = credsAmount;
        return ap;
    }
    
    public static AnalyticsPayload credsExchanged(int subscriberId, int credsAmount, String powerupType)
    {
        if (powerupType == null) throw new IllegalArgumentException("powerupType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.CREDS_EXCHANGED);
        ap._subscriberId = subscriberId;
        ap._powerupCredsAmount = credsAmount;
        ap._powerupType = powerupType;
        return ap;
    }
    
    public static AnalyticsPayload passPurchased(int subscriberId, String passType)
    {
        if (passType == null) throw new IllegalArgumentException("passType can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.PASS_PURCHASED);
        ap._subscriberId = subscriberId;
        ap._powerupPassType = passType;
        return ap;
    }
    
    public static AnalyticsPayload credsAwarded(int subscriberId, int numCreds, int vttId)
    {
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.CREDS_AWARDED);
        ap._subscriberId = subscriberId;
        ap._numCreds = numCreds;
        ap._vttId = vttId;
        return ap;
    }
    
    public static AnalyticsPayload pushSending(int subscriberId, String pushProvider)
    {
        if (pushProvider == null) throw new IllegalArgumentException("pushProvider can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.PUSH_SENDING);
        ap._subscriberId = subscriberId;
        ap._pushProvider = pushProvider;
        return ap;
    }
    
    public static AnalyticsPayload pushSent(String pushProvider, String deviceId, String messageId)
    {
        if (pushProvider == null) throw new IllegalArgumentException("pushProvider can not be null");
        if (deviceId == null) throw new IllegalArgumentException("deviceId can not be null");
        if (messageId == null) throw new IllegalArgumentException("messageId can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.PUSH_SENT);
        ap._pushProvider = pushProvider;
        ap._deviceId = deviceId;
        ap._messageId = messageId;
        return ap;
    }
    
    public static AnalyticsPayload pushError(String pushProvider, String deviceId, String messageId)
    {
        if (pushProvider == null) throw new IllegalArgumentException("pushProvider can not be null");
        if (deviceId == null) throw new IllegalArgumentException("deviceId can not be null");
        if (messageId == null) throw new IllegalArgumentException("messageId can not be null");
        AnalyticsPayload ap = new AnalyticsPayload(TYPE.PUSH_ERROR);
        ap._pushProvider = pushProvider;
        ap._deviceId = deviceId;
        ap._messageId = messageId;
        return ap;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(_type).append(" (").append(_timestamp).append(")");
        switch (_type)
        {
            case EVENT_STARTED:
            case EVENT_ENDED:
                buf.append(", eventId: ").append(_eventId);
                break;
            case EVENT_JOINED:
            case EVENT_LEFT:
            case SHOUTOUT_SENT:
                buf.append(", eventId: ").append(_eventId);
                buf.append(", subscriberId: ").append(_subscriberId);
                break;
            case QUESTION_STARTED:
            case QUESTION_ENDED:
            case QUESTION_SCORED:
                buf.append(", eventId: ").append(_eventId);
                buf.append(", questionId: ").append(_questionId);
                break;
            case QUESTION_ANSWERED:
                buf.append(", eventId: ").append(_eventId);
                buf.append(", questionId: ").append(_questionId);
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", answerId: ").append(_answerId);
                break;
            case VIPBOX_JOINED:
            case VIPBOX_CREATED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", vipboxId: ").append(_vipboxId);
                break;
            case CONTEST_WON:
                buf.append(", eventId: ").append(_eventId);
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", questionId: ").append(_questionId);
                buf.append(", contestId: ").append(_contestId);
                break;
            case SUBSCRIBER_SIGNED_UP:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", subscriberEmail: ").append(_subscriberEmail);
                buf.append(", deviceOsName: ").append(_deviceOsName);
                break;
            case POWERUP_USED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", ccId: ").append(_ccId);
                buf.append(", eventId: ").append(_eventId);
                buf.append(", questionId: ").append(_questionId);
                buf.append(", powerupType: ").append(_powerupType);
                break;
            case CREDS_PURCHASED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", powerupCredsAmount: ").append(_powerupCredsAmount);
                break;
            case CREDS_EXCHANGED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", powerupCredsAmount: ").append(_powerupCredsAmount);
                buf.append(", powerupType: ").append(_powerupType);
                break;
            case PASS_PURCHASED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", powerupPassType: ").append(_powerupPassType);
                break;
            case POWERUP_AMOUNTS_PRE_TOPOFF:
            case PASS_TOPPED_OFF:
            case POWERUP_AMOUNTS_POST_TOPOFF:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", eventId: ").append(_eventId);
                buf.append(", powerupPassType: ").append(_powerupPassType);
                buf.append(", numClockFreeze: ").append(_numClockFreeze);
                buf.append(", numFanCheck: ").append(_numFanCheck);
                buf.append(", numVote2: ").append(_numVote2);
                buf.append(", numMulligan: ").append(_numMulligan);
                buf.append(", numThrowdown: ").append(_numThrowdown);
                buf.append(", numSafetyNet: ").append(_numSafetyNet);
                break;
            case CREDS_AWARDED:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", numCreds: ").append(_numCreds);
                buf.append(", vttId: ").append(_vttId);
                break;
            case PUSH_SENDING:
                buf.append(", subscriberId: ").append(_subscriberId);
                buf.append(", pushProvider: ").append(_pushProvider);
                break;
            case PUSH_SENT:
            case PUSH_ERROR:
                buf.append(", pushProvider: ").append(_pushProvider);
                buf.append(", deviceId: ").append(_deviceId);
                buf.append(", messageId: ").append(_messageId);
                break;
            case QUESTION_BATCH_STARTED:
                buf.append(", eventId: ").append(_eventId);
                buf.append(", questionIds: ").append(_questionIds);
                break;
        }

        return buf.toString();
    }
}
