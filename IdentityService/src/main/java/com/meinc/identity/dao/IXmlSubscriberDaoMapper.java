package com.meinc.identity.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.SubscriberProfile;
import com.meinc.identity.domain.SubscriberSessionLight;

public interface IXmlSubscriberDaoMapper
{
    public List<SubscriberSessionLight> getSubscribersForSessionTokens(List<String> subscriberSessionTokens);
    public List<Subscriber> getSubscribers(List<Long> subscriberIds);
    public List<Subscriber> getSubscribersByPhones(List<String> phones);
    public List<SubscriberProfile> getIdentityInfoForFacebookUsers(@Param("facebookIds") List<String> facebookIds, @Param("contextId") int contextId);
}
