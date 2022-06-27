package tv.shout.snowyowl.domain;

public class AiQuestionBundle
{
    public long aiSubscriberId;
    public String sqaId;
    public Question question;
    public long numMsBeforeTimeout;
    public String correctAnswerId;
    public String incorrectAnswerId;
    public boolean useDoctoredTime;
    public String gameEngineTYpe;

    public AiQuestionBundle(
            long aiSubscriberId, String sqaId, Question question, long numMsBeforeTimeout, String correctAnswerId, String incorrectAnswerId, boolean useDoctoredTime,
            String gameEngineType)
    {
        this.aiSubscriberId = aiSubscriberId;
        this.sqaId = sqaId;
        this.question = question;
        this.numMsBeforeTimeout = numMsBeforeTimeout;
        this.correctAnswerId = correctAnswerId;
        this.incorrectAnswerId = incorrectAnswerId;
        this.useDoctoredTime = useDoctoredTime;
        this.gameEngineTYpe = gameEngineType;
    }

}
