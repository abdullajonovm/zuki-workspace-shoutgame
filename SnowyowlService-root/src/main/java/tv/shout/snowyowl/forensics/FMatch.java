package tv.shout.snowyowl.forensics;

import java.util.Date;
import java.util.List;

public class FMatch
{
    public enum MATCH_STATUS { NEW, OPEN, WAITING_FOR_NEXT_QUESTION, PROCESSING, CLOSED, CANCELLED }

    public enum MATCH_DETERMINATION { WINNER, NO_WINNER, TIE, UNKNOWN }

    public String id;
    public MATCH_STATUS matchStatus;
    public Integer wonSubscriberId;
    public int minimumActivityToWinCount;
    public Integer maximumActivityCount;
    public Integer actualActivityCount;
    public MATCH_DETERMINATION determination;

    public Date createDate;
    public Date startDate;
    public Date completeDate;

    public List<FMatchPlayer> matchPlayers;
    public List<FMatchQuestion> matchQuestions;
}
