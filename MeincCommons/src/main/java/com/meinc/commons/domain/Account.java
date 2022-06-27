package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.List;

public class Account implements Serializable
{
  private static final long serialVersionUID = 6004860621321265501L;

  // These values must match the account types in the database character for
  // character
  public static enum AccountType {
	  BETA,
	  TRIAL,
	  PAYING,
	  SALESAGENT,
	  INTERNAL,
	  RESELLER,
	  RESELLER_CUSTOMER
  }

  // These values must match the account statuses in the database character
  // for character
  public static enum AccountStatus {
	  INACTIVE,
	  SUSPENDED,
	  ACTIVE,
  }

  private int _accountId;
  private int _referenceId;
	private String _name;
	private List<String> _accountOwners;
  private List<Subscriber> _subscribers;
  private String _address1;
  private String _address2;
  private String _city;
  private String _state;
  private String _postalCode;
  private String _phone;
  private String _fax;
  private String _subdomain;
  private AccountStatus _status;
  private AccountType _type;
  private Country _country;
  private boolean _supported;

	public Account()
	{
		_type = AccountType.PAYING;
		_status = AccountStatus.ACTIVE;
		_referenceId = 0;
	}

	public Account(int accountId, String name, List<String> accountOwners)
	{
		_accountId = accountId;
		_name = name;
		_accountOwners = accountOwners;
		_referenceId = 0;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public void setAccountId(int accountId)
	{
		_accountId = accountId;
	}

	public int getReferenceId()
	{
		return _referenceId;
	}

	public void setReferenceId(int referenceId)
	{
		_referenceId = referenceId;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public List<String> getAccountOwners()
	{
		return _accountOwners;
	}

	public void setAccountOwners(List<String> accountOwners)
	{
		_accountOwners = accountOwners;
	}

  public List<Subscriber> getSubscribers()
  {
    return _subscribers;
  }

  public void setSubscribers(List<Subscriber> subscribers)
  {
    _subscribers = subscribers;
  }

  /**
   * @return Returns the address1.
   */
  public String getAddress1()
  {
    return _address1;
  }

  /**
   * @param address1 The address1 to set.
   */
  public void setAddress1(String address1)
  {
    _address1 = address1;
  }

  /**
   * @return Returns the address2.
   */
  public String getAddress2()
  {
    return _address2;
  }

  /**
   * @param address2 The address2 to set.
   */
  public void setAddress2(String address2)
  {
    _address2 = address2;
  }

  /**
   * @return Returns the city.
   */
  public String getCity()
  {
    return _city;
  }

  /**
   * @param city The city to set.
   */
  public void setCity(String city)
  {
    _city = city;
  }

  /**
   * @return Returns the fax.
   */
  public String getFax()
  {
    return _fax;
  }

  /**
   * @param fax The fax to set.
   */
  public void setFax(String fax)
  {
    _fax = fax;
  }

  /**
   * @return Returns the phone.
   */
  public String getPhone()
  {
    return _phone;
  }

  /**
   * @param phone The phone to set.
   */
  public void setPhone(String phone)
  {
    _phone = phone;
  }

  /**
   * @return Returns the postalCode.
   */
  public String getPostalCode()
  {
    return _postalCode;
  }

  /**
   * @param postalCode The postalCode to set.
   */
  public void setPostalCode(String postalCode)
  {
    _postalCode = postalCode;
  }

  /**
   * @return Returns the state.
   */
  public String getState()
  {
    return _state;
  }

  /**
   * @param state The state to set.
   */
  public void setState(String state)
  {
    _state = state;
  }

  /**
   * @return Returns the subdomain.
   */
  public String getSubdomain()
  {
    return _subdomain;
  }

  /**
   * @param subdomain The subdomain to set.
   */
  public void setSubdomain(String subdomain)
  {
    _subdomain = subdomain;
  }

	/**
	 * @return The account status
	 */
	public AccountStatus getStatus() {
		return _status;
	}

	/**
	 * CLIENT CODE SHOULD NOT USE THIS METHOD.
	 * @return The account status string
	 */
	public String getStatusString() {
		return _status.name();
	}

	/**
	 * @param status The account status
	 */
	public void setStatus(AccountStatus status) {
		this._status = status;
	}

	/**
	 * Sets account status using a string.
	 * CLIENT CODE SHOULD NOT USE THIS METHOD.
	 * @param status The account status string
	 */
	public void setStatusString(String status) {
		this._status = AccountStatus.valueOf(status);
	}

	/**
	 * @return The account type
	 */
	public AccountType getType() {
		return _type;
	}

	/**
	 * CLIENT CODE SHOULD NOT USE THIS METHOD.
	 * @return The account type string
	 */
	public String getTypeString() {
		return _type.name();
	}

	/**
	 * @param type The account type
	 */
	public void setType(AccountType type) {
		this._type = type;
	}

	/**
	 * Sets account type using a string
	 * CLIENT CODE SHOULD NOT USE THIS METHOD.
	 * @param type The account type string
	 */
	public void setTypeString(String type) {
		this._type = AccountType.valueOf(type);
	}

	public Country getCountry()
	{
		return _country;
	}

	public void setCountry(Country country)
	{
		_country = country;
	}

	public boolean isSupported()
	{
		return _supported;
	}

	public void setSupported(boolean supported)
	{
		_supported = supported;
	}
}
