package tv.shout.shoutcontestaward.eventprocessor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.trigger.domain.Trigger;
import com.meinc.trigger.filter.Filter;
import com.meinc.trigger.service.ITriggerService;

import tv.shout.shoutcontestaward.dao.IShoutContestAwardServiceDao;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.EventTransferPayload;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.NotificationException;
import tv.shout.shoutcontestaward.domain.GamePayout;
import tv.shout.shoutcontestaward.service.IShoutContestAwardService;
import tv.shout.util.JsonUtil;

public class EventProcessorFilter
implements Filter, GameInteractionEvent.IXPAndPointsPersister
{
    private Logger logger = Logger.getLogger(EventProcessorFilter.class);

    private ObjectMapper jsonMapper = JsonUtil.getObjectMapper();

    @Autowired
    private ITriggerService triggerService;

    @Autowired
    private IShoutContestAwardServiceDao dao;

//    @Autowired
//    private IPushService pushService;

//    @Autowired
//    private INotificationService notificationService;

    @Autowired
    private EventProcessorHelper eventProcessorHelper;

//    @Autowired
//    private PlatformTransactionManager transactionManager;

    private EventProcessorPlayer eventProcessorPlayer;
    private EventProcessorGame eventProcessorGame;
    private EventProcessorQuestion eventProcessorQuestion;
    private EventProcessorBadge eventProcessorBadge;

    public EventProcessorFilter()
    {
        this.eventProcessorGame = new EventProcessorGame();
        this.eventProcessorPlayer = new EventProcessorPlayer();
        this.eventProcessorQuestion = new EventProcessorQuestion();
        this.eventProcessorBadge = new EventProcessorBadge();
    }

    @Override
    public boolean process(Trigger trigger)
    {
        if (this.eventProcessorPlayer.handleTrigger(trigger, this))
            return true;
        if (this.eventProcessorGame.handleTrigger(trigger, this))
            return true;
        if (this.eventProcessorQuestion.handleTrigger(trigger, this))
            return true;
        if (this.eventProcessorBadge.handleTrigger(trigger, this))
            return true;

        return false;
    }

    // TODO: COPYHACK - GamePlayService.BaseXPMessageHandler
    @Override
    public void giveXpAndPoints(int subscriberId, int xp, Integer contextId, int points, String xpType)
    {
//        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
//        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

//        try {
            //update db with the new XP
            // the 99.999% case is that the row already exists and just needs to be updated. In the .001% case, do an insert (lazy init on the row's data)
            if (xpType == null)
                xpType = "UNKNOWN";
            int updateRowCount = dao.updateSubscriberXpAndPoints(subscriberId, xp, points);
            if (updateRowCount == 0) {
                dao.insertSubscriberXpAndPoints(subscriberId, xp, points);
            }

            if (xp > 0) {
                dao.addSubscriberXpHistory(subscriberId, xp, xpType, contextId);
            }
            if (points > 0) {
                //in this case, contextId IS eventId always
                dao.addSubscriberPointHistory(subscriberId, points, contextId);
            }

//            Date date = new Date();

//            // Add a sync message for xp_awarded and points awarded
//            if (xp > 0) {
//                Map<String, Object> xpMap = new HashMap<String, Object>();
//                xpMap.put("xp", xp);
//                xpMap.put("type", XP_TYPE.DAILY_MILLIONAIRE.toString());
//                xpMap.put("awardedDate", date);
//                xpMap.put("contextId", contextId);
//                try {
//                    eventProcessorDaoMapper.insertSyncMessage(subscriberId, "XP_AWARDED", contextId, jsonMapper.writeValueAsString(xpMap), date);
//                    SyncMessageHandler.markSyncChangeForSubscriber(subscriberId);
//                    //FUTURE: possibly also regenerate the profileSummaryData document? or wait for on-demand
//                } catch (Exception e) {
//                    logger.error("unable to write XP_AWARDED sync message", e);
//                    return;
//                }
//            }
//
//            if (points > 0) {
//                eventProcessorDaoMapper.insertSyncMessage(subscriberId, "POINTS_AWARDED", contextId, points+"", date);
//                SyncMessageHandler.markSyncChangeForSubscriber(subscriberId);
//            }

//            transactionManager.commit(txStatus);
//            txStatus = null;
//        } finally {
//            if (txStatus != null) {
//                transactionManager.rollback(txStatus);
//                txStatus = null;
//            }
//        }
    }

    @SuppressWarnings("serial")
    private static final ArrayList<String> validDynamicBadgeKeys = new ArrayList<String>(){
    {
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_5");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_10");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_15");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_25");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_50");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_100");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_200");
    	add(IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_500");
    	add(IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_5");
    	add(IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_10");
    	add(IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_15");
    	add(IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_20");
    	add(IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_25");
    }};

    public static boolean getIsValidBadgeKey(String badgeKey){
    	return validDynamicBadgeKeys.contains(badgeKey);
    }

    public class EventProcessorPlayer
    {
        private final List<String> validTriggerKeys = Arrays.asList(
        		IShoutContestAwardService.USER_EVENT_KEY_user_account_created,
        		IShoutContestAwardService.USER_EVENT_KEY_user_account_loggedin,
        		IShoutContestAwardService.USER_EVENT_KEY_user_account_updated,
        		IShoutContestAwardService.USER_EVENT_KEY_item_purchased
        );

        private boolean getIsMyTrigger(String triggerKey){
        	return validTriggerKeys.contains(triggerKey);
        }

        public boolean handleTrigger(Trigger trigger, GameInteractionEvent.IXPAndPointsPersister xpGiver )
        {
            logger.info(MessageFormat.format(">>> EventProcessorPlayer.handleTrigger trigger:{0}", trigger.getKey()));

            //make sure this is something the filter cares about
            if (trigger == null || trigger.getPayload() == null) {
                return true;
            }

            if (!this.getIsMyTrigger(trigger.getKey()))
            	return false;

            GameInteractionEvent event = null;
            Subscriber subscriber = null;
            TypeReference<Subscriber> subscriberType;
            TypeReference<ReceiptItem> receiptItemType;
            String payload = (String)trigger.getPayload();
            Boolean isHandledTrigger = false;
            Integer xPoints = 0;
            Integer pPoints = 0;

            switch(trigger.getKey()){

                case IShoutContestAwardService.USER_EVENT_KEY_user_account_created:
                    try {
                        subscriberType = new TypeReference<Subscriber>() {};
                        subscriber = jsonMapper.readValue(payload, subscriberType);
                        event = eventProcessorHelper.getEventFromSubscriber(trigger.getContextId(), trigger.getKey(), 100, trigger.getSource(), subscriber);
                        dao.addGameInteractionEvent(event);
                        xPoints = 100;
                        pPoints = 100;
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    isHandledTrigger = true;
                    break;

                case IShoutContestAwardService.USER_EVENT_KEY_user_account_loggedin:
                    try {
                        subscriberType = new TypeReference<Subscriber>() {};
                        subscriber = jsonMapper.readValue(payload, subscriberType);
                        event = eventProcessorHelper.getEventFromSubscriber(trigger.getContextId(), trigger.getKey(), 10, trigger.getSource(), subscriber);
                        dao.addGameInteractionEvent(event);
                        xPoints = 10;
                        pPoints = 10;
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    isHandledTrigger = true;
                    break;

                case IShoutContestAwardService.USER_EVENT_KEY_user_account_updated:
                    try {
                        subscriberType = new TypeReference<Subscriber>() {};
                        subscriber = jsonMapper.readValue(payload, subscriberType);
                        event = eventProcessorHelper.getEventFromSubscriber(trigger.getContextId(), trigger.getKey(), 10, trigger.getSource(), subscriber);
                        dao.addGameInteractionEvent(event);

                        logger.info(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), ">>> about to create badge triggers..."));

                        // Get all the badges for this user -- we'll need them to test in the future.
                        boolean isCreateBadge = false;
                		boolean isPlayerHasBadge = false;
                    	List<GameBadge> badges = dao.getBadgesForSubscriber(subscriber.getSubscriberId());

                        // BADGE_KEY_user_account_updated ?
    	                if (badges.size() > 0){
    	                	isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(IShoutContestAwardService.BADGE_KEY_user_account_updated));
    	                    if (!isPlayerHasBadge){
    	                    	isCreateBadge = true;
    	                    }
    	                }else{
    	                	isCreateBadge = true;
    	                }
    	                if (isCreateBadge){
    	                	EventTransferPayload eventTransfer = new EventTransferPayload(trigger.getContextId(), subscriber.getSubscriberId(), trigger.getKey(), null);
    	                    GameBadge gameBadge = new GameBadge();
    	                    gameBadge.setContextId(eventTransfer.getContextId());
    	                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
    	                    gameBadge.setAssociationId(eventTransfer.getContextId());
    	                    gameBadge.setEventTypeKey(trigger.getKey());
    	                    gameBadge.setBadgeKey(IShoutContestAwardService.BADGE_KEY_user_account_updated);
    	                    xPoints = pPoints = 30;
    	                    eventTransfer.setPointsValue(xPoints);
    	                    eventTransfer.setGameBadge(gameBadge);
    	                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
    	                    triggerService.enqueue(
    	                            IShoutContestAwardService.BADGE_KEY_user_account_updated,
    	                            eventTransferJson,
    	                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
    	                            trigger.getSource(),
    	                            trigger.getBundleIds(),
    	                            trigger.getContextId());
    	                }

                        // BADGE_KEY_user_account_profile_picture ?
    	                if (badges.size() > 0){
    	                	isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(IShoutContestAwardService.BADGE_KEY_user_account_profile_picture));
    	                    if (!isPlayerHasBadge){
    	                    	isCreateBadge = true;
    	                    }
    	                }else{
    	                	isCreateBadge = true;
    	                }
    	                if (isCreateBadge && subscriber.getPhotoUrl() != null && subscriber.getPhotoUrl().trim().length() > 0){
    	                	EventTransferPayload eventTransfer = new EventTransferPayload(trigger.getContextId(), subscriber.getSubscriberId(), trigger.getKey(), "{\"photoUrl\":\"" + subscriber.getPhotoUrl() + "\"}");
    	                    GameBadge gameBadge = new GameBadge();
    	                    gameBadge.setContextId(eventTransfer.getContextId());
    	                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
    	                    gameBadge.setAssociationId(eventTransfer.getContextId());
    	                    gameBadge.setEventTypeKey(trigger.getKey());
    	                    gameBadge.setBadgeKey(IShoutContestAwardService.BADGE_KEY_user_account_profile_picture);
    	                    xPoints = pPoints = 50;
    	                    eventTransfer.setPointsValue(xPoints);
    	                    eventTransfer.setGameBadge(gameBadge);
    	                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
    	                    triggerService.enqueue(
    	                            IShoutContestAwardService.BADGE_KEY_user_account_profile_picture,
    	                            eventTransferJson,
    	                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
    	                            trigger.getSource(),
    	                            trigger.getBundleIds(),
                                    trigger.getContextId());
    	                }
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    isHandledTrigger = true;
                    break;

                case IShoutContestAwardService.USER_EVENT_KEY_item_purchased:
                    try {
                        receiptItemType = new TypeReference<ReceiptItem>() {};
                        ReceiptItem receiptItem = jsonMapper.readValue(payload, receiptItemType);
                        if (receiptItem == null)
                            logger.error(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), "receiptItem == null"));
                        event = eventProcessorHelper.getEventFromReceiptItem(trigger.getContextId(), trigger.getKey(), (int)(receiptItem.getItemPrice() * 10), trigger.getSource(), receiptItem);
                        dao.addGameInteractionEvent(event);
                        xPoints = (int)(receiptItem.getItemPrice() * 10);
                        pPoints = (int)(receiptItem.getItemPrice() * 10);
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("EventProcessorPlayer.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    isHandledTrigger = true;
                    break;
                case "":
                    logger.error(MessageFormat.format("EventProcessorPlayer !!EMPTY!! trigger key {0}", ""));
                    break;
                default:
                    break;
            }

            if (isHandledTrigger) {
                if (event != null && xPoints > 0 || pPoints > 0){
                    xpGiver.giveXpAndPoints(event.getSubscriberId(), xPoints, event.getGameInteractionEventId(), pPoints, trigger.getSource());
                }
                return true;
            }else{
                return false;
            }
        }
    }

    public class EventProcessorGame
    {
        private final List<String> validTriggerKeys = Arrays.asList(
        		 IShoutContestAwardService.GAME_EVENT_KEY_game_started
        		,IShoutContestAwardService.GAME_EVENT_KEY_game_ended
        		,IShoutContestAwardService.GAME_EVENT_KEY_question
        		,IShoutContestAwardService.GAME_EVENT_KEY_question_result
        		,IShoutContestAwardService.GAME_EVENT_KEY_round_started
        		,IShoutContestAwardService.GAME_EVENT_KEY_round_ended
        );

        private boolean getIsMyTrigger(String triggerKey){
        	return validTriggerKeys.contains(triggerKey);
        }

        @SuppressWarnings("unchecked")
        public boolean handleTrigger(Trigger trigger, GameInteractionEvent.IXPAndPointsPersister xpGiver )
        {
            logger.info(MessageFormat.format(">>> EventProcessorGame.handleTrigger trigger:{0}", trigger.getKey()));

            // Ensure this is something the filter cares about
            if (trigger == null || trigger.getPayload() == null) {
                return true;
            }

            if (!this.getIsMyTrigger(trigger.getKey()))
            	return false;

            String meStatus = null;
            String opponentStatus = null;
            String payload = (String)trigger.getPayload();
            HashMap<String, Object> extrasMap = null;
            HashMap<String, Object> meMap = null;
            HashMap<String, Object> opponentMap = null;
            GameInteractionEvent event = null;
            EventTransferPayload eventTransfer = null;
            Boolean isHandledTrigger = false;
            Integer xPoints = 0;
            Integer pPoints = 0;

            try {
                eventTransfer = EventTransferPayload.getFromString(payload);
                extrasMap = eventTransfer.getExtrasMap();

                // Common values for all event payloads
                if (extrasMap.containsKey("game_id"))
                	eventTransfer.setGameId(extrasMap.get("game_id") + "");
                if (extrasMap.containsKey("round_id"))
                	eventTransfer.setRoundId(extrasMap.get("round_id") + "");
                if (extrasMap.containsKey("level_id"))
                	eventTransfer.setLevelId(extrasMap.get("level_id") + "");
                if (extrasMap.containsKey("level_value"))
                	eventTransfer.setLevelValue(extrasMap.get("level_value") + "");

            } catch (Exception e) {
                logger.error(MessageFormat.format("a0. EventProcessorGame.{0}, {1}", trigger.getKey(), e.getMessage()));
                logger.error(Arrays.toString(e.getStackTrace()));
                return false;
            }

            switch(trigger.getKey()){
                case IShoutContestAwardService.GAME_EVENT_KEY_game_started   :
                case IShoutContestAwardService.GAME_EVENT_KEY_game_ended     :
                    logger.error(MessageFormat.format("a1. EventProcessorGame.{0} handler NOT IMPLEMENTED", trigger.getKey()));
                    break;
                case "":
                    logger.error(MessageFormat.format("b. EventProcessorGame !!EMPTY!! trigger key {0}", ""));
                    break;
                default:
                    break;
            }

            if (isHandledTrigger) {
                if (event != null && xPoints > 0 || pPoints > 0){
                    xpGiver.giveXpAndPoints(event.getSubscriberId(), xPoints, event.getGameInteractionEventId(), pPoints, trigger.getSource());
                }
                return true;
            }else{
                return false;
            }
        }
    }

    public class EventProcessorQuestion
    {
        private final List<String> validTriggerKeys = Arrays.asList(
        		 IShoutContestAwardService.QUESTION_KEY_question_received
        		,IShoutContestAwardService.QUESTION_KEY_question_won_correct
        		,IShoutContestAwardService.QUESTION_KEY_question_won_faster
        		,IShoutContestAwardService.QUESTION_KEY_question_won_timeout
        		,IShoutContestAwardService.QUESTION_KEY_question_lost_incorrect
        		,IShoutContestAwardService.QUESTION_KEY_question_lost_slower
        		,IShoutContestAwardService.QUESTION_KEY_question_lost_timeout
        		,IShoutContestAwardService.QUESTION_KEY_question_lost_all_timeout
    		    ,IShoutContestAwardService.MATCH_KEY_match_won
    		    ,IShoutContestAwardService.MATCH_KEY_match_lost
                ,IShoutContestAwardService.ROUND_KEY_round_won
                ,IShoutContestAwardService.ROUND_KEY_round_lost
                ,IShoutContestAwardService.GAME_KEY_game_won
                ,IShoutContestAwardService.GAME_KEY_game_lost
        );

        private boolean getIsMyTrigger(String triggerKey){
        	return validTriggerKeys.contains(triggerKey);
        }

        public boolean handleTrigger(Trigger trigger, GameInteractionEvent.IXPAndPointsPersister xpGiver )
        {
            logger.info(MessageFormat.format(">>> EventProcessorQuestion.handleTrigger trigger:{0}", trigger.getKey()));
            if (trigger == null || trigger.getPayload() == null) {
                return true;
            }

            if(!this.getIsMyTrigger(trigger.getKey()))
                return false;

            GameInteractionEvent event = null;
            EventTransferPayload eventTransfer = null;

            try {
                String payload = (String)trigger.getPayload();
                eventTransfer = EventTransferPayload.getFromString(payload);
            } catch (Exception e) {
                logger.error(MessageFormat.format("a. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e.getMessage()));
                logger.error(Arrays.toString(e.getStackTrace()));
                return false;
            }

            // TODO: set messages for each type of question/round event "You won the question", "You won the round"
            eventTransfer.setMessage(trigger.getKey());
            eventTransfer.setIsNotification(true);
            eventTransfer.setIsPersisted(true);
            eventTransfer.setIsChainedEventTrigger(false);

            switch(trigger.getKey()){
                case IShoutContestAwardService.QUESTION_KEY_question_received         :
                    eventTransfer.setIsPersisted(false);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_won_correct      :
                    eventTransfer.setMessage("You won the question!");
                    eventTransfer.setPointsValue(10);
                    eventTransfer.setIsQuestionWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_won_faster       :
                    eventTransfer.setMessage("You won the question!");
                    eventTransfer.setPointsValue(10);
                    eventTransfer.setIsQuestionWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_won_timeout      :
                    eventTransfer.setMessage("You won the question!");
                    eventTransfer.setPointsValue(10);
                    eventTransfer.setIsQuestionWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_lost_incorrect   :
                    eventTransfer.setMessage("You lost the question!");
                    eventTransfer.setPointsValue(1);
                    eventTransfer.setIsQuestionLost(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_lost_slower      :
                    eventTransfer.setMessage("You lost the question!");
                    eventTransfer.setPointsValue(1);
                    eventTransfer.setIsQuestionLost(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_lost_timeout     :
                    eventTransfer.setMessage("You lost the question!");
                    eventTransfer.setPointsValue(1);
                    eventTransfer.setIsQuestionLost(true);
                    break;
                case IShoutContestAwardService.QUESTION_KEY_question_lost_all_timeout :
                    eventTransfer.setMessage("You lost the question!");
                    eventTransfer.setPointsValue(1);
                    eventTransfer.setIsQuestionLost(true);
                    eventTransfer.setIsNotification(false);
                    break;
                case IShoutContestAwardService.MATCH_KEY_match_won           :
                    eventTransfer.setMessage("You won the match!");
                    eventTransfer.setPointsValue(25);
                    eventTransfer.setIsRoundWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    eventTransfer.setIsNotification(false);
                    break;
                case IShoutContestAwardService.MATCH_KEY_match_lost          :
                    eventTransfer.setMessage("You lost the match!");
                    eventTransfer.setPointsValue(5);
                    eventTransfer.setIsRoundLost(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    eventTransfer.setIsNotification(false);
                    break;
                case IShoutContestAwardService.ROUND_KEY_round_won           :
                    eventTransfer.setMessage("You won the round!");
                    eventTransfer.setPointsValue(100);
                    eventTransfer.setIsRoundWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    eventTransfer.setIsNotification(false);
                    break;
                case IShoutContestAwardService.ROUND_KEY_round_lost          :
                    eventTransfer.setMessage("You lost the round!");
                    eventTransfer.setPointsValue(5);
                    eventTransfer.setIsRoundLost(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    eventTransfer.setIsNotification(false);
                    break;
                case IShoutContestAwardService.ROUND_KEY_round_lost_in_the_money :
                    eventTransfer.setMessage("You lost the round but won cash!");
                    eventTransfer.setPointsValue(100);
                    eventTransfer.setIsRoundLost(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    //
                    // Comes from RoundEngine, and or GameEngine Trigger message
                    // Rehydrate the GamePayout and save it
                    // Ensure no duplicates.
                    //
                    String gamePayoutStr2 = eventTransfer.getReceivedPayload();
                    TypeReference<GamePayout> gamePayoutType2 =  new TypeReference<GamePayout>() {};
                    try {
                        GamePayout gamePayout = jsonMapper.readValue(gamePayoutStr2, gamePayoutType2);
                        dao.addGamePayout(gamePayout);
                        // TODO: send notification
                    } catch (IOException e) {
                        logger.error(MessageFormat.format("a1x. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e.getMessage()));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case IShoutContestAwardService.GAME_KEY_game_won             :
                    eventTransfer.setMessage("You won the game!");
                    eventTransfer.setPointsValue(1000);
                    eventTransfer.setIsRoundWon(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    //
                    // Comes from RoundEngine, and or GameEngine Trigger message
                    // Rehydrate the GamePayout and save it
                    // Ensure no duplicates.
                    //
                    String gamePayoutStr = eventTransfer.getReceivedPayload();
                    TypeReference<GamePayout> gamePayoutType =  new TypeReference<GamePayout>() {};
                    try {
                        GamePayout gamePayout = jsonMapper.readValue(gamePayoutStr, gamePayoutType);
                        dao.addGamePayout(gamePayout);
                        // TODO: send notification
                    } catch (IOException e) {
                        logger.error(MessageFormat.format("a2x. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e.getMessage()));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case IShoutContestAwardService.GAME_KEY_game_lost            :
                    eventTransfer.setMessage("You lost the game!");
                    eventTransfer.setPointsValue(200);
                    eventTransfer.setIsRoundLost(true);
                    eventTransfer.setIsChainedEventTrigger(true);
                    //
                    // Comes from RoundEngine, and or GameEngine Trigger message
                    // Rehydrate the GamePayout and save it
                    // Ensure no duplicates.
                    //
                    String gamePayoutStr3 = eventTransfer.getReceivedPayload();
                    TypeReference<GamePayout> gamePayoutType3 =  new TypeReference<GamePayout>() {};
                    try {
                        GamePayout gamePayout = jsonMapper.readValue(gamePayoutStr3, gamePayoutType3);
                        dao.addGamePayout(gamePayout);
                        // TODO: send notification
                    } catch (IOException e) {
                        logger.error(MessageFormat.format("a3x. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e.getMessage()));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "":
                    logger.error(MessageFormat.format("b. EventProcessorQuestion !!EMPTY!! trigger key {0}", ""));
                    return false;
                default:
                    return false;
            }

            // Persist this particular round event
            if (eventTransfer.getIsPersisted()){
                try {
                    event = eventProcessorHelper.getEventFromEventTransferPayload(eventTransfer);
                    dao.addGameInteractionEvent(event);
                } catch (Exception e) {
                    logger.error(MessageFormat.format("c1. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e));
                    logger.error(Arrays.toString(e.getStackTrace()));
                }

                // Send PERSISTED notification
                if (eventTransfer.getIsNotification()){
                    try{
                        eventTransfer.setAssociationDescription("NO DESCRIPTION");
                        eventProcessorHelper.sendPersistedNotification(trigger.getBundleIds(), eventTransfer);
                    } catch (NotificationException e) {
                        logger.error(MessageFormat.format("c2. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                }
            }else{
                // Send TRANSIENT notification
            	// TODO: do NOT send transients for question or round_started YET, 8/21/2016
                ///if (eventTransfer.getIsNotification()){
                ///    try{
                ///    	eventTransfer.setAssociationDescription("NO DESCRIPTION");
                ///        EventProcessorHelper.sendTransientNotification(pushService, eventTransfer);
                ///    } catch (NotificationException e) {
                ///        logger.error(MessageFormat.format("c3. EventProcessorQuestion.{0}, {1}", trigger.getKey(), e));
                ///        logger.error(Arrays.toString(e.getStackTrace()));
                ///    }
                ///}
            }

            // Points?
            if (event != null && eventTransfer.getPointsValue() > 0){
                xpGiver.giveXpAndPoints(event.getSubscriberId(), event.getPointsValue(), event.getGameInteractionEventId(), event.getPointsValue(), trigger.getSource());
            }

            // Should we perform event interrogation to determine if additional events should be created from this event...
            if (!eventTransfer.getIsChainedEventTrigger()){
                return true;
            }

            try {

                // TODO: Awarded money and In the money badges need to be created

            	logger.info(MessageFormat.format("d. EventProcessorQuestion.{0}, {1}", trigger.getKey(), ">>> about to create badge triggers..."));

                // Get all the badges for this user -- we'll need them to test in the future.
                boolean isCreateBadge = false;
        		boolean isPlayerHasBadge = false;
            	List<GameBadge> badges = dao.getBadgesForSubscriber(eventTransfer.getSubscriberId());

                // BADGE_KEY_player_played_first_round ?
                if (eventTransfer.getIsRoundLost() || eventTransfer.getIsRoundWon()){

                	// BADGE_KEY_player_played_first_round ?
	                if (badges.size() > 0){
	                	isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(IShoutContestAwardService.BADGE_KEY_player_played_first_round));
	                    if (!isPlayerHasBadge){
	                    	isCreateBadge = true;
	                    }
	                }else{
	                	isCreateBadge = true;
	                }

	                if (isCreateBadge){
	                    GameBadge gameBadge = new GameBadge();
	                    gameBadge.setContextId(eventTransfer.getContextId());
	                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
	                    gameBadge.setAssociationId(eventTransfer.getContextId());
	                    gameBadge.setEventTypeKey(trigger.getKey());
	                    gameBadge.setBadgeKey(IShoutContestAwardService.BADGE_KEY_player_played_first_round);
	                    eventTransfer.setGameBadge(gameBadge);
	                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
	                    triggerService.enqueue(
	                            IShoutContestAwardService.BADGE_KEY_player_played_first_round,
	                            eventTransferJson,
	                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
	                            trigger.getSource(),
	                            trigger.getBundleIds(),
                                trigger.getContextId());
	                }
                }

	            // BADGE_KEY_player_rounds_played ?
                isCreateBadge = false;
                isPlayerHasBadge = false;
	            if (eventTransfer.getIsRoundLost() || eventTransfer.getIsRoundWon()){
                	Integer totalRoundsPlayed = dao.getSubscriberRoundsPlayed(eventTransfer.getSubscriberId());

	                if (totalRoundsPlayed != null && totalRoundsPlayed > 0){
	                    String badgeKey = IShoutContestAwardService.BADGE_KEY_player_rounds_played + "_" + totalRoundsPlayed;
	                	boolean isValidBadgeKey = getIsValidBadgeKey(badgeKey);
	                	if (isValidBadgeKey){
	                		if (badges.size() > 0)
	                			isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(badgeKey));
	                		if (!isPlayerHasBadge)
	                			isCreateBadge = true;
	                    }
		                if (isCreateBadge){
		                    GameBadge gameBadge = new GameBadge();
		                    gameBadge.setContextId(eventTransfer.getContextId());
		                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
		                    gameBadge.setAssociationId(eventTransfer.getContextId());
		                    gameBadge.setEventTypeKey(trigger.getKey());
		                    gameBadge.setBadgeKey(badgeKey);
		                    eventTransfer.setGameBadge(gameBadge);
		                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
		                    triggerService.enqueue(
		                            badgeKey,
		                            eventTransferJson,
		                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
		                            trigger.getSource(),
		                            trigger.getBundleIds(),
                                    trigger.getContextId());
		                }
	                }
                }

                // BADGE_KEY_correct_answer_streak ?
                isCreateBadge = false;
                isPlayerHasBadge = false;
                if (eventTransfer.getIsQuestionWon() || eventTransfer.getIsRoundWon()){
                	ArrayList<Integer> recordSet = dao.getSubscriberQuestionStreak(eventTransfer.getSubscriberId());
	                Integer streak = 0;
	                for (Integer record : recordSet)
	                	if (record == 1) streak++; else break;

	                if (streak >= 0){
	                	String badgeKey = IShoutContestAwardService.BADGE_KEY_correct_answer_streak + "_" + streak;
	                	boolean isValidBadgeKey = getIsValidBadgeKey(badgeKey);
	                	if (isValidBadgeKey){
	                		if (badges.size() > 0)
	                			isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(badgeKey));
	                		if (!isPlayerHasBadge)
	                			isCreateBadge = true;
	                    }
		                if (isCreateBadge){
		                    GameBadge gameBadge = new GameBadge();
		                    gameBadge.setContextId(eventTransfer.getContextId());
		                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
		                    gameBadge.setAssociationId(eventTransfer.getContextId());
		                    gameBadge.setEventTypeKey(trigger.getKey());
		                    gameBadge.setBadgeKey(badgeKey);
		                    eventTransfer.setGameBadge(gameBadge);
		                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
		                    triggerService.enqueue(
		                            badgeKey,
		                            eventTransferJson,
		                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
		                            trigger.getSource(),
		                            trigger.getBundleIds(),
                                    trigger.getContextId());
		                }
	                }
                }

                // BADGE_KEY_player_won_first_round ?
                isCreateBadge = false;
        		isPlayerHasBadge = false;
                if (eventTransfer.getIsRoundWon()){
            		if (badges.size() > 0)
            			isPlayerHasBadge = badges.stream().anyMatch(n -> n.getBadgeKey().equals(IShoutContestAwardService.BADGE_KEY_player_won_first_round));
            		if (!isPlayerHasBadge)
            			isCreateBadge = true;
	                if (isCreateBadge){
	                    GameBadge gameBadge = new GameBadge();
	                    gameBadge.setContextId(eventTransfer.getContextId());
	                    gameBadge.setSubscriberId(eventTransfer.getSubscriberId());
	                    gameBadge.setAssociationId(eventTransfer.getContextId());
	                    gameBadge.setEventTypeKey(trigger.getKey());
	                    gameBadge.setBadgeKey(IShoutContestAwardService.BADGE_KEY_player_won_first_round);
	                    eventTransfer.setGameBadge(gameBadge);
	                    String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
	                    triggerService.enqueue(
	                            IShoutContestAwardService.BADGE_KEY_player_won_first_round,
	                            eventTransferJson,
	                            IShoutContestAwardService.TRIGGER_SERVICE_ROUTE,
	                            trigger.getSource(),
	                            trigger.getBundleIds(),
                                trigger.getContextId());
	                }
                }


                //
                // TODO: additional badges for winning $ and winning games
                //


                return true;

            } catch(Exception e){
                logger.error(MessageFormat.format("e. EventProcessorQuestion trigger key {0} EXCEPTION:{1} ", trigger.getKey(), e.getMessage()));
                logger.error(Arrays.toString(e.getStackTrace()));
            }
            return false;
        }
    }

    public class EventProcessorBadge
    {
        private final List<String> validTriggerKeys = Arrays.asList(
        		 IShoutContestAwardService.BADGE_KEY_user_account_updated
        		,IShoutContestAwardService.BADGE_KEY_user_account_profile_picture
        		,IShoutContestAwardService.BADGE_KEY_player_played_first_round
        		,IShoutContestAwardService.BADGE_KEY_player_won_first_round
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_5
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_10
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_15
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_25
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_50
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_100
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_200
        		,IShoutContestAwardService.BADGE_KEY_player_rounds_played_500
        		,IShoutContestAwardService.BADGE_KEY_player_cash_level_attained
        		,IShoutContestAwardService.BADGE_KEY_player_cash_awarded
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak_5
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak_10
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak_15
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak_20
        		,IShoutContestAwardService.BADGE_KEY_correct_answer_streak_25
       );

        private boolean getIsMyTrigger(String triggerKey){
        	return validTriggerKeys.contains(triggerKey);
        }

        public boolean handleTrigger(Trigger trigger, GameInteractionEvent.IXPAndPointsPersister xpGiver )
        {
            logger.info(MessageFormat.format(">>> EventProcessorBadge.handleTrigger trigger:{0}", trigger.getKey()));
            if (trigger == null || trigger.getPayload() == null) {
                return true;
            }

            if (!this.getIsMyTrigger(trigger.getKey()))
            	return false;

            GameBadge badge = null;
            GameInteractionEvent event = null;
            EventTransferPayload eventTransfer = null;
            String payload = (String)trigger.getPayload();

            try {
                eventTransfer = EventTransferPayload.getFromString(payload);
                badge = eventTransfer.getGameBadge();
            } catch (Exception e) {
            	try{
            		badge = jsonMapper.readValue(payload, new TypeReference<GameBadge>(){});
            	} catch(Exception ee){
                    logger.error(MessageFormat.format("a. EventProcessorBadge.{0}, {1}", trigger.getKey(), e.getMessage()));
                    logger.error(Arrays.toString(e.getStackTrace()));
                    return false;
            	}
            }

            switch(trigger.getKey()){
	            case IShoutContestAwardService.BADGE_KEY_user_account_updated        :
	            case IShoutContestAwardService.BADGE_KEY_user_account_profile_picture:
	            case IShoutContestAwardService.BADGE_KEY_player_played_first_round   :
	            case IShoutContestAwardService.BADGE_KEY_player_won_first_round      :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_5      :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_10     :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_15     :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_25     :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_50     :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_100    :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_200    :
	            case IShoutContestAwardService.BADGE_KEY_player_rounds_played_500    :
	            case IShoutContestAwardService.BADGE_KEY_player_cash_level_attained  :
	            case IShoutContestAwardService.BADGE_KEY_player_cash_awarded         :
	            case IShoutContestAwardService.BADGE_KEY_correct_answer_streak_5     :
	            case IShoutContestAwardService.BADGE_KEY_correct_answer_streak_10    :
	            case IShoutContestAwardService.BADGE_KEY_correct_answer_streak_15    :
	            case IShoutContestAwardService.BADGE_KEY_correct_answer_streak_20    :
	            case IShoutContestAwardService.BADGE_KEY_correct_answer_streak_25    :

                    try {
                        // Persist the badge event
                    	if (eventTransfer != null)
                    		event = eventProcessorHelper.getEventFromBadge(trigger.getContextId(), trigger, badge, eventTransfer.getReceivedPayload());
                    	else
                    		event = eventProcessorHelper.getEventFromBadge(trigger.getContextId(), trigger, badge, payload);
                    	dao.addGameInteractionEvent(event);

                        // Persist the badge
                        dao.addGameBadge(badge);

                        // Fetch the badge just created - to get the details for notification
                        badge = dao.getBadgeForGameBadgeId(badge.getGameBadgeId());

                        // Send PERSISTED notification
                        try{
                        	// TODO: eventTransfer may be null!!!
                            eventTransfer.setAssociationDescription(badge.getBadgeDescription());
                            eventTransfer.setMessage(badge.getBadgeName());
                            HashMap<String, Object> badgeMap = jsonMapper.readValue(event.getDeliveredPayload(), new TypeReference<HashMap<String, Object>>() {});
                            eventTransfer.setExtrasMap(badgeMap);
                            eventProcessorHelper.sendPersistedNotification(trigger.getBundleIds(), eventTransfer);
                        } catch (NotificationException e) {
                            logger.error(MessageFormat.format("b. EventProcessorBadge.{0}, {1}", trigger.getKey(), e));
                            logger.error(Arrays.toString(e.getStackTrace()));
                        }

                    } catch (Exception e) {
                        logger.error(MessageFormat.format("c. EventProcessorBadge.{0}, {1}", trigger.getKey(), e));
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }

                    return true;
                case "":
                    logger.error(MessageFormat.format("d. EventProcessorBadge !!EMPTY!! trigger key {0}", ""));
                    break;
                default:
                    break;
                }
            return false;
        }
    }
}

/* ARCHIVE
 *
 * NOTE - 12/9/2016 - we no longer receive these events from numbase ...
case IShoutContestAwardService.GAME_EVENT_KEY_question       :
    /// extras = {
    ///   "round_id":"03962a8d-58b1-4d99-a7e6-c41a17e22722",
    ///   "game_id":4,
    ///   "message":"question",
    ///   "question":{
    ///      "languages":{},
    ///      "description":"How many rings are there in the Olympic Games symbol? ",
    ///      "answers":[
    ///         {"languages":{},"id":520,"description":"8"},
    ///         {"languages":{},"id":518,"description":"6"},
    ///         {"languages":{},"id":517,"description":"5"},
    ///         {"languages":{},"id":519,"description":"7"}
    ///      ],
    ///      "id":246
    ///   },
    ///   "level_id":61
    /// }
    try {
        event = EventProcessorHelper.getEventFromEventTransferPayload(eventTransfer);
        event.setDeliveredPayload(jsonMapper.writeValueAsString(extrasMap));
        event.setIsNotification(true);
        eventTransfer.setMessage("A new question"); // TODO: resource language
        if (extrasMap.containsKey("question")){
            HashMap<String, Object> questionMap = (HashMap<String, Object>)extrasMap.get("question");
            if (questionMap.containsKey("id")){
                event.setAssociationId((Integer)questionMap.get("id"));
            }
            if (questionMap.containsKey("description")){
                eventTransfer.setMessage((String)questionMap.get("description"));
            }
        }
        // Persist event
        dao.addGameInteractionEvent(event);

        // Send Push Notification
        // TODO: do NOT send transients for question or round_started YET, 8/21/2016
        //EventProcessorHelper.sendTransientNotification(pushService, eventTransfer);

    } catch(Exception e){
        logger.error(MessageFormat.format("a2. EventProcessorGame.{0}, {1}", trigger.getKey(), e.getMessage()));
        logger.error(Arrays.toString(e.getStackTrace()));
    }
    isHandledTrigger = true;
    break;

case IShoutContestAwardService.GAME_EVENT_KEY_question_result:
    /// extras= {
    ///   "correct_answer_id":565,
    ///   "question_result":{
    ///      "me":{"status":"won","subscriber_id":"4303","reason":"correctness","questions_status":["won"],"answer_id":517},
    ///      "opponent":{"status":"lost","subscriber_id":"4316","reason":"correctness","time":14555.038,"questions_status":["lost"],"answer_id":518}
    ///   },
    ///   "round_id":"03962a8d-58b1-4d99-a7e6-c41a17e22722",
    ///   "level_id":61,
    ///   "game_id":4,
    ///   "message":"question_result",
    ///   "question_id":258,
    ///   "more_questions":true
    /// }
    try {
        HashMap<String, Object> questionResultMap = null;
        if (extrasMap.containsKey("question_result"))
            questionResultMap = (HashMap<String, Object>)extrasMap.get("question_result");
        if (questionResultMap.containsKey("me"))
            meMap = (HashMap<String, Object>)questionResultMap.get("me");
        if (meMap.containsKey("status"))
            meStatus  = (String)meMap.get("status");
        if (questionResultMap.containsKey("opponent"))
            opponentMap = (HashMap<String, Object>)questionResultMap.get("opponent");
        if (opponentMap.containsKey("status"))
            opponentStatus  = (String)opponentMap.get("status");
    } catch(Exception e){
        logger.error(MessageFormat.format("a52. EventProcessorGame.{0}, {1}", trigger.getKey(), e.getMessage()));
        logger.error(Arrays.toString(e.getStackTrace()));
    }

    try{
        //
        // Extract the precise event type and trigger that
        //
        String questionResultQuestionKey = null;
        switch(meStatus){
            case "timeout":
                if (opponentStatus == "timeout")
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_lost_both_timeout;
                else
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_lost_timeout;
                break;
            case "won":
                if (opponentStatus == "timeout")
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_won_correct;
                else if(opponentStatus == "time")
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_won_faster;
                else
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_won_correct;
                break;
            case "lost":
                if (opponentStatus == "timeout")
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_lost_both_timeout;
                else if(opponentStatus == "time")
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_lost_slower;
                else
                    questionResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_question_lost_wrong;
                break;
        }

        // Create the trigger, this will chain-react downstream...
        logger.info(MessageFormat.format("EventProcessorGame.{0}, {1}", trigger.getKey(), " >>> enqueuing question result trigger..."));
        eventTransfer.setEventTypeKey(questionResultQuestionKey);
        String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
        triggerService.enqueue(questionResultQuestionKey, eventTransferJson, IShoutContestAwardService.TRIGGER_SERVICE_ROUTE, trigger.getSource());
    } catch (Exception e) {
        logger.error(MessageFormat.format("a4. EventProcessorGame.{0}, {1}", trigger.getKey(), e));
        logger.error(Arrays.toString(e.getStackTrace()));
    }

    isHandledTrigger = true;
    break;

case IShoutContestAwardService.GAME_EVENT_KEY_round_started  :
    /// extras={
    ///     "message": "user_matched",
    ///     "retry_round": false,
    ///     "user": {
    ///         "username": "I_AM_GROOT_ATHENA",
    ///         "profile": {
    ///             "lang": "en", "profile_pic": "",
    ///             "country": {"flag": "United-States.png", "name": "United States"}
    ///         },
    ///         "first_name": "",
    ///         "last_name": "",
    ///         "subscriber_id": "1588406",
    ///         "email": "iamgrootathena@email.com"
    ///     }
    /// },
    try {
        eventTransfer.setDeliveredPayload(jsonMapper.writeValueAsString(extrasMap));
        eventTransfer.setIsNotification(true);
        eventTransfer.setMessage("You've been matched"); // TODO: resource language
        if (extrasMap.containsKey("user")){
            HashMap<String, Object> userMap = (HashMap<String, Object>)extrasMap.get("user");
            if (userMap.containsKey("subscriber_id")){
                eventTransfer.setAssociationId(Integer.parseInt((String)userMap.get("subscriber_id")));
            }
            if (userMap.containsKey("email")){
                eventTransfer.setAssociationDescription((String)userMap.get("email"));
            }
            if (userMap.containsKey("username")){
                eventTransfer.setMessage(MessageFormat.format("You've been matched with ", (String)userMap.get("username")));
            }
        }
        // Persist event
        event = EventProcessorHelper.getEventFromEventTransferPayload(eventTransfer);
        dao.addGameInteractionEvent(event);

        // Send Push Notification
        // TODO: do NOT send transients for question or round_started YET, 8/21/2016
        //EventProcessorHelper.sendTransientNotification(pushService, eventTransfer);

    } catch(Exception e){
        logger.error(MessageFormat.format("a4. EventProcessorGame.{0}, {1}", trigger.getKey(), e.getMessage()));
        logger.error(Arrays.toString(e.getStackTrace()));
    }

    isHandledTrigger = true;
    break;

case IShoutContestAwardService.GAME_EVENT_KEY_round_ended    :
    /// "extras":"{
    ///     "round_id": "ea45c079-1efe-445c-b9dd-439b84c084d0",
    ///     "level_id": 61,
    ///     "game_id": 4,
    ///     "message": "level_result",
    ///     "question_results": [
    ///       {
    ///         "me": {"status": "won", "subscriber_id": "1544326", "reason": "response_time", "time": 9273.185, "questions_status": ["won"], "answer_id": 596 },
    ///         "opponent": {"status": "lost", "subscriber_id": "1584111", "reason": "response_time", "time": 10042.895, "questions_status": ["lost"], "answer_id": 596}
    ///       },
    ///       {
    ///         "me": {"status": "won", "subscriber_id": "1544326", "reason": "correctness", "time": 4880.723, "questions_status": ["won", "won"], "answer_id": 471},
    ///         "opponent": {"status": "lost", "subscriber_id": "1584111", "reason": "correctness", "time": 5904.617, "questions_status": ["lost", "lost"], "answer_id": 472}
    ///       }
    ///     ],
    ///     "level_result": {
    ///         "me": {"status": "won", "questions_status": ["won", "won"], "subscriber_id": "1544326", "level_prize": 0},
    ///         "opponent": {"status": "lost", "questions_status": ["lost", "lost"], "subscriber_id": "1584111"}
    ///     }
    /// }"

    HashMap<String, Object> roundResultMap = null;
    try {
        if (extrasMap.containsKey("level_result"))
            roundResultMap = (HashMap<String, Object>)extrasMap.get("level_result");
        if (roundResultMap.containsKey("me"))
            meMap = (HashMap<String, Object>)roundResultMap.get("me");
        if (meMap.containsKey("status"))
            meStatus  = (String)meMap.get("status");
        if (roundResultMap.containsKey("opponent"))
            opponentMap = (HashMap<String, Object>)roundResultMap.get("opponent");
        if (opponentMap.containsKey("status"))
            opponentStatus  = (String)opponentMap.get("status");
    } catch(Exception e){
        logger.error(MessageFormat.format("a5. EventProcessorGame.{0}, {1}", trigger.getKey(), e.getMessage()));
        logger.error(Arrays.toString(e.getStackTrace()));
    }

    try{
        //
        // Extract the precise event type and trigger that
        //
        String roundResultQuestionKey = null;
        switch(meStatus){
            case "timeout":
                if (opponentStatus == "timeout")
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_lost_both_timeout;
                else
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_lost_timeout;
                break;
            case "won":
                if (opponentStatus == "timeout")
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_won_correct;
                else if(opponentStatus == "time")
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_won_faster;
                else
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_won_correct;
                break;
            case "lost":
                if (opponentStatus == "timeout")
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_lost_both_timeout;
                else if(opponentStatus == "time")
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_lost_slower;
                else
                    roundResultQuestionKey = IShoutContestAwardService.QUESTION_KEY_round_lost_wrong;
                break;
        }

        // Create the trigger, this will chain-react downstream...
        logger.info(MessageFormat.format("EventProcessorGame.{0}, {1}", trigger.getKey(), " >>> enqueuing round result trigger..."));
        eventTransfer.setEventTypeKey(roundResultQuestionKey);
        String eventTransferJson = jsonMapper.writeValueAsString(eventTransfer);
        triggerService.enqueue(roundResultQuestionKey, eventTransferJson, IShoutContestAwardService.TRIGGER_SERVICE_ROUTE, trigger.getSource());
    } catch (Exception e) {
        logger.error(MessageFormat.format("a6. EventProcessorGame.{0}, {1}", trigger.getKey(), e));
        logger.error(Arrays.toString(e.getStackTrace()));
    }

    isHandledTrigger = true;
    break;
*/
