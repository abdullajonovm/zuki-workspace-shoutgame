package tv.shout.shoutcontestaward.eventprocessor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.notification.domain.Notification;
import com.meinc.notification.exception.InvalidMessageException;
import com.meinc.notification.exception.InvalidNotifcationException;
import com.meinc.notification.service.INotificationService;
import com.meinc.push.service.IPushService;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.trigger.domain.Trigger;

import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.EventTransferPayload;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.NotificationException;

public class EventProcessorHelper 
{
    @Autowired
    private INotificationService notificationService;
    
//    private static EventProcessorHelper localInstance = null;
    
//    @Value("${dm.context.id}")
//    private int contextId;

    private Logger logger = Logger.getLogger(EventProcessorHelper.class);

    private ObjectMapper jsonMapper = new ObjectMapper();
    
//    private Set<String> appBundleIds = null;
    
    public EventProcessorHelper()
    {
//        // Initialize this instance of the map
//        DistributedMap<String, App> appByNameMap = DistributedMap.getMap("appByName");
//        AppHelper appHelper = new AppHelper();
//        appHelper.setAppMap(appByNameMap);
//        
//        App app = appHelper.getAppById(contextId);
//        appBundleIds = new HashSet<String>();
//        if (app != null){
//            if (app.getAndroidBundleId() != null) {
//                appBundleIds.add(app.getAndroidBundleId());
//            }
//            if (app.getiOSBundleId() != null) {
//                appBundleIds.add(app.getiOSBundleId());
//            }
//            if (app.getWindowsBundleId() != null) {
//                appBundleIds.add(app.getWindowsBundleId());
//            }
//        }
    }
    
//    private static EventProcessorHelper getInstance(){
//
//        if (localInstance == null){
//            localInstance = new EventProcessorHelper();
//            
//            // Initialize this instance of the map
//            DistributedMap<String, App> appByNameMap = DistributedMap.getMap("appByName");
//            AppHelper appHelper = new AppHelper();
//            appHelper.setAppMap(appByNameMap);
//            
//            App app = appHelper.getAppById(contextId);
//            appBundleIds = new HashSet<String>();
//            if (app != null){
//                if (app.getAndroidBundleId() != null) {
//                    appBundleIds.add(app.getAndroidBundleId());
//                }
//                if (app.getiOSBundleId() != null) {
//                    appBundleIds.add(app.getiOSBundleId());
//                }
//                if (app.getWindowsBundleId() != null) {
//                    appBundleIds.add(app.getWindowsBundleId());
//                }
//            }
//        }
//        return localInstance;
//    } 
    
    public GameInteractionEvent getEventFromSubscriber(int contextId, String eventTypeKey, int points, String pointsSource, Subscriber s){
        GameInteractionEvent gie = new GameInteractionEvent();
        gie.setContextId         (contextId);
        gie.setEventTypeKey      (eventTypeKey);
        gie.setSubscriberId      (s.getSubscriberId());
        gie.setAssociationId     (s.getSubscriberId());
        gie.setTargetType        (GameInteractionEvent.TARGET_TYPE.PLAYER);
        gie.setAssociationDescription (s.getNickname());
        gie.setPointsValue       (points);
        gie.setPointsSource      (pointsSource);
        gie.setPurchaseAmount    (0.0);
        gie.setAwardAmount       (0.0);
        gie.setReceivedPayload   ("");
        gie.setDeliveredPayload  ("");
        gie.setIsNotification    (false);
        gie.setIsPersisted       (false);
        gie.setIsBadge           (false);
        gie.setIsQuestionWon     (false);
        gie.setIsQuestionLost    (false);
        gie.setIsRoundWon        (false);
        gie.setIsRoundLost       (false);
        return gie;
    }
    
    public GameInteractionEvent getEventFromReceiptItem(int contextId, String eventTypeKey, int points, String pointsSource, ReceiptItem r){
        GameInteractionEvent gie = new GameInteractionEvent();
        gie.setContextId        (contextId);
        gie.setEventTypeKey     (eventTypeKey);
        gie.setSubscriberId     (r.getSubscriberId());
        gie.setAssociationId    (r.getReceiptId());
        gie.setTargetType       (GameInteractionEvent.TARGET_TYPE.PLAYER);
        gie.setAssociationDescription(r.getItemName());
        gie.setPointsValue      (points);
        gie.setPointsSource     (pointsSource);
        gie.setPurchaseAmount   (r.getItemPrice());
        gie.setAwardAmount      (0.0);
        gie.setReceivedPayload  ("");
        gie.setDeliveredPayload ("");
        gie.setIsNotification   (false);
        gie.setIsPersisted      (false);
        gie.setIsBadge          (false);
        gie.setIsQuestionWon    (false);
        gie.setIsQuestionLost   (false);
        gie.setIsRoundWon       (false);
        gie.setIsRoundLost      (false);
        return gie;
    }
    
    public GameInteractionEvent getEventFromJsonPayload(int contextId, String eventTypeKey, String payload){
        // TODO
        return null;
    }
    
    public GameInteractionEvent getEventFromExtrasMap(int contextId, String eventTypeKey, HashMap<String, Object> dataMap, String eventPayload){
        // TODO: get intelligent about how to build the event
        try{
            GameInteractionEvent gie = new GameInteractionEvent();
            gie.setContextId        (contextId);
            gie.setEventTypeKey     (eventTypeKey);
            
            if (dataMap.containsKey("game_id")){
                gie.setGameId((String)dataMap.get("game_id"));
            }
            if (dataMap.containsKey("round_id")){
                gie.setRoundId((String)dataMap.get("round_id"));
            }
            if (dataMap.containsKey("level_id")){
                gie.setLevelId((String)dataMap.get("level_id"));
            }
            if (dataMap.containsKey("level_value")){
                gie.setLevelId((String)dataMap.get("level_value"));
            }
            if (dataMap.containsKey("gameId")){
                gie.setGameId((String)dataMap.get("gameId"));
            }
            
            if (dataMap.containsKey("recipientId")){
                gie.setSubscriberId(Integer.parseInt((String)dataMap.get("recipientId")));        
            }
            gie.setAssociationId    (contextId);        
            gie.setTargetType       (GameInteractionEvent.TARGET_TYPE.PLAYER);
            gie.setAssociationDescription(eventTypeKey);
            gie.setPointsValue      (0);
            gie.setPointsSource     ("");
            gie.setPurchaseAmount   (0.0);
            gie.setAwardAmount      (0.0);
            gie.setReceivedPayload  (eventPayload);
            gie.setDeliveredPayload ("");
            gie.setIsNotification   (false);
            gie.setIsPersisted      (false);
            gie.setIsBadge          (false);
            gie.setIsQuestionWon    (false);
            gie.setIsQuestionLost   (false);
            gie.setIsRoundWon       (false);
            gie.setIsRoundLost      (false);
            return gie;
        }catch(Exception ex){
            logger.error(ex);
            throw ex;
        }
    }
    
    public GameInteractionEvent getEventFromEventTransferPayload(EventTransferPayload etp){
        try{
            return (GameInteractionEvent)etp;
        }catch(Exception ex){
            logger.error(ex);
            throw ex;
        }
    } 
    
        
    public GameInteractionEvent getEventFromBadge(int contextId, Trigger trigger, GameBadge badge, String eventPayload) throws JsonProcessingException
    {
        return getEventFromBadge(contextId, trigger.getKey(), trigger.getSource(), badge, eventPayload);
    }
    public GameInteractionEvent getEventFromBadge(int contextId, String eventTypeKey, String pointsSource, GameBadge badge, String eventPayload) throws JsonProcessingException
    {
        GameInteractionEvent gie = new GameInteractionEvent();
        gie.setContextId        (contextId);
        gie.setEventTypeKey     (eventTypeKey);
        gie.setSubscriberId     (badge.getSubscriberId());        
        gie.setAssociationId    (contextId);        
        gie.setTargetType       (GameInteractionEvent.TARGET_TYPE.PLAYER);
        gie.setAssociationDescription(eventTypeKey);
        gie.setPointsValue      (0);
        gie.setPointsSource     (pointsSource);
        gie.setPurchaseAmount   (0.0);
        gie.setAwardAmount      (0.0);
        gie.setReceivedPayload  (eventPayload);

        String badgeSerialized = jsonMapper.writeValueAsString(badge);
        gie.setDeliveredPayload(badgeSerialized);
        
        gie.setIsNotification   (false);
        gie.setIsPersisted      (false);
        gie.setIsBadge          (true);
        gie.setIsQuestionWon    (false);
        gie.setIsQuestionLost   (false);
        gie.setIsRoundWon       (false);
        gie.setIsRoundLost      (false);
        return gie;
    }
    
    public GameBadge getBadgeFromExtrasMap(int contextId, String eventTypeKey, String badgeKey, HashMap<String, Object> extrasMap, String eventPayload){
        GameBadge gb = new GameBadge();
        gb.setContextId(contextId);
        // TODO: interogate the extrasMap to ID the subscriber to which this badge should be awarded
        gb.setSubscriberId(0);
        gb.setAssociationId(0);
        gb.setEventTypeKey(eventTypeKey);
        gb.setBadgeKey(badgeKey);        
        return gb;
    }

//    private static void loadNotificationAppTokens(){
//        getInstance().logger.info("AAAAAAAA");
//        if (getInstance().bundleIds == null) {
//            getInstance().logger.info("A-A-A-A-A-A");
//            DistributedMap<String, App> appByNameMap = DistributedMap.getMap("appByName");
//            getInstance().appHelper.setAppMap(appByNameMap);
//            getInstance().logger.info("BBBBBBBB");
//            App app = getInstance().appHelper.getAppById(getInstance().contextId);
//            getInstance().bundleIds = new HashSet<String>();
//            if (app.getAndroidBundleId() != null) {
//                getInstance().bundleIds.add(app.getAndroidBundleId());
//            }
//            getInstance().logger.info("CCCCCCCCCCC");
//            if (app.getiOSBundleId() != null) {
//                getInstance().bundleIds.add(app.getiOSBundleId());
//            }
//            getInstance().logger.info("DDDDDDDDDD");
//            if (app.getWindowsBundleId() != null) {
//                getInstance().bundleIds.add(app.getWindowsBundleId());
//            }
//            getInstance().logger.info("DDDDDDDDDD");
//        }
//    }
    
    public void sendPersistedNotification(Set<String> appBundleIds, EventTransferPayload eventTransfer) 
    throws NotificationException
    {
        if (appBundleIds == null) return;
        
        Integer recipientId         = eventTransfer.getSubscriberId();
        String notificationType     = eventTransfer.getEventTypeKey();
        if (eventTransfer.getGameBadge()!= null)
        	notificationType = eventTransfer.getGameBadge().getBadgeKey();
        String message              = eventTransfer.getMessage();
        int contextId               = eventTransfer.getContextId();
        String description          = eventTransfer.getAssociationDescription();
        Map<String, Object> extras  = eventTransfer.getExtrasMap();
        
        // Append "DM_" to the front of the notificationType
        // And UCASE this key
        notificationType = "DM_" + notificationType;
        notificationType = notificationType.toUpperCase();
        
        //EventProcessorHelper.loadNotificationAppTokens();
        logger.info(MessageFormat.format("Attempting to send notification to:{0}, type:{1}", recipientId + "", notificationType));
        
        // message = _localizedHelper.getLocalizedList(notificationType);
        
        Notification n = new Notification();
        n.setType(notificationType);
        n.setContextId(contextId);
        n.setMessage(message);
        n.setDescription("");
        n.setSender(1); //doesn't matter; just assume the "system" user
        n.setRecipient(recipientId);
        n.setExtras(extras);
        n.setActionType(INotificationService.ACTION_TYPE_NONE);
        n.setPayload("");
        
        try {
            // Send notification
            final Map<String,String> messagesByDeliveryType = new HashMap<String, String>();            
            messagesByDeliveryType.put(INotificationService.COMM_TYPE_APP_PUSH, description != null ? description : message);
            notificationService.sendNotification(contextId, n, messagesByDeliveryType, appBundleIds);
        } catch (InvalidMessageException | InvalidNotifcationException e) {
            logger.error(MessageFormat.format("sendPersistedNotification to:{0}, {1}", recipientId, e.getMessage()), e);
//            logger.error(Arrays.toString(e.getStackTrace()));
            throw new NotificationException("","","");
        }
        
        logger.debug(MessageFormat.format("sendPersistedNotification success, subscriberId: {0}", recipientId + ""));
    }
    
    public void sendTransientNotification(IPushService pushService, EventTransferPayload eventTransfer) 
    throws NotificationException
    {
    	
    	logger.debug(MessageFormat.format("sendTransientNotification NOT IMPLEMENTED: {0}", ""));
    	return;
    	
        /// String sound               = eventTransfer.getSound();       
        /// String message             = eventTransfer.getMessage();     
        /// String pushCategory        = eventTransfer.getEventTypeKey();
        /// Integer recipientId        = eventTransfer.getSubscriberId(); 
        /// Map<String, Object> extras = eventTransfer.getExtrasMap();   
        /// 
        /// getInstance().logger.debug("attempting to send push, type: " + pushCategory);
        /// 
        /// //build up the push payload
        /// Map<String, String> aps = new HashMap<>();
        /// aps.put("alert", message);
        /// if (sound != null) aps.put("sound", sound);
        /// aps.put("category", pushCategory);
        /// 
        /// final Map<String, Object> pushPayload = new HashMap<String, Object>();
        /// pushPayload.put("aps", aps);
        /// pushPayload.put("type", "MESSAGE");
        /// pushPayload.put("extras", extras);
        /// 
        /// try {
        ///     ObjectMapper mapper = new ObjectMapper();
        ///     pushService.pushPayloadToSubscribers(mapper.writeValueAsString(pushPayload), Arrays.asList(recipientId), NotificationHandler.appBundleIds, false);
        /// } catch (JsonProcessingException | PushNetworkIoException | PayloadTooLargeException e) {
        ///     getInstance().logger.error(MessageFormat.format("sendTransientNotification to:{0}, {1}", recipientId, e.getMessage()));
        ///     getInstance().logger.error(Arrays.toString(e.getStackTrace()));
        ///     throw new NotificationHandler.NotificationException("","","");
        /// }
        /// getInstance().logger.debug(MessageFormat.format("sendTransientNotification success, subscriberId: {0}", recipientId + ""));
    }    
}
