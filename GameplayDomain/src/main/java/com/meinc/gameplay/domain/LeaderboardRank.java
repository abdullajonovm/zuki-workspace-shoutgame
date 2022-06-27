package com.meinc.gameplay.domain;


/**
 * Represents a _rank on a leaderboard, no matter what type of leaderboard.
 * 
 * @author bxgrant
 */
public class LeaderboardRank extends BaseDomainObject
{
    private static final long serialVersionUID = -3934288326306562195L;
    
    /**
     * The _rank of the player on the given leadeboard.
     */
    private int _rank;
    
    /** The rank of the player on the event this leaderboard is associated with.  If this member is -1, that means this value is invalid and ignore it. */
    private int _eventRank;

    /**
     * The subscriber of the leaderboard whose _rank this is.
     */
    private int _subscriberId;
    
    /** The nickname of the subscriber whose _rank this is. */
    private String _nickname;
    
    /** The avatar of the subscriber. */
    private String _avatarUrl;
    
    public LeaderboardRank()
    {
    }

    public int getRank()
    {
        return _rank;
    }

    public void setRank(int rank)
    {
        this._rank = rank;
    }

    public int getSubscriberId()
    {
        return _subscriberId;
    }

    public int getEventRank()
    {
        return _eventRank;
    }

    public void setEventRank(int eventRank)
    {
        _eventRank = eventRank;
    }

    public void setSubscriberId(int subscriberId)
    {
        _subscriberId = subscriberId;
    }

    public String getNickname()
    {
        return _nickname;
    }

    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }

    public String getAvatarUrl()
    {
        return _avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl)
    {
        _avatarUrl = avatarUrl;
    }
    
}
