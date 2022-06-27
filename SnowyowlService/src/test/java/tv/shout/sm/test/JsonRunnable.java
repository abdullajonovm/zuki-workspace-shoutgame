package tv.shout.sm.test;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JsonRunnable
implements Runnable
{
    public JsonNode json;
    public boolean showRawResult = true;

    public JsonRunnable()
    {

    }

    public JsonRunnable(boolean showRawResult)
    {
        this.showRawResult = showRawResult;
    }

}
