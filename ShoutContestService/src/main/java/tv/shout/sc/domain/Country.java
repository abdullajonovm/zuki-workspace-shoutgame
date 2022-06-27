package tv.shout.sc.domain;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class Country
implements Serializable
{
    private String _countryCode;
    private int _dialCode;
    private int _sortOrder;
    private Map<String, String> countryNames;

    public String getCountryCode()
    {
        return _countryCode;
    }
    public void setCountryCode(String countryCode)
    {
        _countryCode = countryCode;
    }
    public int getDialCode()
    {
        return _dialCode;
    }
    public void setDialCode(int dialCode)
    {
        _dialCode = dialCode;
    }
    public int getSortOrder()
    {
        return _sortOrder;
    }
    public void setSortOrder(int sortOrder)
    {
        _sortOrder = sortOrder;
    }
    public Map<String, String> getCountryNames()
    {
        return countryNames;
    }
    public void setCountryNames(Map<String, String> countryNames)
    {
        this.countryNames = countryNames;
    }

}
