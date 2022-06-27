package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * Databean class encapsulating a location.
 * 
 * @author shawker
 */
public class Location implements Serializable
{ 
  private static final long serialVersionUID = -300475436952744025L;
  
  private String _id;
  private String _name;
  
  public Location()
  {
  }
  
  public Location(int id, String location)
  {
  	this(id+"", location);
  }
  
  public Location(String id, String location)
  {
    setId(id);
    setName(location);
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
  
  public String getName()
  {
    return _name;
  }
  
  public void setName(String location)
  {
    _name = location;
  }
  
  public String toXml()
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append("<location>");
    buf.append("<id><![CDATA[" + _id + "]]></id>");
    buf.append("<name><![CDATA[" + _name + "]]></name>");
    buf.append("</location>");
    
    return buf.toString();
  }

}
