package tv.shout.snowyowl.engine;

public class NotEnoughSponsorsException
extends Exception
{
    public NotEnoughSponsorsException()
    {
        super();
    }
    public NotEnoughSponsorsException(String msg)
    {
        super(msg);
    }

    private static final long serialVersionUID = 1L;

}
