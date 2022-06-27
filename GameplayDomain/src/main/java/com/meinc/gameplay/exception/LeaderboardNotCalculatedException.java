package com.meinc.gameplay.exception;

import java.io.Serializable;

public class LeaderboardNotCalculatedException extends Exception implements Serializable
{
    private static final long serialVersionUID = 2351480305714643154L;

    public LeaderboardNotCalculatedException()
    {
        super();
    }
    
    public LeaderboardNotCalculatedException(String msg)
    {
        super(msg);
    }
}
