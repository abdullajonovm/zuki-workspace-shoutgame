package tv.shout.snowyowl.engine;

import java.util.Map;

public class PayoutManagerException
extends Exception
{
    private static final long serialVersionUID = 1L;
    private String _errorTypeCode;
    private Map<String, String> _errorDetails;

    public PayoutManagerException(String errorTypeCode, Map<String, String> errorDetails)
    {
        super();
        _errorTypeCode = errorTypeCode;
        _errorDetails = errorDetails;
    }

    public String getErrorTypeCode()
    {
        return _errorTypeCode;
    }

    public Map<String, String> getErrorDetails()
    {
        return _errorDetails;
    }

}
