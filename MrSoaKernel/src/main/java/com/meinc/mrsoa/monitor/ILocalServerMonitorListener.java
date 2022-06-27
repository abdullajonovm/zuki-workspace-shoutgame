package com.meinc.mrsoa.monitor;

/**
 * Receives server-score change updates from a {@link LocalServerMonitor} instance.
 * 
 * @see LocalServerMonitor#registerMonitorListener(ILocalServerMonitorListener)
 * @see LocalServerMonitor#unregisterMonitorListener(ILocalServerMonitorListener)
 * @author Matt
 */
public interface ILocalServerMonitorListener {
  /**
   * Signals the listener that the local server score has changed.
   * <b>This method must return as quickly as possible and never block or 
   * delay!</b>
   * 
   * @param score The new server score
   */
  public void serverScoreUpdated(int score);
}
