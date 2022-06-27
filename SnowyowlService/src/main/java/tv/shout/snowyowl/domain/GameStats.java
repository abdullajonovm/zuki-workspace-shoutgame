package tv.shout.snowyowl.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GameStats
implements Serializable
{
    private String _gameId;
    private Integer _remainingPlayers;
    private Boolean _freeplayNotificationSent;
    private Integer _remainingSavePlayerCount;
    private Long _twitchConsoleFollowedSubscriberId;

    public GameStats() {} //bean compliance for jbdc/mybatis/etc

    public GameStats(String gameId)
    {
        _gameId = gameId;
    }

    public String getGameId()
    {
        return _gameId;
    }
    public void setGameId(String gameId)
    {
        _gameId = gameId;
    }

    public Integer getRemainingPlayers()
    {
        return _remainingPlayers;
    }
    public void setRemainingPlayers(Integer remainingPlayers)
    {
        _remainingPlayers = remainingPlayers;
    }
    public GameStats withRemainingPlayers(Integer remainingPlayers)
    {
        _remainingPlayers = remainingPlayers;
        return this;
    }


    public Boolean getFreeplayNotificationSent()
    {
        return _freeplayNotificationSent;
    }
    public void setFreeplayNotificationSent(Boolean freeplayNotificationSent)
    {
        _freeplayNotificationSent = freeplayNotificationSent;
    }
    public GameStats withFreeplayNotificationSent(Boolean freeplayNotificationSent)
    {
        _freeplayNotificationSent = freeplayNotificationSent;
        return this;
    }

    public Integer getRemainingSavePlayerCount()
    {
        return _remainingSavePlayerCount;
    }
    public void setRemainingSavePlayerCount(Integer remainingSavePlayerCount)
    {
        _remainingSavePlayerCount = remainingSavePlayerCount;
    }
    public GameStats withRemainingSavePlayerCount(Integer remainingSavePlayerCount)
    {
        _remainingSavePlayerCount = remainingSavePlayerCount;
        return this;
    }

    public Long getTwitchConsoleFollowedSubscriberId()
    {
        return _twitchConsoleFollowedSubscriberId;
    }
    public void setTwitchConsoleFollowedSubscriberId(Long twitchConsoleFollowedSubscriberId)
    {
        _twitchConsoleFollowedSubscriberId = twitchConsoleFollowedSubscriberId;
    }
    public GameStats withTwitchConsoleFollowedSubscriberId(Long twitchConsoleFollowedSubscriberId)
    {
        _twitchConsoleFollowedSubscriberId = twitchConsoleFollowedSubscriberId;
        return this;
    }

}
