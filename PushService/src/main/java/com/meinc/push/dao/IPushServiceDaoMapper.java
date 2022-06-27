package com.meinc.push.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.push.domain.SubscriberToken;

public interface IPushServiceDaoMapper
{
    public SubscriberToken getSubscriberToken(SubscriberToken tokenToCheck);
    public void deleteToken(SubscriberToken token);
    public void deleteUsingDeviceToken(String deviceToken);
    public void insertToken(SubscriberToken token);
    public void updateToken(SubscriberToken token);
    public void removeToken(int tokenId);
    public List<SubscriberToken> getTokensForSubscriber(@Param("subscriberId") int subscriberId, @Param("deviceType") String deviceType);
    public List<SubscriberToken> getTokensForSubscriberAndBundleIds(@Param("subscriberId") int subscriberId, @Param("deviceType") String deviceType, @Param("bundleIds") List<String> bundleIds);
    public List<SubscriberToken> getTokensForSubscriberList(@Param("subscriberIds") List<Long> subscriberIds, @Param("deviceType") String deviceType);
    public List<SubscriberToken> getTokensForSubscriberListAndBundleId(@Param("subscriberIds") List<Integer> subscriberIds, @Param("deviceType") String deviceType, @Param("appBundleId") String appBundleId);
    public List<SubscriberToken> getTokensForSubscriberListAndBundleIdList(@Param("subscriberIds") List<Long> subscriberIds, @Param("deviceType") String deviceType, @Param("bundleIds") List<String> bundleIds);
    public List<SubscriberToken> getTokensForDeviceUuidList(List<String> deviceUuids);
    public List<SubscriberToken> getTokensBySubscriberIdAndDeviceUuid(@Param("subscriberId") int subscriberId, @Param("deviceUuid") String deviceUuid);
    public int removeTokensForSubscriberIdAndDeviceUuid(@Param("subscriberId") long subscriberId, @Param("deviceUuid") String deviceUuid);
    public int removeTokensForDeviceUuidExceptForThisSubscriber(@Param("subscriberId") long subscriberId, @Param("deviceUuid") String deviceUuid);
    public int removeTokensForDeviceUuid(String deviceUuid);
    public SubscriberToken getToken(String token);
    public void setDeviceInactive(String token);
    public String getC2dmAuthToken(String c2dmKey);
    public void insertc2dmAuthToken(@Param("osType") String c2dmKey, @Param("authToken") String authToken);
    public void upateC2dmAuthToken(@Param("osType") String c2dmKey, @Param("authToken") String authToken);
    public List<Long> getSubscriberIdsByType(String type);
    public void updateDeviceToken(@Param("oldDeviceToken") String oldDeviceToken, @Param("newDeviceToken") String newDeviceToken);
    public List<SubscriberToken> getAllActiveSubscriberTokens(int subscriberId);
    public List<Long> getSubscribersWithPushToken();
    public List<SubscriberToken> getTokensForDeviceOrToken(@Param("deviceUuid")String deviceUuid, @Param("deviceToken")String deviceToken);
}
