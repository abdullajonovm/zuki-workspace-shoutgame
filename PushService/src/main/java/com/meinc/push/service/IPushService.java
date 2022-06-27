package com.meinc.push.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.meinc.push.exception.PayloadInvalidException;
import com.meinc.push.exception.PayloadTooLargeException;
import com.meinc.push.exception.PushNetworkIoException;

public interface IPushService {

    public static final String DEVICE_TYPE_IOS = "iOS";
    public static final String DEVICE_TYPE_ANDROID_C2DM = "Android";
    public static final String DEVICE_TYPE_ANDROID_GCM = "Android2";
    public static final String DEVICE_TYPE_ANDROID_FCM = "Android3";
    public static final String DEVICE_TYPE_WINDOWS_WNS = "Windows";

    public void load() throws FileNotFoundException;

    public void unload();

    /**
     * Push the values in msgValues to devices of the given subscriberId.
     * For iOS devices, to send an alert, sound or badge, msgValues should have a key named 'aps' that maps to another map, like so:
     *
     * <code>
     * Map<String,Object> msgValues = new HashMap<String,Object>();
     * Map<String,String> aps = new HashMap<String,Object>();
     * aps.put("alert", "This is an alert");
     * msgValues.put("aps", aps);
     * </code>
     *
     * @param msgValues the msg values to send
     * @param subscriberId
     * @param bundleIds TODO
     * @throws PayloadTooLargeException
     * @throws PayloadInvalidException TODO
     */
    public void pushNotificationToSubscriber(Map<String,Object> msgValues, long subscriberId, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException;

    /**
     * Sends the same push notification to the given list of subscribers.
     * @see #pushNotificationToSubscriber(Map, int, Set)
     * @param msgValues
     * @param idList
     * @param bundleIds TODO
     * @throws PayloadTooLargeException
     * @throws PayloadInvalidException
     */
    public void pushNotificationToSubscribers(Map<String,Object> msgValues,  List<Long> idList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException;

    /**
     * Push a simple message to the given list of subscribers. This should be a simple string, NOT a serialized JSON message
     * @param message
     * @param subscriberList
     * @param bundleIds TODO
     * @throws PayloadTooLargeException
     * @throws PayloadInvalidException
     */
    public void pushMessageToSubscribers(String message, List<Long> subscriberList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException;

    /**
     * Push the given payload to the given list of subscribers.  This should be a fully serialized JSON message, and will be sent as-is.
     *
     * @param payload
     * @param subscriberList
     * @param bundleIds TODO
     * @throws PayloadTooLargeException
     */
    public void pushPayloadToSubscribers(String payload, List<Long> subscriberList, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException;

    /**
     * Push the given payload to the given list of subscribers.  This should be a fully serialized JSON message, and will be sent as-is.
     *
     * @param payload
     * @param subscriberList
     * @param bundleIds TODO
     * @param priorityMessage if true, this message will not be throttled
     * @throws PayloadTooLargeException
     */
    public void pushPayloadToSubscribers(String payload, List<Long> subscriberList, Set<String> bundleIds, boolean priorityMessage,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException;

    /**
     * Register the given device token (in binary format) for the given subscriber and deviceType
     *
     * @param token
     * @param subscriberId
     * @param deviceType
     * @param appBundleId TODO
     */
    public void registerTokenForSubscriberAndDeviceType(byte[] token, long subscriberId, String deviceType, String deviceUuid, String appBundleId);

    /**
     * Unregister a device by its ID so it stops receiving push messages.
     *
     * @param subscriberId
     * @param deviceUuid
     */
    public void unregisterTokenForSubscriberDevice(long subscriberId, String deviceUuid);

    /**
     * Clear out any rows matching this deviceUuid except the row for this subscriber.
     *
     * @param subscriberId
     * @param deviceUuid
     */
    public void unregisterTokensForDeviceExceptForThisSubscriber(long subscriberId, String deviceUuid);

    /**
     * Register the given device token (in hex representation) for the given subscriber and deviceType
     *
     * @param token
     * @param subscriberId
     * @param deviceType
     * @param appBundleId TODO
     * @see #DEVICE_TYPE_IOS
     * @see #DEVICE_TYPE_ANDROID
     */
    public void registerHexTokenForSubscriberAndDeviceType(String token, long subscriberId, String deviceType, String deviceUuid, String appBundleId);

    /**
     * Get a list of subscriber who have pushes of the given type.
     *
     * @param type one of 'iOS' or 'Android'
     * @return
     */
    public List<Long> getSubscriberIdsByType(String type);

    public void pushMessageToDevices(Map<String,Object> msgValues, List<String> deviceUuids,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PayloadInvalidException, PayloadTooLargeException;

    public void pushPayloadToDevices(String payload, List<String> deviceUuids,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PayloadTooLargeException;

    boolean doesSubscriberHavePushTokens(int subscriberId);

    /**
     * Send a push notification to all players with a valid push token.
     * @param bundleIds TODO
     */
    public void sendAllPlayerPush(String message, Set<String> bundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws PushNetworkIoException, PayloadTooLargeException, PayloadInvalidException;

}
