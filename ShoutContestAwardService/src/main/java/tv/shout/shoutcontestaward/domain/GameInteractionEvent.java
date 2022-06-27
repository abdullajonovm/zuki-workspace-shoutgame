package tv.shout.shoutcontestaward.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameInteractionEvent 
{
	public static interface IXPAndPointsPersister {
        void giveXpAndPoints(int subscriberId, int xp, Integer contextId, int points, String xpType);
    }
    
    //private static Logger logger = Logger.getLogger(GameInteractionEvent.class);
    
    public static enum TARGET_TYPE {
        GAME, ROUND, QUESTION, PLAYER, ALLPLAYERS, SYSTEM
    }

    private Integer gameInteractionEventId;
    private Integer contextId;
    private String  eventTypeKey;
    private String  gameId;
    private String  roundId;
    private String  levelId;
    private String  levelValue;
    private Integer subscriberId;
    private Integer associationId;
    private String  associationDescription;
    private TARGET_TYPE targetType;
    private Integer pointsValue;
    private String  pointsSource;
    private Double  purchaseAmount;    
    private Double  awardAmount;
    private String  receivedPayload;
    private String  deliveredPayload;
    private boolean isNotification;
    private boolean isPersisted;
    private boolean isBadge;
    private boolean isQuestionWon;
    private boolean isQuestionLost;
    private boolean isRoundWon;
    private boolean isRoundLost;
    private Date createdDate;
        
    protected static ObjectMapper jsonMapper = new ObjectMapper();
    
    public GameInteractionEvent(){
    }
    
    public Integer getGameInteractionEventId() {
        return gameInteractionEventId;
    }
    public void setGameInteractionEventId(Integer gameInteractionEventId) {
        this.gameInteractionEventId = gameInteractionEventId;
    }
    public Integer getContextId() {
        return contextId;
    }
    public void setContextId(Integer contextId) {
        this.contextId = contextId;
    }    
    public String getEventTypeKey() {
        return eventTypeKey;
    }
    public void setEventTypeKey(String eventTypeKey) {
        this.eventTypeKey = eventTypeKey;
    }
    public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String getRoundId() {
		return roundId;
	}
	public void setRoundId(String roundId) {
		this.roundId = roundId;
	}
	public String getLevelId() {
		return levelId;
	}
	public void setLevelId(String levelId) {
		this.levelId = levelId;
	}
	public String getLevelValue() {
		return levelValue;
	}
	public void setLevelValue(String levelValue) {
		this.levelValue = levelValue;
	}
    public Integer getSubscriberId() {
        return subscriberId;
    }
    public void setSubscriberId(Integer value) {
        this.subscriberId = value;
    }
    public Integer getAssociationId() {
        return associationId;
    }
    public void setAssociationId(Integer value) {
        this.associationId = value;
    }
	public String getAssociationDescription() {
		return associationDescription;
	}
	public void setAssociationDescription(String associationDescription) {
		this.associationDescription = associationDescription;
	}
    public TARGET_TYPE getTargetType() {
        return targetType;
    }
    public void setTargetType(TARGET_TYPE targetType) {
        this.targetType = targetType;
    }
    public Integer getPointsValue() {
        return pointsValue;
    }
    public void setPointsValue(Integer pointsValue) {
        this.pointsValue = pointsValue;
    }
    public String getPointsSource() {
        return pointsSource;
    }
    public void setPointsSource(String pointsSource) {
        this.pointsSource = pointsSource;
    }
    public String getReceivedPayload() {
        return receivedPayload;
    }
    public void setReceivedPayload(String receivedPayload) {
        this.receivedPayload = receivedPayload;
    }
    public String getDeliveredPayload() {
        return deliveredPayload;
    }
    public void setDeliveredPayload(String deliveredPayload) {
        this.deliveredPayload = deliveredPayload;
    }
    public Double getPurchaseAmount() {
        return purchaseAmount;
    }
    public void setPurchaseAmount(Double purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }
    public Double getAwardAmount() {
        return awardAmount;
    }
    public void setAwardAmount(Double awardAmount) {
        this.awardAmount = awardAmount;
    }
    public boolean getIsNotification() {
        return isNotification;
    }
    public void setIsNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }
    public boolean getIsPersisted() {
        return isPersisted;
    }
    public void setIsPersisted(boolean isPersisted) {
        this.isPersisted = isPersisted;
    }
    public boolean getIsBadge() {
        return isBadge;
    }
    public void setIsBadge(boolean isBadge) {
        this.isBadge = isBadge;
    }
    public boolean getIsQuestionWon() {
        return isQuestionWon;
    }
    public void setIsQuestionWon(boolean isQuestionWon) {
        this.isQuestionWon = isQuestionWon;
    }
    public boolean getIsQuestionLost() {
        return isQuestionLost;
    }
    public void setIsQuestionLost(boolean isQuestionLost) {
        this.isQuestionLost = isQuestionLost;
    }
    public boolean getIsRoundWon() {
        return isRoundWon;
    }
    public void setIsRoundWon(boolean isRoundWon) {
        this.isRoundWon = isRoundWon;
    }
    public boolean getIsRoundLost() {
        return isRoundLost;
    }
    public void setIsRoundLost(boolean isRoundLost) {
        this.isRoundLost = isRoundLost;
    }
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

    public static class GameBadge implements Serializable  {
        private Integer gameBadgeId;
        private Integer contextId;
        private Integer subscriberId;
        private Integer associationId;
        private String  eventTypeKey;
        private String  badgeKey;
        private String  badgeName;
        private String  badgeOverlayThreshold;
        private String  badgeDescription;
        private String  badgePhotoUrl;
        private String  badgeSetKey;
        private Date    createDate;
        
        public GameBadge(){
        }
        
        public Integer getGameBadgeId() {
            return gameBadgeId;
        }
        public void setGameBadgeId(Integer gameBadgeId) {
            this.gameBadgeId = gameBadgeId;
        }
        public Integer getContextId() {
            return contextId;
        }
        public void setContextId(Integer contextId) {
            this.contextId = contextId;
        }
        public Integer getSubscriberId() {
            return subscriberId;
        }
        public void setSubscriberId(Integer subscriberId) {
            this.subscriberId = subscriberId;
        }
        public Integer getAssociationId() {
            return associationId;
        }
        public void setAssociationId(Integer associationId) {
            this.associationId = associationId;
        }
        public String getEventTypeKey() {
            return eventTypeKey;
        }
        public void setEventTypeKey(String eventTypeKey) {
            this.eventTypeKey = eventTypeKey;
        }
        public String getBadgeKey() {
            return badgeKey;
        }
        public void setBadgeKey(String badgeKey) {
            this.badgeKey = badgeKey;
        }
        public String getBadgeName() {
            return badgeName;
        }
        public void setBadgeName(String badgeName) {
            this.badgeName = badgeName;
        }
        public String getBadgeOverlayThreshold() {
            return badgeOverlayThreshold;
        }
        public void setBadgeOverlayThreshold(String badgeOverlay) {
            this.badgeOverlayThreshold = badgeOverlay;
        }
        public String getBadgeDescription() {
            return badgeDescription;
        }
        public void setBadgeDescription(String badgeDescription) {
            this.badgeDescription = badgeDescription;
        }
        public String getBadgePhotoUrl() {
            return badgePhotoUrl;
        }
        public void setBadgePhotoUrl(String badgePhotoUrl) {
            this.badgePhotoUrl = badgePhotoUrl;
        }
        public String getBadgeSetKey() {
            return badgeSetKey;
        }
        public void setBadgeSetKey(String badgeSetKey) {
            this.badgeSetKey = badgeSetKey;
        }
        public Date getCreateDate() {
            return createDate;
        }
        public void setCreateDate(Date createDate) {
            this.createDate = createDate;
        }
    }    
    
    public static class SubscriberStats implements Serializable {
        private Integer subscriberId;
        private Integer totalXp;
        private Integer totalPoints;
        private Integer totalEventPoints;
        private Double  totalPurchasedAmount;
        private Double  totalAwardedAmount;
        private Integer totalQuestionsWon;
        private Integer totalQuestionsLost;
        private Integer totalRoundsWon;
        private Integer totalRoundsLost;
        
        public SubscriberStats(){}

        public Integer getSubscriberId() {
            return subscriberId;
        }
        public void setSubscriberId(Integer subscriberId) {
            this.subscriberId = subscriberId;
        }
        public Integer getTotalXp() {
            return totalXp;
        }
        public void setTotalXp(Integer totalXp) {
            this.totalXp = totalXp;
        }
        public Integer getTotalPoints() {
            return totalPoints;
        }
        public void setTotalPoints(Integer totalPoints) {
            this.totalPoints = totalPoints;
        }
        public Integer getTotalEventPoints() {
            return totalEventPoints;
        }
        public void setTotalEventPoints(Integer totalEventPoints) {
            this.totalEventPoints = totalEventPoints;
        }
        public Double getTotalPurchasedAmount() {
            return totalPurchasedAmount;
        }
        public void setTotalPurchasedAmount(Double totalPurchasedAmount) {
            this.totalPurchasedAmount = totalPurchasedAmount;
        }
        public Double getTotalAwardedAmount() {
            return totalAwardedAmount;
        }
        public void setTotalAwardedAmount(Double totalAwardedAmount) {
            this.totalAwardedAmount = totalAwardedAmount;
        }
        public Integer getTotalQuestionsWon() {
            return totalQuestionsWon;
        }
        public void setTotalQuestionsWon(Integer totalQuestionsWon) {
            this.totalQuestionsWon = totalQuestionsWon;
        }
        public Integer getTotalQuestionsLost() {
            return totalQuestionsLost;
        }
        public void setTotalQuestionsLost(Integer totalQuestionsLost) {
            this.totalQuestionsLost = totalQuestionsLost;
        }
        public Integer getTotalRoundsWon() {
            return totalRoundsWon;
        }
        public void setTotalRoundsWon(Integer totalRoundsWon) {
            this.totalRoundsWon = totalRoundsWon;
        }
        public Integer getTotalRoundsLost() {
            return totalRoundsLost;
        }
        public void setTotalRoundsLost(Integer totalRoundsLost) {
            this.totalRoundsLost = totalRoundsLost;
        }
    }

    public static class EventTransferPayload extends GameInteractionEvent {
        private String  sound;
        private String  message;
        private HashMap<String, Object> extrasMap;
        private HashMap<String, Object> requestPropsMap;
        private GameBadge gameBadge;
        private boolean isChainedEventTrigger;

        public EventTransferPayload(){ 
        }
        
        public EventTransferPayload(Integer contextId, Integer subscriberId, String eventTypeKey, String receivedPayload){
            super.contextId = contextId;
            super.subscriberId = subscriberId;
            super.eventTypeKey = eventTypeKey;
            super.receivedPayload = receivedPayload;
            super.pointsValue = 0;
            super.targetType = TARGET_TYPE.PLAYER;
        }

        public String getSound() {
            return sound;
        }
        public void setSound(String sound) {
            this.sound = sound;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public HashMap<String, Object> getExtrasMap() {
            return extrasMap;
        }
        public void setExtrasMap(HashMap<String, Object> extrasMap) {
            this.extrasMap = extrasMap;
        }
        public HashMap<String, Object> getRequestPropsMap() {
            return requestPropsMap;
        }
        public void setRequestPropsMap(HashMap<String, Object> requestPropsMap) {
            this.requestPropsMap = requestPropsMap;
        }
        public GameBadge getGameBadge(){
            return gameBadge;
        }
        public void setGameBadge(GameBadge gameBadge){
            this.gameBadge = gameBadge;
        }
        public boolean getIsChainedEventTrigger() {
            return isChainedEventTrigger;
        }
        public void setIsChainedEventTrigger(boolean isChainedEventTrigger) {
            this.isChainedEventTrigger = isChainedEventTrigger;
        }        
        
        public static EventTransferPayload getFromString(String payload) throws NotificationException{
            try {
                EventTransferPayload result = jsonMapper.readValue(payload, new TypeReference<EventTransferPayload>() {});
                TypeReference<HashMap<String, Object>> hashMapType =  new TypeReference<HashMap<String, Object>>() {};
                try{
                    // The ReceivedPayload may NOT be a HashMap !!!
                    result.requestPropsMap = jsonMapper.readValue(result.getReceivedPayload(), hashMapType);
                }finally{}
                if (result.sound == null && result.requestPropsMap.containsKey("sound"))
                    result.sound = (String)result.requestPropsMap.get("sound");
                if (result.requestPropsMap.containsKey("extras")){
                    // The extras are a STRING inside the payload
                    // need to extract, convert and the put them back on the payload for downstream use
                    String resultMapString = (String)result.requestPropsMap.get("extras");
                    HashMap<String, Object> resultMap = jsonMapper.readValue(resultMapString, hashMapType);
                    result.setExtrasMap(resultMap);
                }
                return result;
            } catch (Exception e) {
                throw new NotificationException("EventTransferPayload getFromString",e.getMessage(), payload);
            }
        }
    } 

    @SuppressWarnings("serial")
    public static class NotificationException
    extends Exception{
        private String methodName;
        private String message;
        private String payload;
        
        public NotificationException(){}
        
        public NotificationException(String methodName, String message, String payload){
            this.methodName = methodName;
            this.message = message;
            this.payload = payload;
        }

        public String getEventKeyName() {
            return methodName;
        }
        public void setEventKeyName(String methodName) {
            this.methodName = methodName;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public String getPayload() {
            return payload;
        }
        public void setPayload(String payload) {
            this.payload = payload;
        }
        
    }}
