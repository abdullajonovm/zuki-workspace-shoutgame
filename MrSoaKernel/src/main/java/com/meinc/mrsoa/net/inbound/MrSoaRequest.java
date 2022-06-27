package com.meinc.mrsoa.net.inbound;

import java.io.Serializable;
import java.util.List;

import com.meinc.mrsoa.service.ServiceCallStackRow;

/**
 * Represents a MrSOA request from a client to a server.
 * 
 * @author Matt
 */
public class MrSoaRequest implements Serializable {
  private static final long serialVersionUID = -5438004322757196779L;
  
  /**
   * The service descriptor of the originating client
   */
  public String from;
  
  /**
   * The service descriptor of the destination service
   */
  public String destination;
  
  /**
   * The service call stack thus far
   */
  public List<ServiceCallStackRow> callStack;
  
  /**
   * True if this request is an internal MrSOA request
   */
  public boolean isInternalMethodCall;
  
  /**
   * The name of the service method to invoke
   */
  public String methodName;
  
  /**
   * The number of arguments to be passed to the service method
   */
  public int argsCount;
}
