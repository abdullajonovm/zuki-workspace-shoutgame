package tv.shout.sm.test;

import java.util.List;
import java.util.Map;

/**
 * A  helper class that will trap network exceptions so that a DataReceiver.dataCallbackFailure message can be sent
 */
@SuppressWarnings("serial")
public class NetworkException 
extends Exception
{
    public int httpResponseCode;
    public Map<String, List<String>> responseHeaders;
    public String responseBody;

    public NetworkException(int httpResponseCode, Map<String, List<String>> responseHeaders, String responseBody)
    {
        super();
        this.httpResponseCode = httpResponseCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

}
