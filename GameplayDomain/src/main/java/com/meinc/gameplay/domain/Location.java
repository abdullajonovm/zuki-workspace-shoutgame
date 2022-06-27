package com.meinc.gameplay.domain;

public class Location extends BaseDomainObject
{
    private static final long serialVersionUID = -6562417666579062101L;
    
    private int _locationId;
    
    /** A name for the location, may be null. */
    private String _name;
    
    /** Optional. Street address field 1 */
    private String _addr1;
    
    /** Optional. Street address field 2 */
    private String _addr2;
    
    /** Optional. City of org. */
    private String _city;
    
    /** Optional. State of org. */
    private String _state;
    
    /** Optional. Postal code of org. */
    private String _postalCode;
    
    /** Optional. Country of org. */
    private int _countryCode;
    
    /** Optional. Longitude of location -- Decima(9,6) meaning 3 places to left and 6 to right of decimal point as in ###.###### */
    private float _longitude;
    
    /** Optional. Latitude of location -- Decima(9,6) meaning 3 places to left and 6 to right of decimal point as in ###.###### */
    private float _latitude;

    public Location()
    {
    }

    public int getLocationId()
    {
        return _locationId;
    }

    public void setLocationId(int locationId)
    {
        _locationId = locationId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getAddr1()
    {
        return _addr1;
    }

    public void setAddr1(String addr1)
    {
        _addr1 = addr1;
    }

    public String getAddr2()
    {
        return _addr2;
    }

    public void setAddr2(String addr2)
    {
        _addr2 = addr2;
    }

    public String getCity()
    {
        return _city;
    }

    public void setCity(String city)
    {
        _city = city;
    }

    public String getState()
    {
        return _state;
    }

    public void setState(String state)
    {
        _state = state;
    }

    public String getPostalCode()
    {
        return _postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        _postalCode = postalCode;
    }

    public int getCountryCode()
    {
        return _countryCode;
    }

    public void setCountryCode(int countryCode)
    {
        _countryCode = countryCode;
    }

    public float getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(float longitude)
    {
        _longitude = longitude;
    }

    public float getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(float latitude)
    {
        _latitude = latitude;
    }
}
