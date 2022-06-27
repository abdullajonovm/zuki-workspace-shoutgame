package com.meinc.push.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.meinc.push.domain.SubscriberToken;

public class FcmPushHandler
{
    private static Logger _logger = Logger.getLogger(FcmPushHandler.class);
    private GcmDelegate _delegate;
    private RetryRunner _runner;

    public FcmPushHandler(GcmDelegate delegate)
    {
        _delegate = delegate;

        _runner = new RetryRunner();
        _runner.start();
    }

    public void stop()
    {
        _runner.interrupt();
    }

    public void push(List<SubscriberToken> tokens, String payload, boolean priorityMessage,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    {
        for (SubscriberToken token : tokens) {
            String languageCode = subIdToLanguageCodeMap.get(token.getSubscriberId());
            if (languageCode == null) languageCode = "en";
            sendPush(token, payload, priorityMessage, languageCodeToNotificationTitleMap.get(languageCode), languageCodeToNotificationBodyMap.get(languageCode)
                    );
        }
    }

    private void sendPush(SubscriberToken token, String payload, boolean priorityMessage, String notificationTitle, String notificationBody)
    {
        String registrationToken = token.getDeviceToken();

        Message message;
        if (notificationTitle != null && notificationBody != null) {
            message = Message.builder()
                .setToken(registrationToken)
                .setNotification(new Notification(notificationTitle, notificationBody))
                .putData("payload", payload)
                .build();
        } else {
            message = Message.builder()
                .setToken(registrationToken)
                .putData("payload", payload)
                .build();
        }

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            if (_logger.isDebugEnabled()) {
                _logger.debug(MessageFormat.format("successfully sent message: {0}", response));
            }
            clearAnyRetries(token, payload);

        } catch (FirebaseMessagingException e) {
            switch (e.getErrorCode())
            {
                case "messaging/invalid-recipient":
                case "messaging/invalid-registration-token":
                case "messaging/registration-token-not-registered":
                case "messaging/invalid-package-name":
                    _logger.warn(MessageFormat.format("unable to send FCM message: {0}: {1}", e.getErrorCode(), e.getMessage()));
                    clearAnyRetries(token, payload);
                    _delegate.messageSendFailedGcm(token.getDeviceUuid());
                    break;

                case "messaging/payload-size-limit-exceeded": //max size is 4k
                    //this shouldn't happen since payload size is handled elsewhere
                    _logger.warn(MessageFormat.format("unable to send FCM message: {0}: {1}", e.getErrorCode(), e.getMessage()));
                    clearAnyRetries(token, payload);
                    break;

                case "messaging/message-rate-exceeded":
                case "messaging/device-message-rate-exceeded":
                case "messaging/topics-message-rate-exceeded":
                    _logger.warn(MessageFormat.format("unable to send FCM message: {0}. retry? {1}", e.getErrorCode(), priorityMessage));
                    if (priorityMessage) {
                        retry(token, payload, priorityMessage, notificationTitle, notificationBody);
                    } else {
                        clearAnyRetries(token, payload);
                    }
                    break;

                case "messaging/server-unavailable":
                case "messaging/internal-error":
                    _logger.warn(MessageFormat.format("unable to send FCM message: {0}. retry? {1}", e.getErrorCode(), true));
                    //Honor the Retry-After header if it is included in the response
                    //but ... what Retry-After header? in fact, what headers at all? i don't see how i have access to any headers
                    retry(token, payload, priorityMessage, notificationTitle, notificationBody);
                    break;

                //none of these should happen if the system is configured properly
                case "messaging/invalid-argument":
                case "messaging/invalid-payload ":
                case "messaging/invalid-data-payload-key":
                case "messaging/invalid-options":
                case "messaging/too-many-topics":
                case "messaging/invalid-apns-credentials":
                case "messaging/mismatched-credential":
                case "messaging/authentication-error":
                case "messaging/unknown-error":
                default:
                    _logger.warn(MessageFormat.format("unable to send FCM message: {0}: {1}", e.getErrorCode(), e.getMessage()));
                    clearAnyRetries(token, payload);
                    break;
            }
        }
    }

    private Map<String, Integer> _retryTimesMap = new HashMap<>();

    private void retry(SubscriberToken token, String payload, boolean priorityMessage, String notificationTitle, String notificationBody)
    {
        //generate a key based on the token
        String key = getKeyFromToken(token, payload);

        //add or update the max # of tries so far for this "key" (set object)
        Integer numRetries = _retryTimesMap.get(key);
        if (numRetries == null) numRetries = 1;
        numRetries++;
        _retryTimesMap.put(key, numRetries);

        //determine the backoffTime based on the # of tries (exponential backoff)
        long exponentialBackoffDelay = (long) (Math.pow(2D, numRetries-1)) * 1000L;
        long randomDelay = numRetries > 1 ? new Random().nextInt(3_000) : 0L;

        //create a "retry" object: key (based off the token somehow), token/payload/priorityMessage/randomDelay+backoffTime
        RetryStruct r = new RetryStruct(token, payload, priorityMessage, notificationTitle, notificationBody, exponentialBackoffDelay + randomDelay);

        //add retry object to the retryQueue
        try {
            _retryQueue.put(r);
        } catch (InterruptedException e) {
            //nothing to be done
            _logger.warn("unable to retry FCM message, queue put was interrupted");
        }
    }

    private String getKeyFromToken(SubscriberToken token, String payload)
    {
        String p = payload.length() > 50 ? payload.substring(0, 50) : payload;
        return (token.getDeviceUuid() + token.getDeviceToken() + p).hashCode() + "";
    }

    private void clearAnyRetries(SubscriberToken token, String payload)
    {
        String key = getKeyFromToken(token, payload);
        _retryTimesMap.remove(key);
    }

    private ArrayBlockingQueue<RetryStruct> _retryQueue = new ArrayBlockingQueue<>(5);

    private class RetryRunner
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    RetryStruct retryStruct = _retryQueue.take(); //block until input is available

                    //FUTURE: rather than use threads, consider coroutines (Kotlin?) or some other lightweight threading mechanism (Quasar?) to schedule these retries
                    new Timer().schedule(new RetryTimerTask(retryStruct), retryStruct.delay);
                }
            } catch (InterruptedException e) { }
        }
    }

    private class RetryTimerTask
    extends TimerTask
    {
        private RetryStruct retryStruct;

        public RetryTimerTask(RetryStruct retryStruct)
        {
            this.retryStruct = retryStruct;
        }

        @Override
        public void run()
        {
            sendPush(retryStruct.token, retryStruct.payload, retryStruct.priorityMessage, retryStruct.notificationTitle, retryStruct.notificationBody);
        }
    }

    private static class RetryStruct
    {
        public SubscriberToken token;
        public String payload;
        public boolean priorityMessage;
        public String notificationTitle;
        public String notificationBody;
        public long delay;

        public RetryStruct(SubscriberToken token, String payload, boolean priorityMessage, String notificationTitle, String notificationBody, long delay)
        {
            this.token = token;
            this.payload = payload;
            this.priorityMessage = priorityMessage;
            this.notificationTitle = notificationTitle;
            this.notificationBody = notificationBody;
            this.delay = delay;
        }
    }

}
