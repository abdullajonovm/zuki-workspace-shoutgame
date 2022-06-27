package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;

public interface IContactLog extends Comparable<IContactLog>, Serializable {

	public abstract int getAccountId();

	public abstract void setAccountId(int accountId);

	public abstract int getContactId();

	public abstract void setContactId(int contactId);

	public abstract boolean isDistContact();

	public abstract void setDistContact(boolean distContact);

	public abstract String getEmail();

	public abstract void setEmail(String email);

	public abstract Date getEventDate();

	public abstract void setEventDate(Date eventDate);

	public abstract int getEventId();

	public abstract void setEventId(int eventId);

	public abstract boolean isCreatedType();

	public abstract boolean isDeletedType();

}