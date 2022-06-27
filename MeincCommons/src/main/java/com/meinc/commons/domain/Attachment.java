package com.meinc.commons.domain;

import java.io.Serializable;

/**
 * @author shawker
 */
public class Attachment implements Serializable
{
  private static final long serialVersionUID = 8886287745975158346L;
  
  private int _attachmentId;
  private String _filename;
  private String _type;
  private String _mimeType;
  
  public Attachment()
  {
  }
  
  public Attachment(int attachmentId, String filename, String type, String mimeType)
  {
    _attachmentId = attachmentId;
    _filename = filename;
    _type = type;
    _mimeType = mimeType;
  }
  
  public int getAttachmentId()
  {
    return _attachmentId;
  }
  public void setAttachmentId(int attachmentId)
  {
    _attachmentId = attachmentId;
  }
  public String getFilename()
  {
    return _filename;
  }
  public void setFilename(String filename)
  {
    _filename = filename;
  }
  public String getType()
  {
    return _type;
  }
  public void setType(String type)
  {
    _type = type;
  }
  public String getMimeType()
  {
    return _mimeType;
  }
  public void setMimeType(String mimeType)
  {
    _mimeType = mimeType;
  }
}
