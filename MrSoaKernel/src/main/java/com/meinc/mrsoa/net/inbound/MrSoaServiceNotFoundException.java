package com.meinc.mrsoa.net.inbound;

/**
 * Represents the exception that occurs when a matching service could not be
 * found on a server.
 * 
 * @author Matt
 */
public class MrSoaServiceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -8836744948779284794L;

  /**
   * Creates the exception with the provided service descriptor.
   * 
   * @param serviceDescriptor
   *          The service descriptor that could not be found
   */
  public MrSoaServiceNotFoundException(String serviceDescriptor) {
    super(serviceDescriptor + " could not be found");
  }
}
