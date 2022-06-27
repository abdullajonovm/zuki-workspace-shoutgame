package tv.shout.snowyowl.forensics;

import java.util.Date;
import java.util.List;

import tv.shout.snowyowl.forensics.FMatch.MATCH_DETERMINATION;
import tv.shout.snowyowl.forensics.FMatch.MATCH_STATUS;

public class FMatchQuestion
{
    public String id;
    public String questionId;
    //public String questionValue; //ala jeopardy: $100, or 25points

    public MATCH_STATUS matchQuestionStatus;
    public Integer wonSubscriberId; // will be null until question is scored, will still be null if no one won.
    public MATCH_DETERMINATION determination; // will be null until question is scored

    public Date createDate;
    public Date completedDate;

    public FQuestion question;
    public List<FSubscriberQuestionAnswer> sqas;
}
