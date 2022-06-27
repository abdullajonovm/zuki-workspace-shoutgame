package com.meinc.mrsoa.net.inbound;

/**
 * Represents an exception that occurred inside of internal MrSOA code (not in
 * the client, not in the service application). Such exceptions represent
 * serious bugs that should be fixed as soon as possible.
 * 
 * @author Matt
 */
public class MrSoaInternalResponderException extends Exception {

  private static final long serialVersionUID = -411880951221761197L;

  public MrSoaInternalResponderException(Throwable cause) {
    super(cause);
  }
}
