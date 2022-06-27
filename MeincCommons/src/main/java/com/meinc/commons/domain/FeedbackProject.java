package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * databean class.
 * 
 * @author shawker
 */
public class FeedbackProject implements Serializable
{
  private static final long serialVersionUID = 3440778805657073439L;
  
  private int _id;
  private String _name;
  
  public FeedbackProject()
  {
  }
  
  public FeedbackProject(int id, String name)
  {
    setId(id);
    setName(name);
  }
  
  /**
   * @return Returns the id.
   */
  public int getId()
  {
    return _id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(int id)
  {
    _id = id;
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
  
    public String toXml()
    {
      StringBuffer buf = new StringBuffer();
      
      buf.append("<project id=\"" + this.getId() + "\">");
      buf.append("<![CDATA[" + this.getName() + "]]>");
      buf.append("</project>");
      
      return buf.toString();
    }

}
