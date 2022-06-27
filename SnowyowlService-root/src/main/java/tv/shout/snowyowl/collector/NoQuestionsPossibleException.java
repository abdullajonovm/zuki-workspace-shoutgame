package tv.shout.snowyowl.collector;

public class NoQuestionsPossibleException extends Exception
{
    private static final long serialVersionUID = 1L;

    private String _roundId;

    public NoQuestionsPossibleException(String roundId)
    {
        super();
        _roundId = roundId;
    }

    public String getRoundId()
    {
        return _roundId;
    }

}
