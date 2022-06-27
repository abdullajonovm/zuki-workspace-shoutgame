package com.meinc.commons.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * If, when attempting to determine groups, contacts, subscribers from xml,
 * any of those group/contact/subscriber ids are invalid, rather than returning
 * an exception and cancelling the entire operation, the invalid entries will
 * be stored in this object so that the operation may continue for all the 
 * entries which _were_ valid.  Then an exception which contains the invalid 
 * entries can be returned after the fact so that the sender can choose what
 * to do.
 *
 * @author shawker
 */
public class MissingData
{
	private List<Integer> _groupIds = new ArrayList<Integer>();
	private List<Integer> _subscriberIds = new ArrayList<Integer>();
	private List<Integer> _contactIds = new ArrayList<Integer>();
	
	public MissingData()
	{
	}

	public void addGroupId(int groupId)
	{
		_groupIds.add(groupId);
	}
	
	public void addSubsriberId(int subscriberId)
	{
		_subscriberIds.add(subscriberId);
	}
	
	public void addContactId(int contactId)
	{
		_contactIds.add(contactId);
	}

	public List<Integer> getContactIds()
	{
		return _contactIds;
	}

	public List<Integer> getGroupIds()
	{
		return _groupIds;
	}

	public List<Integer> getSubscriberIds()
	{
		return _subscriberIds;
	}
	
	/**
	 * Was there any missing data?
	 * 
	 * @return true if anything was added as missing, false otherwise
	 */
	public boolean isInvalid()
	{
		return 
			_groupIds.size() > 0 ||
			_subscriberIds.size() > 0 ||
			_contactIds.size() > 0;
	}
	
	public String toXml()
	{
		StringBuilder buf = new StringBuilder();
		
		buf.append("<groups>");
		for (Integer groupId : _groupIds)
			buf.append("<group id=\"" + groupId + "\" />");
		buf.append("</groups>");
		
		buf.append("<subscribers>");
		for (Integer subscriberId : _subscriberIds)
			buf.append("<subscriber id=\"" + subscriberId + "\" />");
		buf.append("</subscribers>");
		
		buf.append("<contacts>");
		for (Integer contactId : _contactIds)
			buf.append("<contact id=\"" + contactId + "\" />");
		buf.append("</contacts>");
		
		return buf.toString();
	}
	
}
