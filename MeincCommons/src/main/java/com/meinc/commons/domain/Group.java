package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data container class to represent a Community created by a Subscriber.
 * 
 * @author shawker
 */
public class Group implements Serializable, Comparable<Group>
{	
  private static final long serialVersionUID = 6473594616366904484L;
  
  public static final int COMMUNITY_NONE = 1;
	public static final int COMMUNITY_COMPANY = 2;
	public static final int COMMUNITY_DIRECT_REPORTS = 3;
	public static final int COMMUNITY_TWO_LEVELS_DOWN = 4;
	public static final int COMMUNITY_RECIPIENTS = -100;
	
	private int _groupId;
	private String _name;
	private int _ownerId;
	private boolean _public;
	private boolean _hideGroupMembers;
	private boolean _accountGlobal;
	private boolean _readOnly;
  private List<Contact> _contacts;
  private List<Subscriber> _subscribers;
  private Map<String, Extension> _extensions;
	
	public Group() {
	}

	public Group(boolean accountGlobal, boolean hideGroupMembers,
			String name, int ownerId, boolean publicFlag) {
		_accountGlobal = accountGlobal;
		_hideGroupMembers = hideGroupMembers;
		this._name = name;
		_ownerId = ownerId;
		_public = publicFlag;
	}

	public int getGroupId() {
		return _groupId;
	}

	public void setGroupId(int groupId) {
		this._groupId = groupId;
	}

	public boolean isAccountGlobal() {
		return _accountGlobal;
	}

	public void setAccountGlobal(boolean accountGlobal) {
		this._accountGlobal = accountGlobal;
	}

	public boolean isHideGroupMembers() {
		return _hideGroupMembers;
	}

	public void setHideGroupMembers(boolean hideGroupMembers) {
		this._hideGroupMembers = hideGroupMembers;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public int getOwnerId() {
		return _ownerId;
	}

	public void setOwnerId(int ownerId) {
		this._ownerId = ownerId;
	}

	public boolean isPublic() {
		return _public;
	}

	public void setPublic(boolean publicFlag) {
		this._public = publicFlag;
	}

	public boolean isReadOnly() {
		return _readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this._readOnly = readOnly;
	}

  /**
   * @return Returns the contacts.
   */
  public List<Contact> getContacts()
  {
    return _contacts;
  }

  /**
   * @param contacts The contacts to set.
   */
  public void setContacts(List<Contact> contacts)
  {
    _contacts = contacts;
  }

  /**
   * @return Returns the subscribers.
   */
  public List<Subscriber> getSubscribers()
  {
    return _subscribers;
  }

  /**
   * @param subscribers The subscribers to set.
   */
  public void setSubscribers(List<Subscriber> subscribers)
  {
    _subscribers = subscribers;
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

  public int compareTo(Group o2)
  {
    Group o1 = this;
    
    if (o2 == null) return 1;
    
    String name1 = o1.getName().toLowerCase();
    String name2 = o2.getName().toLowerCase();
    
    return name1.compareTo(name2);
  }
}