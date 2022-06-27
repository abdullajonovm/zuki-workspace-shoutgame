package com.meinc.mrsoa.net.inbound;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a MrSOA response from a server to a client.
 * 
 * @author Matt
 */
public class MrSoaResponse implements Serializable {
  private static final long serialVersionUID = -6180424097643895098L;
  
  /**
   * True if the response represents a Java exception
   */
  public boolean isException;
  
  /**
   * True if the response is null, False otherwise
   */
  public boolean isNull;
  
  /**
   * The result to be sent back to the client. Transient because the result is
   * sent back separately over the wire and not a part of this object.
   */
  public transient Object result;
  
  /**
   * The flattened exception to be sent back to the client. Transient because
   * the exception is sent back separately over the wire and not a part of this
   * object.
   */
  public transient List<Object> flatException;
  
  /**
   * The actual exception to be sent back to the client. Transient because
   * the exception is sent back separately over the wire and not a part of this
   * object.
   */
  public transient Throwable exception;
}
