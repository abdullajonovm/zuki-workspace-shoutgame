package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A subscriber represents a person in the system.  A subscriber can be part of 
 * multiple communities.
 * 
 * @author shawker
 */
public class Subscriber implements Serializable, Cloneable
{
  private static final long serialVersionUID = -7343561630252304638L;
  
  private int _subscriberId; 
  private String _sessionId;
  private String _firstName;
  private String _middleName;
  private String _lastName;
  private String _userName;
  private String _password;  
  private String _address1;
  private String _address2;
  private String _city;
  private String _state;
  private String _postalCode;
  private String _fax;
  private String _cellNumber;
  private Carrier _carrier;
  private String _phone;
  private String _title;
  private String _email;
  private String _department;
  private String _location;
  private String _photoUrl;
  private boolean _changePassword;
  private boolean _superUser;
  private int _managerId = 0; // Default to no manager == 0
  private boolean _eulaAccepted = false; // Default to eula not yet accepted
  private boolean _admin; /** Marks this subscriber as an account adminsitrator. */  
  private boolean _primarySubscriber; /** Marks this as the primary subscriber for the account he/she is in. */
  private boolean _active = true; /** Defaults to active by default. */
  private boolean _readOnly;
  private Country _country;
  private Date _createDate;
  private String _deviceId;
  private Map<String, Extension> _extensions;
  
  public static final int UNKNOWN_USER = 1;
  public static final int TASK_SCHEDULER_USER = 2;
  
  public Subscriber()
  {
  }
  
  public Subscriber(int subscriberId, String sessionId)
  {
  		this(subscriberId, sessionId, null,  
      null, null, Carrier.getDefaultCarrier(), null,
      null, null, 
      null, null,
      null,
      false, null, Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER),null);
  }
  
  public Subscriber(int subscriberId, String username, String firstName, String lastName, 
  		String cellNumber, Carrier carrier, Date createDate)
  {
    this(subscriberId, null, firstName, lastName, cellNumber, carrier, 
    		null, null, null, null, null,  
    		null, false, username, Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER), createDate);  	
  }

  public Subscriber(
      int subscriberId, String firstName,  
      String lastName, String cellNumber, Carrier carrier, String workNumber,
      String title, String email, 
      String department, String location, String photoUrl,
      boolean administrator, String username, Date createDate)
  {
    this(subscriberId, null, firstName, lastName, cellNumber, carrier, 
        workNumber, title, email, department, location,  
        photoUrl, administrator, username, Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER),
        createDate);
  }
  
  public Subscriber(
      int subscriberId, String sessionId, String firstName, 
      String lastName, String cellNumber, Carrier carrier, String workNumber,
      String title, String email, 
      String department, String location, String photoUrl,
      boolean administrator, String username, Country country, Date createDate)
  {
    setSubscriberId(subscriberId);
    setSessionId(sessionId);
    setFirstName(firstName);
    setLastName(lastName);
    setCellNumber(cellNumber);
    setCarrier(carrier);
    setPhone(workNumber);
    setTitle(title);
    setEmail(email);
    setDepartment(department);
    setLocation(location);
    setAdmin(administrator);
    setUserName(username);
    setEulaAccepted(false);
    setCountry(country);
    setCreateDate(createDate);
  }
  
  /**
   * @return Returns the id.
   */
  public int getSubscriberId()
  {
    return _subscriberId;
  }
  /**
   * @param subscriberId The id to set.
   */
  public void setSubscriberId(int subscriberId)
  {
    _subscriberId = subscriberId;
  }
  
  /**
   * @return Returns the cellNumber.
   */
  public String getCellNumber()
  {
    return _cellNumber;
  }
  /**
   * @param cellNumber The cellNumber to set.
   */
  public void setCellNumber(String cellNumber)
  {
    _cellNumber = cellNumber;
  }
  /**
   * @return Returns the firstName.
   */
  public String getFirstName()
  {
    return _firstName;
  }
  /**
   * @param firstName The firstName to set.
   */
  public void setFirstName(String firstName)
  {
    _firstName = firstName;
  }
  /**
   * @return Returns the lastName.
   */
  public String getLastName()
  {
    return _lastName;
  }
  /**
   * @param lastName The lastName to set.
   */
  public void setLastName(String lastName)
  {
    _lastName = lastName;
  }
  /**
   * @return Returns the middleName.
   */
  public String getMiddleName()
  {
    return _middleName;
  }
  /**
   * @param middleName The middleName to set.
   */
  public void setMiddleName(String middleName)
  {
    _middleName = middleName;
  }
  /**
   * @return Returns the title.
   */
  public String getTitle()
  {
    return _title;
  }
  /**
   * @param title The title to set.
   */
  public void setTitle(String title)
  {
    _title = title;
  }
  /**
   * @return Returns the email.
   */
  public String getEmail()
  {
    return _email;
  }
  /**
   * @param email The email to set.
   */
  public void setEmail(String email)
  {
    _email = email;
  }

  /**
   * @return Returns the department.
   */
  public String getDepartment()
  {
    return _department;
  }
  /**
   * @param department The department to set.
   */
  public void setDepartment(String department)
  {
    _department = department;
  }
  
  /**
   * @return Returns the workNumber.
   */
  public String getPhone()
  {
    return _phone;
  }
  /**
   * @param workNumber The workNumber to set.
   */
  public void setPhone(String workNumber)
  {
    _phone = workNumber;
  }

  /**
   * @return Returns the location.
   */
  public String getLocation()
  {
    return _location;
  }
  /**
   * @param location The location to set.
   */
  public void setLocation(String location)
  {
    _location = location;
  }
  
    public String getPhotoUrl() {
        return _photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        _photoUrl = photoUrl;
    }

  /**
   * @return Returns the sessionId.
   */
  public String getSessionId()
  {
    return _sessionId;
  }
  /**
   * @param sessionId The sessionId to set.
   */
  public void setSessionId(String sessionId)
  {
    _sessionId = sessionId;
  }
    
  public boolean isAdmin()
  {
    return _admin;
  }
  
  public void setAdmin(boolean admin)
  {
    _admin = admin;
  }
  
  public boolean isManager()
  {
    // If the manager ID is 0, it means either the person is not a manager or
    // that the member hasn't been set.
    return _managerId != 0;
  }
    
	public Carrier getCarrier()
	{
		return _carrier;
	}

	public void setCarrier(Carrier carrier)
	{
		_carrier = carrier;
	}

	public boolean isActive() {
		return _active;
	}

	public void setActive(boolean activeFlag) {
		_active = activeFlag;
	}

  /**
   * @return Returns the changePassword.
   */
  public boolean isChangePassword()
  {
    return _changePassword;
  }

  /**
   * @param changePassword The changePassword to set.
   */
  public void setChangePassword(boolean changePassword)
  {
    _changePassword = changePassword;
  }

  /**
   * @return Returns the userName.
   */
  public String getUserName()
  {
    return _userName;
  }

  /**
   * @param userName The userName to set.
   */
  public void setUserName(String userName)
  {
    _userName = userName;
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
   * @return Returns the password.
   */
  public String getPassword()
  {
    return _password;
  }

  /**
   * @param password The password to set.
   */
  public void setPassword(String password)
  {
    _password = password;
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
   * @return Returns the superUser.
   */
  public boolean isSuperUser()
  {
    return _superUser;
  }

  /**
   * @param superUser The superUser to set.
   */
  public void setSuperUser(boolean superUser)
  {
    _superUser = superUser;
  }
  
  public String getFullName()
  {
    String fName = _firstName;
    String lName = _lastName;
    
    if (fName == null)
      fName = "";
    
    if (lName == null)
      lName = "";
    
    return fName + " " + lName;    
  }

  /**
   * @return Returns the managerId.
   */
  public int getManagerId()
  {
    return _managerId;
  }

  /**
   * @param managerId The managerId to set.
   */
  public void setManagerId(int managerId)
  {
    _managerId = managerId;
  }

  /**
   * @return Returns the primarySubscriber.
   */
  public boolean isPrimarySubscriber()
  {
    return _primarySubscriber;
  }

  /**
   * @param primarySubscriber The primarySubscriber to set.
   */
  public void setPrimarySubscriber(boolean primarySubscriber)
  {
    _primarySubscriber = primarySubscriber;
  }  
  
  /**
   * Getter for readOnly member.
  
   * @return Subscriber Returns the readOnly.
   */
  public boolean isReadOnly()
  {
    return _readOnly;
  }

  /**
   * Setter for member readOnly.
  
   * @param readOnly The readOnly to set.
   */
  public void setReadOnly(boolean readOnly)
  {
    _readOnly = readOnly;
  }
  
  /**
   * Getter for eulaAccepted member.
  
   * @return boolean Returns the eulaAccepted.
   */
  public boolean isEulaAccepted()
  {
    return _eulaAccepted;
  }

  /**
   * Setter for member eulaAccepted.
  
   * @param eulaAccepted The eulaAccepted value to set.
   */
  public void setEulaAccepted(boolean eulaAccepted)
  {
  	_eulaAccepted = eulaAccepted;
  }
  
	public Country getCountry()
	{
		return _country;
	}

	public void setCountry(Country country)
	{
		_country = country;
	}
	
	public Date getCreateDate()
	{
		return _createDate;
	}

	public void setCreateDate(Date createDate)
	{
		_createDate = createDate;
	}
	
  public String getDeviceId()
	{
		return _deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		_deviceId = deviceId;
	}

	public Map<String, Extension> getExtensions()
	{
		if (_extensions == null)
			_extensions = new HashMap<String, Extension>();
		return _extensions;
	}

	public void setExtensions(Map<String, Extension> extensions)
	{
		_extensions = extensions;
	}
	
	public String extensionAsString(String extName) {
	    String result = null;
	    Map<String, Extension> extensions = getExtensions();
	    Extension ext = extensions.get(extName);
	    if (ext != null) {
	        byte[] data = ext.getValue();
	        if (data != null) {
	            result = new String(data);
	        }
	    }
	    return result;
	}
	
	public boolean extensionAsBoolean(String extName) {
	    String ext = extensionAsString(extName);
	    return Boolean.valueOf(ext);
	}
	
	public Integer extensionAsInteger(String extName) {
	    String ext = extensionAsString(extName);
	    if (ext != null) {
	        try {
	            return new Integer(ext);
	        }
	        catch (NumberFormatException e) {
	            return null;
	        }
	    }
	    return null;
	}

	/**
   * Copy the members from the Subscriber passed in over the top of this instance,
   * except for subscriber ID, password and username.
   *
   * @param subscriber The subscriber to copy members from.
   */
  public void copyMembers(Subscriber subscriber)
  {   
    _firstName = subscriber.getFirstName(); 
    _middleName = subscriber.getMiddleName();
    _lastName = subscriber.getLastName();
    _address1 = subscriber.getAddress1();
    _address2 = subscriber.getAddress2();
    _city = subscriber.getCity();
    _state = subscriber.getState();
    _postalCode = subscriber.getPostalCode();
    _fax = subscriber.getFax();
    _cellNumber = subscriber.getCellNumber();
    _carrier = subscriber.getCarrier();
    _phone = subscriber.getPhone();
    _title = subscriber.getTitle();
    _email = subscriber.getEmail();
    _department = subscriber.getDepartment();
    _location = subscriber.getLocation();
    _changePassword = subscriber.isChangePassword();
    _superUser = subscriber.isSuperUser();
    _managerId = subscriber.getManagerId();
    _eulaAccepted = subscriber.isEulaAccepted();
    _createDate = subscriber.getCreateDate();
    _deviceId = subscriber.getDeviceId();
    _extensions = subscriber.getExtensions();
  }

  public boolean equals(Object obj)
  {
    return (obj != null && obj instanceof Subscriber && 
            _subscriberId == ((Subscriber) obj).getSubscriberId());
  }
  
  @Override
  public Subscriber clone() throws CloneNotSupportedException
  {
  	Subscriber cloned = (Subscriber) super.clone();
  	if (_carrier != null)
  		cloned.setCarrier(_carrier.clone());
  	if (_country != null)
  		cloned.setCountry(_country.clone());
  	return cloned;
  }

}
