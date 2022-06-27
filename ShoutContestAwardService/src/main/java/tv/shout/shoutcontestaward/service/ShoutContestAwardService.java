package tv.shout.shoutcontestaward.service;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.service.IIdentityService;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.notification.service.INotificationService;
import com.meinc.trigger.domain.Trigger;
import com.meinc.trigger.service.ITriggerService;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.Payout;
import com.paypal.api.payments.PayoutBatch;
import com.paypal.api.payments.PayoutItem;
import com.paypal.api.payments.PayoutItemDetails;
import com.paypal.api.payments.PayoutSenderBatchHeader;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import tv.shout.shoutcontestaward.dao.IShoutContestAwardServiceDao;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.GameBadge;
import tv.shout.shoutcontestaward.domain.GameInteractionEvent.SubscriberStats;
import tv.shout.shoutcontestaward.domain.GamePayout;
import tv.shout.shoutcontestaward.eventprocessor.EventProcessorFilter;
import tv.shout.util.DateUtil;
import tv.shout.util.JsonUtil;

@Service(
    namespace=       IShoutContestAwardService.SERVICE_NAMESPACE,
    name=            IShoutContestAwardService.SERVICE_NAME,
    interfaces=      IShoutContestAwardService.SERVICE_INTERFACE,
    version=         IShoutContestAwardService.SERVICE_VERSION,
    exposeAs=        IShoutContestAwardService.class
)
public class ShoutContestAwardService
implements IShoutContestAwardService
{
    private static Logger _logger = Logger.getLogger(ShoutContestAwardService.class);

    private ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    @Autowired
    private IShoutContestAwardServiceDao _dao;

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private IIdentityService _identityService;

    @Autowired
    private INotificationService _notificationService;

    @Autowired
    private EventProcessorFilter _eventProcessorFilter;

//TODO: this should be in the property file...
    public class PayPalSampleConstants {
        public static final String clientID = "AYSq3RDGsmBLJE-otTkBtM-jBRd1TCQwFf9RGfwddNXWz0uFU9ztymylOhRS";
        public static final String clientSecret = "EGnHDxD_qRPdaLdZz8iCr8N7_MzF-YHPTkjs6NKYQvQSBngp4PTTVWkPZRbL";
        public static final String mode = "sandbox";
    }

    @Override
    @ServiceMethod
    @OnStart
    public void start()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestAwardService starting...");
        }

        ServiceEndpoint thisServiceEndpoint = new ServiceEndpoint(IShoutContestAwardService.SERVICE_NAMESPACE, IShoutContestAwardService.SERVICE_NAME, IShoutContestAwardService.SERVICE_VERSION);
        ServiceEndpoint triggerServiceEndpoint = new ServiceEndpoint("meinc-service", "TriggerService", "1.0");

        //wait for the trigger service and then register for callbacks
        ServiceMessage.waitForServiceRegistration(triggerServiceEndpoint);
        _triggerService.registerCallback(thisServiceEndpoint, "processTriggerMessages", IShoutContestAwardService.TRIGGER_SERVICE_ROUTE);

//#@)(@#$()*#$ nope....
//        //this will cause the GamePlayService registration to load which will populate the AppHelper's app map
//        ServiceMessage.waitForServiceRegistration(new ServiceEndpoint("default", "GamePlayService", "3.0"));

        //wait for the notification service to load and then register for callbacks
        ServiceMessage.waitForServiceRegistration(new ServiceEndpoint("meinc-service", "NotificationService", "1.0"));
        _notificationService.addNotificationType(NOTIFICATION_TYPE_ROUND_ENDED, 1);
        _notificationService.setNotificationCategory(NOTIFICATION_TYPE_ROUND_ENDED, "roundended");

        _logger.info("ShoutContestAwardService started");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void stop()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestAwardService stopping...");
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("ShoutContestAwardService stopped");
        }
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public boolean processTriggerMessages(Trigger trigger)
    {
        _eventProcessorFilter.process(trigger);
        return false;
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public List<SubscriberStats> getSubscriberStats(int subscriberId) {
        // TODO
        return _dao.getSubscriberStats(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public List<GameBadge> getBadgesForSubscriber(int subscriberId) {
        // TODO
        return _dao.getBadgesForSubscriber(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public List<GameInteractionEvent> getEventsForSubscriber(int subscriberId) {
        // TODO
        return _dao.getEventsForSubscriber(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public List<GamePayout> getGamePayoutsForSubscriber(int subscriberId) {
        // TODO
        return _dao.getGamePayoutListForSubscriberId(subscriberId);
    }

    @Override
    @Transactional(propagation=NESTED)
    @ServiceMethod
    public void redeemGamePayout(int gamePayoutId) throws Exception {
        GamePayout gamePayout = null;
        try{
            // TODO
            // Get the game payout for this id
            gamePayout = _dao.getGamePayoutForId(gamePayoutId);

            // Test exists
            if (gamePayout == null){
                throw new Exception(MessageFormat.format("{0} {1}", "redeemGamePayout", "GamePayout record missing."));
            }

            switch(gamePayout.getPayoutStatus()){
                case NEW:
                    this.createSynchronousSinglePayout(gamePayout);
                    break;
                case INPROCESS:
                case INREVIEW:
                case UNCLAIMED:
                    this.updateSynchronousSinglePayout(gamePayout);
                    break;
                case DENIED:
                    throw new Exception(MessageFormat.format("{0} {1} {2}", "redeemGamePayout", "payoutDenied", gamePayout.getPayoutDescription()));
                case PAID:
                    throw new Exception(MessageFormat.format("{0} {1} {2}", "redeemGamePayout", "previouslyPaid", DateUtil.dateToIso8601(gamePayout.getFinalizedDate())));
            }
        }finally{
            if (gamePayout != null)
                _dao.updateGamePayout(gamePayout);
        }

    }

    @Override
    @ServiceMethod
    public GamePayout createGamePayout( int contextId, Integer subscriberId, String gameId, String roundId, String roundSequence, Double amount) {

        GamePayout gamePayout = new GamePayout();
        gamePayout.setContextId(contextId);
        gamePayout.setPrizeKey(UUID.randomUUID().toString());
        gamePayout.setGameId(gameId);
        gamePayout.setLevelId(roundId);
        gamePayout.setLevelNumber(roundSequence);
        gamePayout.setPayoutRequestAmount(amount);
        gamePayout.setSubscriberId(subscriberId);
        Subscriber subscriber = _identityService.getSubscriberById(subscriberId);
        String subscriberEmail = subscriber.getEmail();
        gamePayout.setPayoutEmail(subscriberEmail);
        gamePayout.setPayoutChannel(GamePayout.PayoutChannelEnum.EMAIL);
        gamePayout.setPayoutCurrency(GamePayout.PayoutCurrencyEnum.USD);
        gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.NEW);
        gamePayout.setCreatedDate(new Date());
        return gamePayout;
    }

    /*
     * Internal: sends the payout request through PayPal.
     * */
    private void createSynchronousSinglePayout(GamePayout gamePayout) throws Exception {

        // Payout Amount
        Currency payoutAmount = new Currency();
        payoutAmount.setValue(gamePayout.getPayoutRequestAmount() + "");
        payoutAmount.setCurrency(gamePayout.getPayoutCurrency() + "");

        // Payout Item
        // Please note that if you are using single payout with sync mode, you
        // can only pass one Item in the request
        PayoutItem payoutItem = new PayoutItem();
        payoutItem.setRecipientType(gamePayout.getPayoutChannel() + "");
        payoutItem.setNote(gamePayout.getPayoutDescription());
        payoutItem.setReceiver(gamePayout.getPayoutEmail());
        payoutItem.setSenderItemId(gamePayout.getProcessorBatchKey());
        payoutItem.setAmount(payoutAmount);
        List<PayoutItem> items = new ArrayList<PayoutItem>();
        items.add(payoutItem);

        // NOTE:
        // You can prevent duplicate batches from being processed. If you
        // specify a `sender_batch_id` that was used in the last 30 days, the
        // batch will not be processed. For items, you can specify a
        // `sender_item_id`. If the value for the `sender_item_id` is a
        // duplicate of a payout item that was processed in the last 30 days,
        // the item will not be processed.

        // Batch Header Instance
        PayoutSenderBatchHeader senderBatchHeader = new PayoutSenderBatchHeader();
        senderBatchHeader.setEmailSubject(gamePayout.getPayoutDescription());
        senderBatchHeader.setSenderBatchId(gamePayout.getProcessorBatchKey());
        senderBatchHeader.setRecipientType(gamePayout.getPayoutChannel() + "");

        // Payout
        // A resource representing a payout
        Payout payout = new Payout();
        payout.setSenderBatchHeader(senderBatchHeader);
        payout.setItems(items);

        PayoutBatch batch = null;
        try {
            // Api Context
            // Pass in a `ApiContext` object to authenticate the call and to send a unique request id
            // (that ensures idempotency). The SDK generates a request id if you do not pass one explicitly.
            APIContext apiContext = new APIContext(
                    PayPalSampleConstants.clientID,
                    PayPalSampleConstants.clientSecret,
                    PayPalSampleConstants.mode);

            // Create Payout Synchronous
            // This should be a blocking call...
            batch = payout.createSynchronous(apiContext);

            // PayPal's batch status list:
            // CANCELLED    This status cannot occur if the sender uses the API to only send payouts. This status is an edge-case if a sender uses both the MassPay web upload and the Payouts API, cancels the web upload, and then uses the API to find the batch payout or payout items. Then, the CANCELLED status is possible.
            // DENIED   No batch payout items are processed.
            // NEW  The batch payout is delayed due to PayPal internal updates.
            // PENDING  The batch payout is waiting to be processed.
            // PROCESSING   The batch payout is being processed.
            // SUCCESS  The batch payout was successfully processed. Note that some batch payout items might not be completely processed because, for example, they are unclaimed or on hold.

            // PayPal's item status list:
            // BLOCKED   The payout item is blocked.
            // DENIED    The payout item was denied payment.
            // FAILED    Processing for the payout item failed.
            // NEW       The payment processing is delayed due to PayPal internal updates.
            // ONHOLD    The payout item is on hold.
            // PENDING   The payout item is awaiting payment.
            // REFUNDED  The payment for the payout item was successfully refunded.
            // RETURNED  The payout item is returned. If the recipient does not claim it in 30 days, the funds are returned.
            // SUCCESS   The payout item was successfully processed.
            // UNCLAIMED The payout item is unclaimed. If it is not claimed within 30 days, the funds are returned to the sender.

            // Record the PayoutBatchId for later use in status updates
            gamePayout.setPayoutKey(batch.getBatchHeader().getPayoutBatchId());
            List<PayoutItemDetails> itemDetails = batch.getItems();
            String itemStatus = itemDetails.get(0).getTransactionStatus();
            if (itemStatus == "UNCLAIMED")
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.UNCLAIMED);
            else if (itemStatus == "SUCCESS")
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.PAID);
            else
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.INPROCESS);
            gamePayout.setProcessorPayoutStatus(itemStatus);
            gamePayout.setPayoutDescription(Payout.getLastRequest()+ " : " + Payout.getLastResponse());

            _logger.info("Create Payout Batch With ID: " + batch.getBatchHeader().getPayoutBatchId());

        } catch (PayPalRESTException e) {
            try{
                gamePayout.setPayoutDescription(Payout.getLastRequest() + " : " + e.getMessage());
                gamePayout.setProcessorPayoutStatus("ERROR");
            }catch(Exception ex){
                _logger.error(MessageFormat.format("Created Single Synchronous Payout {0} : {1}", Payout.getLastRequest(), e.getMessage()));
                throw new Exception(MessageFormat.format("CreateSynchronousSinglePayout {0} : {1}", Payout.getLastRequest(), e.getMessage()));
            }
        }
    }

    /*
     * Internal: attempts to retrieve updated status of an existing payout transaction.
     * */
    private void updateSynchronousSinglePayout(GamePayout gamePayout) throws Exception {

        PayoutBatch batch = null;
        try {

            if (gamePayout.getPayoutKey() == null){
                _logger.info(MessageFormat.format("updateSynchronousSinglePayout gamePayout batch id null: gamePayoutId: {0}", gamePayout.getGamePayoutId()));
                throw new Exception(MessageFormat.format("updateSynchronousSinglePayout gamePayout batch id null: gamePayoutId: {0}", gamePayout.getGamePayoutId()));
            }

            // Api Context
            // Pass in a `ApiContext` object to authenticate the call and to send a unique request id
            // (that ensures idempotency). The SDK generates a request id if you do not pass one explicitly.
            APIContext apiContext = new APIContext(
                    PayPalSampleConstants.clientID,
                    PayPalSampleConstants.clientSecret,
                    PayPalSampleConstants.mode);

            // Create Payout Synchronous
            // This should be a blocking call...
            batch = Payout.get(apiContext, gamePayout.getPayoutKey());
            if (batch == null){
                _logger.info(MessageFormat.format("updateSynchronousSinglePayout batch null {0}", Payout.getLastRequest()));
                throw new Exception(MessageFormat.format("updateSynchronousSinglePayout batch null {0}", Payout.getLastRequest()));
            }

            // Update the payout records with the new data from the batch
            List<PayoutItemDetails> itemDetails = batch.getItems();
            String itemStatus = itemDetails.get(0).getTransactionStatus();
            if (itemStatus == "UNCLAIMED")
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.UNCLAIMED);
            else if (itemStatus == "SUCCESS")
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.PAID);
            else
                gamePayout.setPayoutStatus(GamePayout.PayoutStatusEnum.INPROCESS);
            gamePayout.setProcessorPayoutStatus(itemStatus);
            gamePayout.setPayoutDescription(Payout.getLastRequest()+ " : " + Payout.getLastResponse());

            _logger.info("Update Payout Batch With ID: " + batch.getBatchHeader().getPayoutBatchId());

        } catch (PayPalRESTException e) {
            try{
                gamePayout.setPayoutDescription(Payout.getLastRequest() + " : " + e.getMessage());
                gamePayout.setProcessorPayoutStatus("ERROR");
            }catch(Exception ex){
                _logger.error(MessageFormat.format("updateSynchronousSinglePayout {0} : {1}", Payout.getLastRequest(), e.getMessage()));
                throw new Exception(MessageFormat.format("updateSynchronousSinglePayout {0} : {1}", Payout.getLastRequest(), e.getMessage()));
            }
        }
    }

    /*
     * Internal: run this method on a scheduled chron job
     * */
    @Scheduled(cron="0 0 * * * *")//top of the hour
    private void scheduledUpdatePayoutStatusJob(){
        //
        // Fetch the open payout records from the database
        // For each payout that is in a state that is maleable:
        //   try to fetch that payout transaction
        //   and update it.
        //

        List<GamePayout> payouts = _dao.getGamePayoutListForOpenStatus();

        for(GamePayout gamePayout : payouts){
            try {
                updateSynchronousSinglePayout(gamePayout);
                _dao.updateGamePayout(gamePayout);
            } catch (Exception e) {
                _logger.error(MessageFormat.format("scheduledUpdatePayoutStatusJob {0}", Payout.getLastRequest()), e);
            }
            // Keep going on any error...
        }
    }
}
