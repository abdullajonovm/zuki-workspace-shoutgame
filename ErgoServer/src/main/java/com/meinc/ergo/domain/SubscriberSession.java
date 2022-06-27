package com.meinc.ergo.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.meinc.ergo.util.SubscriberSessionSerializer;

@JsonSerialize(using=SubscriberSessionSerializer.class)
public class SubscriberSession
{
    private Subscriber subscriber;
    private String sessionKey;
    
    public SubscriberSession(Subscriber s, String key)
    {
        subscriber = s;
        sessionKey = key;
    }
    
    public Subscriber getSubscriber()
    {
        return subscriber;
    }
    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }
    public String getSessionKey()
    {
        return sessionKey;
    }
    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }
}
