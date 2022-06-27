package com.meinc.store.domain;

import java.io.Serializable;

public class BillingInfo implements Serializable
{
    private static final long serialVersionUID = -141408990043731706L;

    private String _firstName;
    private String _lastName;
    private String _ccNum;
    private String _cvv2;
    private String _zip;
    private int _ccExpMonth; // 1 - 12
    private int _ccExpYear; // YYYY

    public BillingInfo()
    {
    }

    public String getFirstName()
    {
        return _firstName;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public String getLastName()
    {
        return _lastName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public String getCcNum()
    {
        return _ccNum;
    }

    public void setCcNum(String ccNum)
    {
        _ccNum = ccNum;
    }

    public String getCvv2()
    {
        return _cvv2;
    }

    public void setCvv2(String cvv2)
    {
        _cvv2 = cvv2;
    }

    public String getZip()
    {
        return _zip;
    }

    public void setZip(String zip)
    {
        _zip = zip;
    }

    public int getCcExpMonth()
    {
        return _ccExpMonth;
    }

    public void setCcExpMonth(int ccExpMonth)
    {
        _ccExpMonth = ccExpMonth;
    }

    public int getCcExpYear()
    {
        return _ccExpYear;
    }

    public void setCcExpYear(int ccExpYear)
    {
        _ccExpYear = ccExpYear;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{firstName: ").append(_firstName).append(", lastName: ").append(_lastName).append(", ccnum: ").append(_ccNum).append(", cvv2: ").append(_cvv2);
        sb.append(", zip: ").append(_zip).append(", ccExpMonth").append(_ccExpMonth).append(", ccExpYear").append(_ccExpYear).append("}");

        return sb.toString();
    }

}
