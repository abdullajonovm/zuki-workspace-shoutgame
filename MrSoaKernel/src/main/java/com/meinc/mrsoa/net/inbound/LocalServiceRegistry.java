package com.meinc.mrsoa.net.inbound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a registry of services available on the local server.
 * 
 * @author Matt
 */
public class LocalServiceRegistry {
  /**
   * The actual registry implementation
   */
  private static volatile Map<String,IMrSoaService> registry = new HashMap<String,IMrSoaService>();
  
  /**
   * Registers the availability of a MrSOA service.
   * 
   * @param serviceDescriptor
   *          The descriptor of the service being made available
   * @param service
   *          The service implementation
   */
  public static synchronized void registerService(String serviceDescriptor, IMrSoaService service) {
    /* SYNC:
     * - Deadlock
     *   ? deregisterService never releases monitor -> Method always releases 
     *     monitor
     *   ? This block never releases monitor -> Block always releases monitor */
    if (registry.containsKey(serviceDescriptor))
      throw new IllegalStateException("Service " + serviceDescriptor + " already exists in service registry, ignoring registration");

    Map<String,IMrSoaService> newRegistry = new HashMap<String,IMrSoaService>(registry);
    newRegistry.put(serviceDescriptor, service);
    registry = newRegistry;
  }
  
  /**
   * Removes the specified service from the registry.
   * 
   * @param serviceDescriptor
   *          The descriptor of the service to remove
   */
  /* SYNC: See registerService(){1} */
  public static synchronized void deregisterService(String serviceDescriptor) {
    Map<String,IMrSoaService> newRegistry = new HashMap<String,IMrSoaService>(registry);
    newRegistry.remove(serviceDescriptor);
    registry = newRegistry;
  }
  
  /**
   * @param request
   * @return
   * @throws MrSoaServiceNotFoundException
   * @throws MrSoaInternalResponderException
   */
  public static IMrSoaService getService(MrSoaRequest request)
  throws MrSoaServiceNotFoundException, MrSoaInternalResponderException {
    IMrSoaService service;
    try {
      String serviceDescriptor = request.destination;
      service = registry.get(serviceDescriptor);
      if (service == null)
        throw new MrSoaServiceNotFoundException(serviceDescriptor);
      
    } catch (Throwable e) {
      if (e instanceof MrSoaServiceNotFoundException)
        throw (MrSoaServiceNotFoundException) e;
      throw new MrSoaInternalResponderException(e);
    }
    
    return service;
  }
  
  public static Map<String,IMrSoaService> getServices() {
    return new HashMap<String,IMrSoaService>(registry);
  }
}
