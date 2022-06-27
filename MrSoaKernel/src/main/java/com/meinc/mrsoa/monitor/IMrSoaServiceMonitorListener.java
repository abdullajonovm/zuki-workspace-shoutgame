package com.meinc.mrsoa.monitor;

import java.util.List;

import com.meinc.mrsoa.service.ServiceEndpoint;

/**
 * Receives MrSOA service registration changes from a {@link MrSoaServiceMonitor} instance.
 * 
 * @see MrSoaServiceMonitor#registerServiceListener(IMrSoaServiceMonitorListener, ServiceEndpoint)
 * @see MrSoaServiceMonitor#deregisterServiceListener(IMrSoaServiceMonitorListener)
 * @see MrSoaServiceMonitor#registerInterfaceListener(IMrSoaServiceMonitorListener, String)
 * @see MrSoaServiceMonitor#deregisterInterfaceListener(IMrSoaServiceMonitorListener)
 * @author Matt
 */
public interface IMrSoaServiceMonitorListener {
  /**
   * Signals the listener that a service has changed.
   * <b>This method must return as quickly as possible and never block or 
   * delay!</b>
   * 
   * @param matchingEndpoints A list of services that have changed
   */
  public void serviceEvent(List<? extends ServiceEndpoint> matchingEndpoints);
}
