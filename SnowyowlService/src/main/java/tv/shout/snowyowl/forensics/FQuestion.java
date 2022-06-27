package tv.shout.snowyowl.forensics;

import java.util.Date;
import java.util.List;

public class FQuestion
{
    public String id;
    public String questionText;
    public Date createDate;

    public List<FQuestionAnswer> answers;

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(questionText);
        answers.forEach(a -> buf.append("\n").append(a));

        return buf.toString();
    }
}
