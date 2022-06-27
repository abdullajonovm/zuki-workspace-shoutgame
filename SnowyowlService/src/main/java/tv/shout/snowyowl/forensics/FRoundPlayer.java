package tv.shout.snowyowl.forensics;

import java.util.Date;

public class FRoundPlayer
{
    public enum ROUND_PLAYER_DETERMINATION {
        WON, LOST, TIMEDOUT, ABANDONED, UNKNOWN, CANCELLED, SAVED
    }

    public String id;
    public Integer playedMatchCount;
    public ROUND_PLAYER_DETERMINATION determination;
    public Double skillAnswerCorrectPct;
    public Long skillAverageAnswerMs;
    public Integer rank;
    public Double skill;
    public Date createDate;

    //public Double amountPaid;
    //public Boolean refunded;

    public int subscriberId;
    public String nickname;
}
