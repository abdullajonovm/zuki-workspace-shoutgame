package com.meinc.gameplay.domain;

/**
 * Represents a new leaderboard that may be associated with a VIP box, an event, all events in a league, etc.
 * @author bxgrant
 */
public class LeaderboardPlus extends BaseDomainObject
{
    private static final long serialVersionUID = 1344254326550922206L;

    /**
     * A leaderboard currently may represent all the players in an event
     * and possible also the players in a VIP box.
     * @author bxgrant
     */
    public static enum LeaderboardType
    {
        /** This leaderboard is associated with a vip box. */
        VIPBOX,
        /** This leaderboard is associated with an event. */
        EVENT,
        /** This leaderboard represents the top xxx players in a given VipBox averaged over the lifetime of the vipbox */
        VIPBOX_OVERALL,
        /** This leaderboard represents the top xxx players for all events of a given category for a given year (season) */
        EVENT_CATEGORY_OVERALL
    };
    
    /**
     * The type of leaderboard.  @see LeaderboardType
     */
    private LeaderboardType _type;
    
    /**
     * The container that this leaderboard is associated with.  Right now
     * we support either an eventId or an  eventVipBoxId.  You can tell which it is
     * by looking at the value of the type member.  Soon, this will also
     * support a categoryId for the event category overall leaders.
     */
    private int _containerId;
    
    /**
     * The ranks of the leaderboard, returned in a pageable list in case there are too many
     * to return at once.
     */
    private PageableList<LeaderboardRank> _leaderboardRanks;
    
    /**
     * The rank of the subscriber asking for the leaderboard.
     */
    private LeaderboardRank _subscriberRank;
    
    public LeaderboardPlus()
    {
    }

    public LeaderboardType getType()
    {
        return _type;
    }

    public void setType(LeaderboardType type)
    {
        _type = type;
    }

    public int getContainerId()
    {
        return _containerId;
    }

    public void setContainerId(int containerId)
    {
        _containerId = containerId;
    }

    public PageableList<LeaderboardRank> getLeaderboardRanks()
    {
        return _leaderboardRanks;
    }

    public void setLeaderboardRanks(PageableList<LeaderboardRank> leaderboardRanks)
    {
        _leaderboardRanks = leaderboardRanks;
    }

    public LeaderboardRank getSubscriberRank()
    {
        return _subscriberRank;
    }

    public void setSubscriberRank(LeaderboardRank subscriberRank)
    {
        _subscriberRank = subscriberRank;
    }


}
