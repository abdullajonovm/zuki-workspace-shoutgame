package com.meinc.mrsoa.net.inbound;

/**
 * Represents an object which may be reset. This interface is intended to be
 * used on synchronous monitor objects which may be reset as a signal to a
 * blocking network operation to return immediately.
 * 
 * @author Matt
 */
public interface IResettable {
  /**
   * Returns whether this object is currently in a reset state.
   * 
   * @return True if in a reset state
   */
  public boolean isReset();
}
