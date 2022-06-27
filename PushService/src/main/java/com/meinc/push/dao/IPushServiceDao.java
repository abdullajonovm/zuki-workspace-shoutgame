package com.meinc.push.dao;

import java.util.List;

import com.meinc.push.domain.SubscriberToken;

public interface IPushServiceDao {

    public SubscriberToken getSubscriberToken(SubscriberToken tokenToCheck);

    public void deleteToken(SubscriberToken token);

    public void deleteUsingDeviceToken(String deviceToken);

    public void insertToken(SubscriberToken token);

    public void updateToken(SubscriberToken token);

    public void removeToken(int tokenId);

    public List<SubscriberToken> getTokensForDeviceOrToken(String deviceUuid, String deviceToken);

    public List<SubscriberToken> getTokensForSubscriberAndDeviceType(int subscriberId, String deviceType);

    public List<SubscriberToken> getTokensForSubscriberListAndDeviceType(List<Long> subscriberIds, String deviceType);

    public List<SubscriberToken> getTokensForDeviceUuidList(List<String> deviceUuids);

    public List<SubscriberToken> getTokensBySubscriberIdAndDeviceUuid(int subscriberId, String deviceUuid);

    public int removeTokensForSubscriberIdAndDeviceUuid(long subscriberId, String deviceUuid);

    public int removeTokensForDeviceUuidExceptForThisSubscriber(long subscriberId, String deviceUuid);

    public int removeTokensForDeviceUuid(String deviceUuid);

    public SubscriberToken getToken(String token);

    public void setDeviceInactive(String token);

    public String getC2dmAuthToken();

    public void insertc2dmAuthToken(String authToken);

    public void upateC2dmAuthToken(String authToken);

    public List<Long> getSubscriberIdsByType(String type);

    public void updateDeviceToken(String oldDeviceToken, String newDeviceToken);

    public List<SubscriberToken> getAllActiveSubscriberTokens(int subscriberId);

    public List<Long> getSubscribersWithPushToken();

    public List<SubscriberToken> getTokensForSubscriberAndBundleIds(int subscriberId, String deviceType, List<String> bundleIds);
    public List<SubscriberToken> getTokensForSubscriberListAndBundleId(List<Integer> subscriberIds, String deviceType, String appBundleId);
    public List<SubscriberToken> getTokensForSubscriberListAndBundleIdList(List<Long> subscriberIds, String deviceType, List<String> bundleIds);

}
