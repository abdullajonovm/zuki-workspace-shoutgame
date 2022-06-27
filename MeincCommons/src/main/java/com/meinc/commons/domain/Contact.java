package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Contact implements Serializable
{
  private static final long serialVersionUID = -5549362615026716329L;
  public static final String DEFAULT_FOREIGN_ID = "FROM_WEB";

  private int _contactId;
	private int _ownerId;

  /** Defaults to indicate came from web instead of from mobile device. */
	private String _foreignId = DEFAULT_FOREIGN_ID;
	private String _name;
	private String _phone;
	private String _email;
	private Carrier _carrier;
	private boolean _public = false;
  private boolean _readOnly = false;
  private boolean _sendEmail = false;
  private boolean _sendSms = false;
  private boolean _syncToPhone = true;
  private Map<String, Extension> _extensions;
  private Date _createDate;

	public Contact()
	{
	}

	public Contact(
			int contactId, int ownerId, String foreignId,
			String name, String phone, String email,
			boolean publicFlag, Date createDate)
	{
		_email = email;
		_foreignId = foreignId;
		_name = name;
		_ownerId = ownerId;
		_contactId = contactId;
		_phone = phone;
		_public = publicFlag;
		_createDate = createDate;

		if (_email != null)
			_sendEmail = true;
	}

	public String getEmail()
	{
		return _email;
	}

	public void setEmail(String email)
	{
		_email = email;
	}

	public String getForeignId()
	{
		return _foreignId;
	}

	public void setForeignId(String foreignId)
	{
		_foreignId = foreignId;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public void setOwnerId(int ownerId)
	{
		_ownerId = ownerId;
	}

	public int getContactId()
	{
		return _contactId;
	}

	public void setContactId(int contactId)
	{
		_contactId = contactId;
	}

	public String getPhone()
	{
		return _phone;
	}

	public void setPhone(String phone)
	{
		_phone = phone;
	}

	public boolean isPublic() {
		return _public;
	}

	public void setPublic(boolean publicFlag) {
		_public = publicFlag;
	}

  /**
   * @return Returns the readOnly.
   */
  public boolean isReadOnly()
  {
    return _readOnly;
  }

  /**
   * @param readOnly The readOnly to set.
   */
  public void setReadOnly(boolean readOnly)
  {
    _readOnly = readOnly;
  }

	public Carrier getCarrier()
	{
		return _carrier;
	}

	public void setCarrier(Carrier carrier)
	{
		_carrier = carrier;
	}

	public boolean isSendEmail()
	{
		return _sendEmail;
	}

	public void setSendEmail(boolean sendEmail)
	{
		_sendEmail = sendEmail;
	}

	public boolean isSendSms()
	{
		return _sendSms;
	}

	public void setSendSms(boolean sendSms)
	{
		_sendSms = sendSms;
	}

	public boolean isSyncToPhone()
	{
		return _syncToPhone;
	}

	public void setSyncToPhone(boolean syncToPhone)
	{
		_syncToPhone = syncToPhone;
	}

	public Date getCreateDate()
	{
		return _createDate;
	}

	public void setCreateDate(Date createDate)
	{
		_createDate = createDate;
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

	/**
	 * Copy the members of one contact to another
	 * only copy in the stuff that can be updated by the user.
	 */
	public void copyMembers(Contact contact) {
		_name = contact.getName();
		_phone = contact.getPhone();
		_email = contact.getEmail();
		_carrier = contact.getCarrier();
		_sendEmail = contact.isSendEmail();
		_sendSms = contact.isSendSms();
		_createDate = contact.getCreateDate();
		_extensions = contact.getExtensions();
	}

  public boolean equals(Object obj)
  {
    return (obj != null && obj instanceof Contact &&
            _contactId == ((Contact) obj).getContactId());
  }

}
