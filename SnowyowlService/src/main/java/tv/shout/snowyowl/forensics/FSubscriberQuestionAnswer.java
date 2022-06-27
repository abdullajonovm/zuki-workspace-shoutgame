package tv.shout.snowyowl.forensics;

import java.util.Date;

public class FSubscriberQuestionAnswer
{
    public enum ANSWER_DETERMINATION {
        UNKNOWN,                       //not yet scored
        WON_TIME, LOST_TIME,           //both got it correct, one was faster
        WON_CORRECT, LOST_INCORRECT,   //one got it correct, one got it incorrect
        WON_TIMEOUT, LOST_TIMEOUT,     //one got it correct, one timed out
        LOST_ALL_TIMEOUT               //both timed out
    }

    public String id;
    public String selectedAnswerId; //null=not yet answered
    //public String questionDecryptKey;
    public Date questionPresentedTimestamp; //when the server sent the decrypt key (i.e. this data)
    public Long durationMilliseconds; //how long it took them to answer
    public ANSWER_DETERMINATION determination;
    public boolean won; //did the subscriber win (WON_TIME, WON_CORRECT, WON_TIMEOUT all mean the subscriber won)
    public Date createDate;

    public int subscriberId;
    public String nickname;
}
