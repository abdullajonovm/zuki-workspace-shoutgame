package com.meinc.gameplay.domain;

/**
 * This is how you represent a large organization and its subsidiaries (McDonald's corporate and the
 * various McD's locations).
 * 
 * @author bxgrant
 */
public class Org extends BaseDomainObject
{
    private static final long serialVersionUID = 3206963833422618466L;

    /** Id of organization. */
    private int _orgId;

    /** Parent organization ID. If set to zero, there is no parent. */
    private int _parentOrgId;
    
    /** The name of the organization. */
    private String _name;
    
    /** Optional billing/administrative street address field 1 */
    private String _addr1;
    
    /** Optional billing/administrative street address field 2 */
    private String _addr2;
    
    /** Optional billing/administrative city of org. */
    private String _city;
    
    /** Optional billing/administrative state of org. */
    private String _state;
    
    /** Optional billing/administrative postal code of org. */
    private String _postalCode;
    
    /** Optional billing/administrative country of org. */
    private int _countryCode;
    
    /** The physical location of the organization */
    private Location _location;

    public Org()
    {
    }

    public int getOrgId()
    {
        return _orgId;
    }

    public void setOrgId(int orgId)
    {
        _orgId = orgId;
    }

    public int getParentOrgId()
    {
        return _parentOrgId;
    }

    public void setParentOrgId(int parentOrgId)
    {
        _parentOrgId = parentOrgId;
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

    public Location getLocation()
    {
        return _location;
    }

    public void setLocation(Location location)
    {
        _location = location;
    }
}
