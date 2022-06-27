/**
 * 
 */
package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * @author grant
 *
 */
public class SubscriberPhone implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4897807030482002680L;
	protected int _id;
	protected int _subscriberId;
	protected String _phoneNumber;
	protected String _activationCode;
	protected boolean _active;

	/**
	 * @return the activationCode
	 */
	public String getActivationCode() {
		return _activationCode;
	}
	/**
	 * @param activationCode the activationCode to set
	 */
	public void setActivationCode(String activationCode) {
		_activationCode = activationCode;
	}
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return _active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		_active = active;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return _id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		_id = id;
	}
	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return _phoneNumber;
	}
	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		_phoneNumber = phoneNumber;
	}
	/**
	 * @return the subscriberId
	 */
	public int getSubscriberId() {
		return _subscriberId;
	}
	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(int subscriberId) {
		_subscriberId = subscriberId;
	}

}
