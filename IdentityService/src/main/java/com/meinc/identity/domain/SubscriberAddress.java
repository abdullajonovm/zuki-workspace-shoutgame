package com.meinc.identity.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 * {
 *   "addressId": int,
 *   "subscriberId": int,
 *   "type": enum[HOME, BILLING, SHIPPING],
 *   "addr1": string,
 *   "addr2": string (optional),
 *   "city": string,
 *   "state": string,
 *   "zip": string,
 *   "countryCode": string (2 char code),
 *   "current": boolean,
 *   "createDate": iso8601 date,
 *   "updateDate": iso8601 date
 * }
 * </pre>
 */
public class SubscriberAddress
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 2;

    public static enum ADDRESS_TYPE {HOME, BILLING, SHIPPING};

    private int _addressId;
    private long _subscriberId;
    private ADDRESS_TYPE _type = ADDRESS_TYPE.HOME;
    private String _addr1;
    private String _addr2;
    private String _city;
    private String _state;
    private String _zip;
    private String _countryCode;
    private boolean _current;
    private Date _createDate;
    private Date _updateDate;

    public int getAddressId()
    {
        return _addressId;
    }
    public void setAddressId(int addressId)
    {
        _addressId = addressId;
    }
    public long getSubscriberId()
    {
        return _subscriberId;
    }
    public void setSubscriberId(long subscriberId)
    {
        _subscriberId = subscriberId;
    }
    public ADDRESS_TYPE getType()
    {
        return _type;
    }
    public void setType(ADDRESS_TYPE type)
    {
        _type = type;
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
    public String getZip()
    {
        return _zip;
    }
    public void setZip(String zip)
    {
        _zip = zip;
    }
    public String getCountryCode()
    {
        return _countryCode;
    }
    public void setCountryCode(String countryCode)
    {
        _countryCode = countryCode;
    }
    public boolean isCurrent()
    {
        return _current;
    }
    public void setCurrent(boolean current)
    {
        _current = current;
    }
    public Date getCreateDate()
    {
        return _createDate;
    }
    public void setCreateDate(Date createDate)
    {
        _createDate = createDate;
    }
    public Date getUpdateDate()
    {
        return _updateDate;
    }
    public void setUpdateDate(Date updateDate)
    {
        _updateDate = updateDate;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("addr1: ").append(_addr1);

        return buf.toString();
    }
}
