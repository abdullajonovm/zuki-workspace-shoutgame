package com.meinc.mrsoa.net;

/**
 * Represents a networking exception that has occurred during a service request
 * or response to or from a MrSOA service.
 * 
 * @author Matt
 */
public class MrSoaNetworkingException extends RuntimeException {

  private static final long serialVersionUID = 5498181613029501876L;

  public MrSoaNetworkingException(String message) {
    super(message);
  }

  public MrSoaNetworkingException(String message, Throwable cause) {
    super(message, cause);
  }
}
