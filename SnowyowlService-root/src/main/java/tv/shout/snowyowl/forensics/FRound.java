package tv.shout.snowyowl.forensics;

import java.util.Date;
import java.util.List;

public class FRound
{
    public enum ROUND_TYPE {
        POOL, BRACKET, BINARY
    }

    public enum ROUND_STATUS {
        PENDING   //round is created but not yet ready to play
       ,CANCELLED //round has been cancelled
       ,VISIBLE   //round is no longer pending (can be seen), but isn't started (can't be joined)
       ,OPEN      //round is ready to join (or leave)
       ,FULL      //round is ready to join, but is full
       //,READY     //round is ready to being pairing
       ,INPLAY    //round is in play (no more joining/leaving/pairing)
       ,CLOSED    //round is complete
   }

    public String id;
    public String name;
    public ROUND_TYPE roundType;
    public ROUND_STATUS roundStatus;
    public int roundSequence;
    public boolean finalRound;

    public int currentPlayerCount;   // how many players are currently in the round
    public int maximumPlayerCount;    //maximum # of players to let into a round (this round only supports 100, for example)
    public int minimumMatchCount;     //you have to play (and win) at least this many to move on
    public Integer maximumMatchCount;     //you can't play more than this many or we kick you out
    public int minimumActivityToWinCount;  //minimum number of activities (questions) needed to win/move on
    public Integer maximumActivityCount;

    //public Double roundPurse;
    //public Double costPerPlayer;

    public Date pendingDate;
    public Date openDate;

    public List<FRoundPlayer> roundPlayers;
    public List<FMatch> matches;
}
