package com.meinc.notification.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.notification.domain.Notification;
import com.meinc.notification.domain.NotificationCallback;
import com.meinc.notification.domain.NotificationPref;

public interface INotificationServiceDaoMapper {
    public void addCallback(NotificationCallback callback);
    public NotificationCallback getCallbackForEndpoint(NotificationCallback callback);
    public List<NotificationCallback> getCallbacksForType(String notificationType);
    public int removeCallback(NotificationCallback callback);
    public void addNotification(Notification notification);
    public List<Notification> getNotificationsByRecipient(@Param("subscriberId") long recipient, @Param("status") String status, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    public void updateNotification(Notification notification);
    public Notification getNotificationById(int notificationId);
    public List<NotificationPref> getPrefsForSubscriber(long subscriberId);
    public NotificationPref getPrefForSubscriberByType(@Param("subscriberId") long subscriberId, @Param("prefType") int prefType);
    public void setPrefForSubscriber(NotificationPref pref);
    public boolean hasSubscriberBeenNotifiedOfLeaderboardTop50ForEvent(@Param("subscriberId") long subscriberId, @Param("eventId") int eventId);
    public void setSubscriberNotifiedOfLeaderboardTop50ForEvent(@Param("subscriberId") long subscriberId, @Param("eventId") int eventId);
}
