package tv.shout.snowyowl.forensics;

import java.text.MessageFormat;
import java.util.Date;

public class FQuestionAnswer
{
    public String id;
    public String answerText;
    public Boolean correct;
    //public Integer surveyPercent; //survey says ... 42
    public Date createDate;

    @Override
    public String toString()
    {
        return MessageFormat.format("\t{0}: {1}", correct, answerText);
    }
}
