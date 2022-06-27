package tv.shout.snowyowl.engine;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

public class MMEProcess
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public String gameId;
    public String id;
    public long time;
    public boolean isBracket;

    public MMEProcess(String id, String gameId)
    {
        this.id = id;
        this.gameId = gameId;
        time = System.currentTimeMillis();
    }

    public MMEProcess(String id, String gameId, boolean isBracket)
    {
        this.id = id;
        this.gameId = gameId;
        this.isBracket = isBracket;
        time = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj)
    {
        return ((MMEProcess) obj).id.equals(id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("id: {0}, bracket: {1}, added: {2,date,yyyy-MM-dd hh:mm:ss.SSS}", id, isBracket, new Date(time));
    }
}
