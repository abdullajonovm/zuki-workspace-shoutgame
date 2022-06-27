package com.meinc.mrsoa.net.inbound;

/**
 * Represents the exception that occurs when a method specified by a request
 * could not be found in the service specified by the request.
 * 
 * @author Matt
 */
public class MrSoaServiceMethodNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 776105461084006203L;

  /**
   * Creates the exception with the provided service and method history.
   * 
   * @param serviceDescriptor
   *          The service descriptor in which a method could not be found
   * @param serviceMethodName
   *          The method which could not be found
   */
  public MrSoaServiceMethodNotFoundException(String serviceDescriptor, String serviceMethodName) {
    super("Service method " + serviceMethodName + " could not be found in service " + serviceDescriptor);
  }
}
