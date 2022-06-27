package tv.shout.snowyowl.forensics;

import java.util.Date;
import java.util.List;

public class FGame
{
    public enum GAME_STATUS {
        PENDING     //game is created, but not ready for public consumption
        ,CANCELLED
        ,OPEN       //game is ready for players to join (and leave). pool play may or may not be started (need to check rounds)
        ,INPLAY     //game is currently in bracket play
        ,CLOSED
    }

    /*public enum PAYOUT_CALCULATION_METHOD {
        STATIC      //payout amount does not vary
        ,DYNAMIC    //payout amount varies based on number of players
    }*/

    public String id;
    public String name;
    public GAME_STATUS gameStatus;
    public Integer bracketEliminationCount; //how many times can the player lose in bracket rounds before being eliminated; null=no limit

    //public PAYOUT_CALCULATION_METHOD payoutCalculationMethod = PAYOUT_CALCULATION_METHOD.STATIC;
    //private Double mPurse; //calculated field
    //private Double mCostToJoin; //calculated field
    //private float payoutHouseTakePercentage;
    //private float payoutPercentageOfUsersToAward;

    public Date pendingDate;
    public Date openDate;
    public Date closedDate;

    public List<FGamePlayer> gamePlayers;
    public List<FRound> rounds;
}
