package com.meinc.gameplay.domain;

import java.io.Serializable;

import com.meinc.identity.domain.Subscriber;


public class NetworkWinner 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public Subscriber s;
    public int numActiveShoutFriends;
    
    public NetworkWinner(Subscriber s, int numActiveShoutFriends)
    {
        this.s = s;
        this.numActiveShoutFriends = numActiveShoutFriends;
    }

}
