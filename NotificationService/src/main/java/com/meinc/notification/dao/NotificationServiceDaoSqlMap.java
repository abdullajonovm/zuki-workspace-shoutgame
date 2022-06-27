package com.meinc.notification.dao;

import java.util.Date;
import java.util.List;

import com.meinc.notification.domain.Notification;
import com.meinc.notification.domain.NotificationCallback;
import com.meinc.notification.domain.NotificationPref;

public class NotificationServiceDaoSqlMap implements INotificationServiceDao {

    private INotificationServiceDaoMapper _mapper;

    public INotificationServiceDaoMapper getMapper() {
        return _mapper;
    }

    public void setMapper(INotificationServiceDaoMapper mapper) {
        _mapper = mapper;
    }

    @Override
    public void addCallback(NotificationCallback callback) {
        _mapper.addCallback(callback);
    }

    @Override
    public NotificationCallback getCallbackForEndpoint(NotificationCallback callback) {
        return _mapper.getCallbackForEndpoint(callback);
    }

    @Override
    public List<NotificationCallback> getCallbacksForType(String notificationType) {
        return _mapper.getCallbacksForType(notificationType);
    }

    @Override
    public int removeCallback(NotificationCallback callback) {
        return _mapper.removeCallback(callback);
    }

    @Override
    public void addNotification(Notification notification) {
        _mapper.addNotification(notification);
    }

    @Override
    public List<Notification> getNotificationsByRecipient(long recipient, String status, Date startDate, Date endDate) {
        return _mapper.getNotificationsByRecipient(recipient, status, startDate, endDate);
    }

    @Override
    public void updateNotification(Notification notification) {
        _mapper.updateNotification(notification);
    }

    @Override
    public Notification getNotificationById(int notificationId) {
        return _mapper.getNotificationById(notificationId);
    }

    @Override
    public List<NotificationPref> getPrefsForSubscriber(long subscriberId) {
        return _mapper.getPrefsForSubscriber(subscriberId);
    }

    @Override
    public NotificationPref getPrefForSubscriberByType(long subscriberId, int prefType) {
        return _mapper.getPrefForSubscriberByType(subscriberId, prefType);
    }

    @Override
    public void setPrefForSubscriber(NotificationPref pref) {
        _mapper.setPrefForSubscriber(pref);
    }

    @Override
    public boolean hasSubscriberBeenNotifiedOfLeaderboardTop50ForEvent(long subscriberId, int eventId)
    {
        return _mapper.hasSubscriberBeenNotifiedOfLeaderboardTop50ForEvent(subscriberId, eventId);
    }

    @Override
    public void setSubscriberNotifiedOfLeaderboardTop50ForEvent(long subscriberId, int eventId)
    {
        _mapper.setSubscriberNotifiedOfLeaderboardTop50ForEvent(subscriberId, eventId);
    }
}
