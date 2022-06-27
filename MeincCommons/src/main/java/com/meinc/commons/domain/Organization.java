package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of members that have been grouped together.
 * 
 * @author shawker
 */
public class Organization implements Serializable 
{
  private static final long serialVersionUID = 9128627306273246894L;
  
  private String _id;
  private String _name;
  private String _parentId; //which organization contains this organization
  private String _ownerId; //which subscriber is in charge of this organization
  private List<Subscriber> _subscribers = new ArrayList<Subscriber>();
  
  public Organization()
  {
  }
  
  public Organization(int id, String name, int parentId, int ownerId)
  {
  	this(id+"", name, parentId+"", ownerId+"", new ArrayList<Subscriber>());
  }
  
  public Organization(String id, String name, String parentId, String ownerId, List<Subscriber> subscribers)
  {
    setId(id);
    setName(name);
    setSubscribers(subscribers);
    setParentId(parentId);
    setOwnerId(ownerId);
  }
  
  /**
   * @return Returns the subscribers.
   */
  public List<Subscriber> getSubscribers()
  {
    return _subscribers;
  }
  /**
   * @param members The subscribers to set.
   */
  public void setSubscribers(List<Subscriber> subscribers)
  {
		if (subscribers == null)
			_subscribers = new ArrayList<Subscriber>();
		else
			_subscribers = subscribers;
  }
  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return _name;
  }
  /**
   * @param name The name to set.
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * @return Returns the id.
   */
  public String getId()
  {
    return _id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(String id)
  {
    _id = id;
  }
    
  /**
   * @return Returns the ownerId.
   */
  public String getOwnerId()
  {
    return _ownerId;
  }
  /**
   * @param ownerId The ownerId to set.
   */
  public void setOwnerId(String ownerId)
  {
    _ownerId = ownerId;
  }
  /**
   * @return Returns the parentId.
   */
  public String getParentId()
  {
    return _parentId;
  }
  /**
   * @param parentId The parentId to set.
   */
  public void setParentId(String parentId)
  {
    _parentId = parentId;
  }

}
