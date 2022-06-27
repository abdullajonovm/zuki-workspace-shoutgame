package com.meinc.notification.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.notification.domain.Notification;
import com.meinc.notification.domain.NotificationPref;
import com.meinc.notification.exception.InvalidMessageException;
import com.meinc.notification.exception.InvalidNotifcationException;
import com.meinc.notification.exception.InvalidPrefException;
import com.meinc.notification.exception.NotificationAlreadyHandledException;
import com.meinc.notification.exception.NotificationDeletedException;

public interface INotificationService {

    public static final int EVENT_STATUS_CHANGED = 1;
    public static final int EVENT_ACTION_TAKEN = 2;

    public static final String ACTION_TYPE_NONE = "NONE";
    public static final String ACTION_TYPE_ACCEPT_REJECT = "ACCEPT_REJECT";
    public static final String ACTION_TYPE_YES_NO = "YES_NO";
    public static final String ACTION_TYPE_CUSTOM = "CUSTOM";

    public static final String ACTION_ACCEPTED = "ACCEPTED";
    public static final String ACTION_REJECTED = "REJECTED";
    public static final String ACTION_YES = "YES";
    public static final String ACTION_NO = "NO";

    /**
     * There is a pre-defined pref type of 1 (PREF_TYPE_GENERAL).
     * When assigning actions to pref types, it's up to the caller to assign meaning to what the numbers mean that they pass.
     */
    public static final int PREF_TYPE_GENERAL  = 1;

    public static final String COMM_TYPE_SMS                = "SMS";
    public static final String COMM_TYPE_EMAIL              = "EMAIL";
    public static final String COMM_TYPE_APP_PUSH           = "APP_PUSH";
    public static final String COMM_TYPE_APP_PUSH_AND_EMAIL = "APP_PUSH_AND_EMAIL";
    public static final String COMM_TYPE_SMS_AND_EMAIL      = "SMS_AND_EMAIL";
    public static final String COMM_TYPE_NONE               = "NONE";

    public static final int MAX_MESSAGE_LENGTH = 2048; //FUTURE: pull this from the push service

    public void load();

    public void unload();

    /**
     * Define a notification type. An example might be:
     * <pre>addNotificationType("TYPE_PLAYER_WON_DEAL", 3);</pre>
     * Where 3 is a constant that has been defined by your service to mean "PREF_TYPE_REWARDS".
     * <br/><br/>
     * By default, everything has a "general" pref type.
     *
     * @param name the name of the notification
     * @param prefType the type of pref (you keep track of the types)
     */
    public void addNotificationType(String name, int prefType);

    /**
     * If you wish to limit how often the notification service will send a specific type of notification to someone, call this.
     * <br/>An example might be:
     * <pre>setNotificationLimit("TYPE_PLAYER_ON_LEADERBOARD", 1);</pre>
     *
     * @param name the name of the notification
     * @param limit what is the limit
     */
    public void setNotificationLimit(String name, int limit);

    /**
     * If you want a specific type of notification to have a sound other than "default", call this.
     * <br/>An example might be:
     * <pre>setNotificationSound("TYPE_PLAYER_WON_DEAL", "winner.caf");</pre>
     *
     * @param name the name of the notification
     * @param sound the name of the sound file to pass along in the payload
     */
    public void setNotificationSound(String name, String sound);

    /**
     * If you want to add a category to the aps map of a push service for a certain type of notification,
     * this is where you setup the mapping.
     *
     * @param name the name of the notification
     * @param category the value of the category value in the push's aps payload
     */
    public void setNotificationCategory(String name, String category);

    /**
     * Send the given notification.
     * @param accountId
     * @param note
     * @param messagesByDeliveryType
     * @param appBundleIds TODO
     * @throws InvalidMessageException if the message is too long or missing
     * @throws InvalidNotifcationException if the required parameters are missing or have invalid values
     */
    public void sendNotification(
        int accountId, Notification note, Map<String,String> messagesByDeliveryType, Set<String> appBundleIds,
        Map<Long, String> subIdToLanguageCodeMap, Map<String, String> languageCodeToNotificationTitleMap, Map<String, String> languageCodeToNotificationBodyMap)
    throws InvalidMessageException, InvalidNotifcationException;

    /**
     * Change the status of the given notification,
     * and marks the lastUpdatedBy field to be idOfUpdater.
     *
     * @param notificationId
     * @param idOfUpdater
     * @param status
     */
    public void changeStatus(int notificationId, long idOfUpdater, String status);

    /**
     * Set the actionTake to action for the given notificationId, and marks
     * the lastUpdatedBy field to be idOfUpdater.
     *
     * @param notificationId
     * @param idOfUpdater
     * @param action
     * @throws NotificationAlreadyHandledException
     * @throws NotificationDeletedException
     */
    public void actionTaken(int notificationId, long idOfUpdater, String action) throws NotificationAlreadyHandledException, NotificationDeletedException;

    /**
     * Get the list of notifications for the given subscriber,
     * and filter by status, startDate, and endDate.  startDate and endDate
     * are compared against the created date.
     *
     * @param subscriberId
     * @param status the status to filter by, or null to not filter by status
     * @param startDate returns notifications created after startDate, or null to not filter by startDate
     * @param endDate returns notifications create before endDate, or null to not filter by endDate
     * @return
     */
    public List<Notification> getNotificationsForSubscriber(long subscriberId, String status, Date startDate, Date endDate);

    /**
     * Get the list of notification preferences for the given subscriberId
     * @param subscriberId
     * @return
     */
    public List<NotificationPref> getPrefsForSubscriber(long subscriberId);

    /**
     * Given a list of NotificationPrefs, loops through and sets each one
     * @param prefs
     * @throws InvalidPrefException
     */
    public void setPrefs(List<NotificationPref> prefs) throws InvalidPrefException;

    public HttpResponse doGet(HttpRequest request);

    public HttpResponse doPost(HttpRequest request);

    /**
     * Register a callback for a given notification type. The method name passed in should have
     * the following signature:
     *     void notificationCallback(Notification note, int eventType);
     * @param endpoint
     * @param methodName
     * @param notificationType
     * @return
     */
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName, String notificationType);

    /**
     * Un-register a callback for the given notification type.
     *
     * @param endpoint
     * @param notificationType
     * @return
     */
    public boolean unregisterCallback(ServiceEndpoint endpoint, String notificationType);

}
