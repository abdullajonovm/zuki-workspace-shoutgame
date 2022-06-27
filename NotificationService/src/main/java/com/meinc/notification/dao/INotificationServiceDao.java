package com.meinc.notification.dao;

import java.util.Date;
import java.util.List;

import com.meinc.notification.domain.Notification;
import com.meinc.notification.domain.NotificationCallback;
import com.meinc.notification.domain.NotificationPref;

public interface INotificationServiceDao {
    public void addCallback(NotificationCallback callback);
    public NotificationCallback getCallbackForEndpoint(NotificationCallback callback);
    public List<NotificationCallback> getCallbacksForType(String notificationType);
    public int removeCallback(NotificationCallback callback);
    public void addNotification(Notification notification);
    public List<Notification> getNotificationsByRecipient(long recipient, String status, Date startDate, Date endDate);
    public void updateNotification(Notification notification);
    public Notification getNotificationById(int notificationId);
    public List<NotificationPref> getPrefsForSubscriber(long subscriberId);
    public NotificationPref getPrefForSubscriberByType(long subscriberId, int prefType);
    public void setPrefForSubscriber(NotificationPref pref);
    public boolean hasSubscriberBeenNotifiedOfLeaderboardTop50ForEvent(long subscriberId, int eventId);
    public void setSubscriberNotifiedOfLeaderboardTop50ForEvent(long subscriberId, int eventId);
}
