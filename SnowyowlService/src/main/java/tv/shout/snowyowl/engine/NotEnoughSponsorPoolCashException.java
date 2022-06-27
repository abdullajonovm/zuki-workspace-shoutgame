package tv.shout.snowyowl.engine;

public class NotEnoughSponsorPoolCashException
extends Exception
{
    public NotEnoughSponsorPoolCashException()
    {
        super();
    }
    public NotEnoughSponsorPoolCashException(String msg)
    {
        super(msg);
    }

    private static final long serialVersionUID = 1L;

}
