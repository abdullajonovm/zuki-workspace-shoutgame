package tv.shout.snowyowl.forensics;

import java.util.Date;

public class FMatchPlayer
{
    public enum MATCH_PLAYER_DETERMINATION {
        UNKNOWN,    // Not yet scored
        WON,        // Won the set of questions
        LOST,       // Lost the set of questions
        CANCELLED,  //the game was cancelled
        SAVED
    }

    public String id;
    public MATCH_PLAYER_DETERMINATION determination;
    public Double score;
    public Date createDate;

    public int subscriberId;
    public String nickname;
}
