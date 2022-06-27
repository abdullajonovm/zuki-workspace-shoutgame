package com.meinc.push.service;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.meinc.mrsoa.distdata.simple.DistributedMap;
import com.meinc.push.domain.SubscriberToken;

public class GcmPushHandler
{
    private static Logger _logger = Logger.getLogger(GcmPushHandler.class);
    //private static Logger _logger = Logger.getLogger(PushService.class);

    //how many pushes to sent at the same time (gcm currently supports up to 1000)
    private static final int MULTICAST_SIZE = 1000;

    private static final int NUM_RETRIES = 5;

    //thread pool for async sending of pushes
    private static final Executor threadPool = Executors.newFixedThreadPool(5);

    //thread pool for async sending of delayed (throttled) pushes
    private ScheduledExecutorService _scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private Sender _sender;
    private GcmDelegate _delegate;

    //keep track of when a push was last sent to a subscriber
    private static final long THROTTLE_DELAY_MS = 60000;
    private DistributedMap<String, Long> _subscriberPushTimeMap = DistributedMap.getMap("subscriberPushTimeMap");
    private PlatformTransactionManager _transactionManager;

    public void setApiKey(String apiKey)
    {
        _sender = new Sender(apiKey);
    }

    public void setDelegate(GcmDelegate delegate)
    {
        _delegate = delegate;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        _transactionManager = transactionManager;
    }

    /**
     * @param tokens which phones will receive the push
     * @param payload what to send to the phones
     * @param priorityMessage if true, this message will not be put on the throttled queue but go right through
     */
    public void push(List<SubscriberToken> tokens, String payload, boolean priorityMessage)
    {
        throw new UnsupportedOperationException("GCM push service isn't used anymore, so if it ever gets used again, fix this code");

        /*
        //one time initialization of the Distributed map that keeps track of when a push was last sent to a subscriber
        if (_subscriberPushTimeMap == null) {
            Properties props = ServerPropertyHolder.getProps();
            String instanceName = props.getProperty("shared.cluster.distdata.name");
            if (instanceName != null && instanceName.trim().length() > 0) {
                DistributedInstance distributedInstance = DistributedInstance.getInstance(instanceName);
                _subscriberPushTimeMap = distributedInstance.getMap("subscriberPushTimeMap");
            }
            else {
                // shouldn't get here, if it does web-collector is probably disabled
                _logger.warn("no value specified for 'shared.cluster.distdata.name', not throttling pushes");
            }
        }

        //break the tokens into two parts: one list of "good to go right now" and the rest as future scheduled items
        List<SubscriberToken> okToSendRightNowTokens = new ArrayList<SubscriberToken>();
        for (SubscriberToken token : tokens) {
            Long lastSendTimeForSubscriber = _subscriberPushTimeMap == null ? null : _subscriberPushTimeMap.get(token.getDeviceToken());
            if (
                priorityMessage ||
                lastSendTimeForSubscriber == null ||
                (lastSendTimeForSubscriber != null && lastSendTimeForSubscriber + THROTTLE_DELAY_MS <= System.currentTimeMillis())
               ) {
                //send the push right away, and mark a push as having been sent right now even though it will still be a moment
                // (prevents other pushes from coming in and not knowing something is about to be sent if a bunch come in rapid succession)
                //_logger.info("immediately sending push to: " + token.getSubscriberId() + ", ttl: " + THROTTLE_DELAY_MS + ", payload: " + payload);
                okToSendRightNowTokens.add(token);

                //put an entry into the map indicating that a push is about to be sent to this subscriber (with a 1 minute ttl)
                if (_subscriberPushTimeMap != null) {
                    if (lastSendTimeForSubscriber == null) {
//_logger.info("\t>>> setting lastSendTime for " + token.getSubscriberId() + ": " + (System.currentTimeMillis()));
                        _subscriberPushTimeMap.put(token.getDeviceToken(), System.currentTimeMillis(), THROTTLE_DELAY_MS, TimeUnit.MILLISECONDS);
                    } else {
                        //do nothing; scheduling of other things in the queue are already still in flight and no update necessary since we're not rescheduling things
                    }
                }

            } else {
                long delay = lastSendTimeForSubscriber + THROTTLE_DELAY_MS - System.currentTimeMillis();
                long ttl = delay;
//_logger.info(">>> delay sending push to: " + token.getSubscriberId() + "; delay/ttl: " + delay + " ms, which means push will fire at: " + (System.currentTimeMillis() + delay));
                _scheduledExecutorService.schedule(new FutureRunnableForThrottledPush(token, payload), delay, TimeUnit.MILLISECONDS);

                if (_subscriberPushTimeMap != null) {
//_logger.info("\t>>> setting expected lastSendTime for " + token.getSubscriberId() + ": " + (System.currentTimeMillis() + delay));
                    _subscriberPushTimeMap.put(token.getDeviceToken(), System.currentTimeMillis() + delay, ttl, TimeUnit.MILLISECONDS);
                }
            }
        }

        //send the "good to go right now" list same as before
        if (okToSendRightNowTokens.size() > 0) {
            doBatchSendOfPushes(okToSendRightNowTokens, payload);
        }
        */
    }

    class FutureRunnableForThrottledPush
    implements Runnable
    {
        private SubscriberToken _token;
        private String _payload;

        public FutureRunnableForThrottledPush(SubscriberToken token, String payload)
        {
            _token = token;
            _payload = payload;
        }

        @Override
        public void run()
        {
            //_logger.info("sending delayed push to: " + _token.getSubscriberId() + ", payload: " + _payload);
            doBatchSendOfPushes(Arrays.asList(new SubscriberToken[] {_token}), _payload);
        }
    }

    private void doBatchSendOfPushes(List<SubscriberToken> tokens, String payload)
    {
        //batch send the messages
        int total = tokens.size();
        List<String> partialDevices = new ArrayList<String>(total);
        int counter = 0;
        int tasks = 0;
        for (SubscriberToken token : tokens) {
            counter++;
            partialDevices.add(token.getDeviceToken());
            int partialSize = partialDevices.size();
            if (partialSize == MULTICAST_SIZE || counter == total) {
                asyncSend(partialDevices, payload);
                partialDevices.clear();
                tasks++;
            }
        }
        _logger.debug("asynchronously sending gcm multicast message to " + total + " devices in " + tasks + " batch(es)");
    }

    private void asyncSend(List<String> partialDevices, final String payload)
    {
        //copy the list for async sending
        final List<String> devices = new ArrayList<String>(partialDevices);

        //do async send
        threadPool.execute(new Runnable() {
            @Override
            public void run()
            {
                //build up the message
                Message message = new Message.Builder()
                    .addData("message", payload)
                    .collapseKey("1")
                    .timeToLive(3600)
                    .build();

                //send the message
                MulticastResult multicastResult;
                try {
//_logger.info(MessageFormat.format(">>> ANDROID push send to {0}, payload: {1}", devices, payload));
                    multicastResult = _sender.send(message, devices, NUM_RETRIES);
                } catch (IOException e) {
                    _logger.error("Unable to send Android GCM push", e);
                    return;
                }

                //analyze the results
                if (_transactionManager != null) {
                    TransactionDefinition txDef = new DefaultTransactionDefinition(PROPAGATION_NESTED);
                    TransactionStatus txStatus = _transactionManager.getTransaction(txDef);
                    try {
                        analyzeResults(multicastResult, devices);
                        _transactionManager.commit(txStatus);
                        txStatus = null;
                    } finally {
                        if (txStatus != null) {
                            _transactionManager.rollback(txStatus);
                            txStatus = null;
                        }
                    }

                } else {
                    analyzeResults(multicastResult, devices);
                }
            }
        });
    }

    private void analyzeResults(MulticastResult multicastResult, List<String> devices)
    {
        List<Result> results = multicastResult.getResults();
        for (int i=0; i<devices.size(); i++) {
            String regId = devices.get(i);
            Result result = results.get(i);
            String messageId = result.getMessageId();

            if (messageId != null) {
                _delegate.messageSentGcm(regId, messageId);

                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    //same device has more than one registration id; update it
                    _delegate.updateDeviceTokenGcm(regId, canonicalRegId);
                }

            } else {
                //some type of error
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    _delegate.messageSendFailedGcm(regId);

                } else {
                    //other types of errors are (pretty much) unrecoverable without manual intervention
                    _delegate.serverExceptionGcm(regId, error);
                }
            }
        }
    }

//    public static String getUserInput(String message)
//    {
//        InputStreamReader isr = new InputStreamReader(System.in);
//        BufferedReader br = new BufferedReader(isr);
//        System.out.print(message + " ");
//        String s=null;
//        try {
//            s = br.readLine();
//        } catch (IOException e) {
//            e.printStackTrace(System.err);
//        }
//        return s;
//    }
//
//    //https://firebase.google.com/docs/cloud-messaging/http-server-ref
//    //https://stackoverflow.com/questions/37426542/firebase-cloud-messaging-notification-from-java-instead-of-firebase-console
//    //  see answer by "aes", Jun 1, 2016
//    private void sendPush(String apiKey, String deviceToken)
//    throws IOException
//    {
//        Map<String, String> notificationMap = new HashMap<>();
//        notificationMap.put("title", "this is the notification title");
//        notificationMap.put("body", "this is the notification body");
//
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("key1", "value1");
//        dataMap.put("key2", "value2");
//
//        Map<String, Object> bodyMap = new HashMap<>();
//        bodyMap.put("to", deviceToken);
//        bodyMap.put("priority", "high");
//        bodyMap.put("notification", notificationMap);
//        bodyMap.put("data", dataMap);
//
//        ObjectMapper jsonMapper = JsonUtil.getObjectMapper();
//        String payload = jsonMapper.writeValueAsString(bodyMap);
//
//        //create the request
//        URL obj = new URL("https://fcm.googleapis.com/fcm/send");
//        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//        con.setRequestMethod("POST");
//
//        //add the headers
//        con.setRequestProperty("Authorization", "key=" + apiKey);
//        con.setRequestProperty("content-type", "application/json");
//
//        //send the data
//        con.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//        wr.writeBytes(payload);
//        wr.flush();
//        wr.close();
//
//        //get the response code
//        int responseCode = con.getResponseCode();
//System.out.println("response code: " + responseCode);
//
//        //read the response body
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuilder response = new StringBuilder();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//System.out.println(response.toString());
////
////        if (responseCode == 200) {
////            return response.toString();
////        } else {
////            throw new NetworkException(responseCode, con.getHeaderFields(), response.toString());
////        }
//    }
//
//    public static void main(String[] args)
//    throws Exception
//    {
//            GcmPushHandler push = new GcmPushHandler();
////            //String apiKey = "TODO";
////            String apiKey = getUserInput("API Key:");
////            push.setApiKey(apiKey);
////            push.setDelegate(new GcmDelegate() {
////                @Override
////                public void updateDeviceTokenGcm(String oldDeviceId, String newDeviceId)
////                {
////                    System.out.println(MessageFormat.format("updateDeviceTokenGcm, oldDeviceId: {0}, newDeviceId: {1}", oldDeviceId, newDeviceId));
////                    System.exit(0);
////                }
////                @Override
////                public void serverExceptionGcm(String deviceId, String error)
////                {
////                    System.out.println(MessageFormat.format("serverExceptionGcm, error: {0}, deviceId: {1}", error, deviceId));
////                    System.exit(0);
////                }
////                @Override
////                public void messageSentGcm(String deviceId, String messageId)
////                {
////                    System.out.println(MessageFormat.format("messageSentGcm, messageId: {0}, deviceId: {1}", messageId, deviceId));
////                    System.exit(0);
////                }
////                @Override
////                public void messageSendFailedGcm(String deviceId)
////                {
////                    System.out.println(MessageFormat.format("messageSendFailedGcm, deviceId: {0}", deviceId));
////                    System.exit(0);
////                }
////            });
////
////            SubscriberToken token = new SubscriberToken();
////            //String deviceToken = "TODO";
////            String deviceToken = getUserInput("Device Token:");
////            token.setDeviceToken(deviceToken);
////
//////            String payload = getUserInput("payload:");
////            String payload = "{ \"notificationId\":99, \"contextId\":0, \"type\":\"n\", \"notificationType\": \"COMMENT_MENTION\", \"aps\": { \"category\": \"mentioned\", \"alert\": \"hi\", \"sound\": \"default\" }, \"extras\": { \"voteId\": 99, \"voteUuid\": \"hi\", \"commentUuid\": \"hi\", \"question\": \"hi\", \"comment\": \"hi\", \"commentTruncated\": true } }";
////
////            push.push(Arrays.asList(token), payload, true);
////
//            push.sendPush(
//                    "AAAAO5n9Rnw:APA91bEix_98yIodvSBKfNB_C6-onnhIR8MbbDs-0b_EpgzYZ-RJBRyEPxMeKGivYIDjjGTQj2HEoaTBOd9dMFdUEQ7umziDt6AdX7D-7nL6BGt5TnNY-YxIXd91Zf0hzuKpBcl-Ex0w",
//                    "fWFdfQ-B6Jc:APA91bGBwEP3qS6eYViMdUfiUd3aaj2tabb0zwIk4J_n-Z9aVGKosbFa89T_o3rcCB19ZCxhm0An2zjR_oQt0kMqEygAyIbzgaN2FF8IbFl6APqWTd2CDRaZ5EcjpaJ3cET5ViqFcxro");
//    }
}
