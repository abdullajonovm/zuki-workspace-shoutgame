package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;

public interface ISubscriberLog extends Comparable<ISubscriberLog>, Serializable {

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#getAccountId()
	 */
	public abstract int getAccountId();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#setAccountId(int)
	 */
	public abstract void setAccountId(int accountId);

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#getContactId()
	 */
	public abstract int getSubscriberId();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#setContactId(int)
	 */
	public abstract void setSubscriberId(int contactId);

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#getEmail()
	 */
	public abstract String getUserName();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#setEmail(java.lang.String)
	 */
	public abstract void setUserName(String userName);

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#getEventDate()
	 */
	public abstract Date getEventDate();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#setEventDate(java.util.Date)
	 */
	public abstract void setEventDate(Date eventDate);

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#getEventId()
	 */
	public abstract int getEventId();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#setEventId(int)
	 */
	public abstract void setEventId(int eventId);

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#isCreatedType()
	 */
	public abstract boolean isCreatedType();

	/* (non-Javadoc)
	 * @see com.meinc.billing.scanner.IContactLog#isDeletedType()
	 */
	public abstract boolean isDeletedType();

}