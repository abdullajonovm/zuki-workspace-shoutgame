package tv.shout.snowyowl.forensics;

import java.util.Date;

public class FGamePlayer
{
    public enum GAME_PLAYER_DETERMINATION {
        INPLAY,     // actively playing the game
        SIDELINES,  // sitting out a round (but still in the game)
        ELIMINATED, // no longer eligible to play rounds (you were eliminated)
        AWARDED,    // game is over, you have been awarded your winnings (if any)
        REMOVED,    // if a player "unjoins" a game - retains history
        CANCELLED   // the game was cancelled
    }

    public String id;
    public Double rank;
    //public Double payoutAwardedAmount;
    //public boolean payoutCompleted;
    public GAME_PLAYER_DETERMINATION determination;
    public Date createDate;

    public int subscriberId;
    public String nickname;
}
