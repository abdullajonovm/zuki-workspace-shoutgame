package com.meinc.push.dao;

import java.util.List;

import com.meinc.push.domain.SubscriberToken;

public class PushServiceDaoSqlMap implements IPushServiceDao {

    private static final String C2DM_KEY = "Android";
    private IPushServiceDaoMapper _mapper;

    @Override
    public SubscriberToken getSubscriberToken(SubscriberToken tokenToCheck) {
        return _mapper.getSubscriberToken(tokenToCheck);
    }

    @Override
    public void deleteToken(SubscriberToken token) {
        _mapper.deleteToken(token);
    }

    @Override
    public void deleteUsingDeviceToken(String deviceToken)
    {
        _mapper.deleteUsingDeviceToken(deviceToken);
    }

    @Override
    public void insertToken(SubscriberToken token) {
        _mapper.insertToken(token);
    }

    @Override
    public List<SubscriberToken> getTokensForSubscriberAndDeviceType(int subscriberId, String deviceType) {
        return _mapper.getTokensForSubscriber(subscriberId, deviceType);
    }

    @Override
    public List<SubscriberToken> getTokensForSubscriberListAndDeviceType(List<Long> subscriberIds, String deviceType) {
        return _mapper.getTokensForSubscriberList(subscriberIds, deviceType);
    }

    @Override
    public List<SubscriberToken> getTokensForDeviceUuidList(List<String> deviceUuids) {
        return _mapper.getTokensForDeviceUuidList(deviceUuids);
    }

    @Override
    public List<SubscriberToken> getTokensBySubscriberIdAndDeviceUuid(int subscriberId, String deviceUuid) {
        return _mapper.getTokensBySubscriberIdAndDeviceUuid(subscriberId, deviceUuid);
    }

    @Override
    public int removeTokensForSubscriberIdAndDeviceUuid(long subscriberId, String deviceUuid) {
        return _mapper.removeTokensForSubscriberIdAndDeviceUuid(subscriberId, deviceUuid);
    }

    @Override
    public int removeTokensForDeviceUuidExceptForThisSubscriber(long subscriberId, String deviceUuid)
    {
        return _mapper.removeTokensForDeviceUuidExceptForThisSubscriber(subscriberId, deviceUuid);
    }

    @Override
    public int removeTokensForDeviceUuid(String deviceUuid)
    {
        return _mapper.removeTokensForDeviceUuid(deviceUuid);
    }

    @Override
    public SubscriberToken getToken(String token) {
        return _mapper.getToken(token);
    }

    @Override
    public void setDeviceInactive(String token) {
        _mapper.setDeviceInactive(token);
    }

    @Override
    public String getC2dmAuthToken()
    {
        return  _mapper.getC2dmAuthToken(C2DM_KEY);
    }

    @Override
    public void insertc2dmAuthToken(String authToken)
    {
        _mapper.insertc2dmAuthToken(C2DM_KEY, authToken);
    }

    @Override
    public void upateC2dmAuthToken(String authToken)
    {
        _mapper.upateC2dmAuthToken(C2DM_KEY, authToken);
    }

    @Override
    public List<Long> getSubscriberIdsByType(String type)
    {
        return _mapper.getSubscriberIdsByType(type);
    }

    @Override
    public void updateDeviceToken(String oldDeviceToken, String newDeviceToken)
    {
        _mapper.updateDeviceToken(oldDeviceToken, newDeviceToken);
    }

    @Override
    public List<SubscriberToken> getAllActiveSubscriberTokens(int subscriberId) {
        return _mapper.getAllActiveSubscriberTokens(subscriberId);
    }

    @Override
    public List<Long> getSubscribersWithPushToken()
    {
        return _mapper.getSubscribersWithPushToken();
    }

    public IPushServiceDaoMapper getMapper()
    {
        return _mapper;
    }

    public void setMapper(IPushServiceDaoMapper mapper)
    {
        _mapper = mapper;
    }

	@Override
	public List<SubscriberToken> getTokensForSubscriberAndBundleIds(
			int subscriberId, String deviceType, List<String> bundleIds) {
		return _mapper.getTokensForSubscriberAndBundleIds(subscriberId, deviceType, bundleIds);
	}

	@Override
	public List<SubscriberToken> getTokensForSubscriberListAndBundleId(
			List<Integer> subscriberIds, String deviceType, String appBundleId) {
		return _mapper.getTokensForSubscriberListAndBundleId(subscriberIds, deviceType, appBundleId);
	}

	@Override
	public List<SubscriberToken> getTokensForSubscriberListAndBundleIdList(
			List<Long> subscriberIds, String deviceType,
			List<String> bundleIds)
	{
		return _mapper.getTokensForSubscriberListAndBundleIdList(subscriberIds, deviceType, bundleIds);
	}

    @Override
    public List<SubscriberToken> getTokensForDeviceOrToken(String deviceUuid, String deviceToken) {
        return _mapper.getTokensForDeviceOrToken(deviceUuid, deviceToken);
    }

    @Override
    public void updateToken(SubscriberToken token) {
        _mapper.updateToken(token);
    }

    @Override
    public void removeToken(int tokenId) {
        _mapper.removeToken(tokenId);
    }
}
