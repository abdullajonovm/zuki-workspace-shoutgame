package com.meinc.commons.billing;

import java.util.Date;
import java.util.List;

import com.meinc.commons.domain.Contact;
import com.meinc.commons.domain.IContactLog;
import com.meinc.commons.domain.ISubscriberLog;

public interface IBillingApi {

	public abstract void logInsertContactEvent(int accountId, int contactId,
			String contactEmail, boolean isDistContact);

	public abstract void logInsertContactBatchEvent(int accountId, List<Contact> contacts, boolean areDistContacts);

	public abstract void logDeleteContactEvent(int accountId, int contactId,
			String contactEmail, boolean isDistContact);

	public abstract void logInsertSubscriberEvent(int accountId,
			int subscriberId, String subscriberUsername);

	public abstract void logDeleteSubscriberEvent(int accountId,
			int subscriberId, String subscriberUsername);

	public abstract List<IContactLog> getContactEventsBeforeDate(int accountId,
			Date beforeDate);

	public abstract List<ISubscriberLog> getSubscriberEventsBeforeDate(
			int accountId, Date beforeDate);

	public abstract boolean isAccountPaying(int accountId);
	
	public abstract void logInsertAccountEvent(int accountId, String accountType);
	
	public abstract void logDeleteAccountEvent(int accountId);
	
	public abstract void logActivateAccountEvent(int accountId);
	
	public abstract void logSuspendAccountEvent(int accountId);
	
	public abstract void logConvertAccountEvent(int accountId, String type);

	public abstract void logDeleteContactsBatchEvent(int accountId, int subscriberId, List<Contact> contacts);

}