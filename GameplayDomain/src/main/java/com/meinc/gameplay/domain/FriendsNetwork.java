package com.meinc.gameplay.domain;

import java.io.Serializable;

import com.meinc.identity.domain.Subscriber;

public class FriendsNetwork
implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Subscriber _subscriber;
    private int _networkSize;
    
    public FriendsNetwork()
    {
    }

    public void setGameplaySubscriber(Subscriber subscriber)
    {
        _subscriber = subscriber;
    }

    public Subscriber getGameplaySubscriber()
    {
        return _subscriber;
    }

    public void setNetworkSize(int networkSize)
    {
        _networkSize = networkSize;
    }

    public int getNetworkSize()
    {
        return _networkSize;
    }
}
