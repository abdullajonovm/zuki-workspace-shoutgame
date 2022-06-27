package com.meinc.mrsoa.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.service.ServiceEndpoint;

/**
 * Provides a global registry of services as well as methods whereby local
 * classes can listen to changes to the global registry.
 * 
 * @author Matt
 */
public class MrSoaServiceMonitor extends Thread {
  private static final Log log = LogFactory.getLog(MrSoaServiceMonitor.class);
  private static MrSoaServiceMonitor singleton = new MrSoaServiceMonitor();
  
  /**
   * Returns the singleton service-monitor instance
   * 
   * @return The instance
   */
  public static MrSoaServiceMonitor getInstance() {
    return singleton;
  }
  
  /**
   * A Terracotta-shared object that does nothing other than allow remote
   * service-monitor objects to remotely synchronize.
   */
  private List<String> mrsoaRunMonitor = new ArrayList<String>();
  
  /**
   * A Terracotta-shared variable that each MrSOA server uses to determine
   * whether it has checked the latest changes to the Terracotta-shared maps
   * below
   */
  private int globalEventCount;
  
  /**
   * A local non-shared variable that is used by the local MrSOA server to
   * determne whether it has checked the latest changes to the Terracotta-shared
   * maps below
   */
  private int localEventCount;
  
  /**
   * A Terracotta-shared Map of service-interface-names to
   * list-of-service-endpoints
   */
  private Map<String,List<ServiceEndpointPair>> serviceInterfaceToServices = new HashMap<String,List<ServiceEndpointPair>>();
  
  /**
   * A Terracotta-shared Map of live service endpoints
   */
  private Map<ServiceEndpointPair,ServiceEndpointPair> liveServices = new HashMap<ServiceEndpointPair,ServiceEndpointPair>();
  
  /**
   * A local non-shared map of local listeners interested in changes to the
   * interfaces map above
   */
  private Map<String,List<IMrSoaServiceMonitorListener>> interfaceListeners = new HashMap<String,List<IMrSoaServiceMonitorListener>>();
  
  /**
   * A local non-shared map of local listeners interested in changes to the
   * service endpoints map above
   */
  private Map<ServiceEndpoint,List<IMrSoaServiceMonitorListener>> serviceListeners = new HashMap<ServiceEndpoint,List<IMrSoaServiceMonitorListener>>();
  
  private MrSoaServiceMonitor() {
    setDaemon(true);
    setPriority(Thread.NORM_PRIORITY+1);
    start();
  }
  
  /**
   * Overrides {@link Thread#run()} by waiting for a notification that
   * a change has occurred in one of the following:
   * <ol>
   * <li>Registered listeners</li>
   * <li>Service interfaces</li>
   * <li>Service endpoints</li>
   * </ol>
   * Once a change notification is received, this method notifies any
   * appropriate registered listeners and then
   * <em>automatically unregisters these listeners</em>.
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    while (!isInterrupted()) {
      
      // Check for service interfaces that interest current listeners
      /* SYNC:
       * ? This block delays/blocks monitor release -> block does not
       *   delay/block (See following points and nested sync blocks)
       * ? registerInterfaceListener(){1} delays/blocks monitor release ->
       *   block does not delay/block
       * ? deregisterInterfaceListener(){1} delays/blocks monitor release ->
       *   block does not delay/block */
      synchronized (interfaceListeners) {
        Iterator<Entry<String,List<IMrSoaServiceMonitorListener>>> listenersIt = interfaceListeners.entrySet().iterator();
        while (listenersIt.hasNext()) {
          Entry<String,List<IMrSoaServiceMonitorListener>> listenerEntry = listenersIt.next();
          /* SYNC:
           * ? This block delays/blocks monitor release -> block does not
           *   delay/block (See following points and nested sync comments)
           * ? registerNewService(){1} delays/blocks monitor release -> block
           *   does not delay/block
           * ? deregisterService(){1} delays/blocks monitor release -> block
           *   does not delay/block
           * ? getEndpointsImplementingInterface(){1} delays/blocks monitor
           *   release -> block does not delay/block */
          synchronized (serviceInterfaceToServices) {
            List<ServiceEndpointPair> endpoints = serviceInterfaceToServices.get(listenerEntry.getKey());
            if (endpoints != null) {
              for (IMrSoaServiceMonitorListener listener : listenerEntry.getValue()) {
                try {
                  /* SYNC:
                   * ? Method hangs/delays returning -> This is outside the
                   *   scope of our control. serviceEvent() implementations
                   *   should make sure to never hang/delay. */
                  listener.serviceEvent(endpoints);
                } catch (Throwable e) {
                  log.error("ServiceMonitorListener caused error", e);
                }
              }
              listenersIt.remove();
            }
          }
        }
      }
      
      // Check for service endpoints that interest current listeners
      /* SYNC:
       * ? This block delays/blocks monitor release -> block does not
       *   delay/block (See following points and nested sync block)
       * ? registerServiceListener(){1} delays/blocks monitor release -> block
       *   does not delay/block
       * ? deregisterServiceListener(){1} delays/blocks monitor release -> block
       *   does not delay/block */
      synchronized (serviceListeners) {
        Iterator<Entry<ServiceEndpoint,List<IMrSoaServiceMonitorListener>>> listenersIt = serviceListeners.entrySet().iterator();
        while (listenersIt.hasNext()) {
          Entry<ServiceEndpoint,List<IMrSoaServiceMonitorListener>> listenerEntry = listenersIt.next();
          /* SYNC:
           * ? This block delays/blocks monitor release -> block does not
           *   delay/block (See following points and nested sync comments)
           * ? registerNewService(){3} delays/blocks monitor release -> block
           *   does not delay/block
           * ? deregisterService(){2} delays/blocks monitor release -> block
           *   does not delay/block */
          synchronized (liveServices) {
            if (liveServices.containsKey(listenerEntry.getKey())) {
              for (IMrSoaServiceMonitorListener listener : listenerEntry.getValue()) {
                try {
                  List<ServiceEndpoint> endpoints = new ArrayList<ServiceEndpoint>();
                  endpoints.add(listenerEntry.getKey());
                  /* SYNC:
                   * ? Method hangs/delays returning -> This is outside the
                   *   scope of our control. serviceEvent() implementations
                   *   should make sure to never hang/delay. */
                  listener.serviceEvent(endpoints);
                } catch (Throwable e) {
                  log.error("ServiceMonitor Listener caused error", e);
                }
              }
              listenersIt.remove();
            }
          }
        }
      }
      
      // Wait for a notification that something has changed
      /* SYNC:
       * ? This block blocks monitor release -> block does not block (See
       *   following points)
       * ? registerNewService(){2,4} delays/blocks monitor release -> blocks
       *   do not delay/block
       * ? registerInterfaceListener(){2} delays/blocks monitor release ->
       *   block does not delay/block
       * ? registerServiceListener(){2} delays/blocks monitor release -> block
       *   does not delay/block */
      synchronized (mrsoaRunMonitor) {
        if (localEventCount != globalEventCount) {
          localEventCount = globalEventCount;
        } else {
          try {
            mrsoaRunMonitor.wait(3000);
          } catch (InterruptedException e) {
            interrupt();
            continue;
          }
        }
      }
    }
  }
  
  /**
   * Registers a service in the global MrSOA registry.
   * 
   * @param endpoint
   *          The service endpoint
   * @param interfaceNames
   *          The interfaces that this service implements
   */
  public void registerNewService(ServiceEndpoint endpoint, List<String> interfaceNames) {
    /* SYNC: See run(){2} */
    synchronized (serviceInterfaceToServices) {
      for (String interfaceName : interfaceNames) {
        List<ServiceEndpointPair> endpoints = serviceInterfaceToServices.get(interfaceName);
        if (endpoints == null) {
          endpoints = new ArrayList<ServiceEndpointPair>();
          serviceInterfaceToServices.put(interfaceName, endpoints);
        }
        int endpointIndex = endpoints.indexOf(endpoint);
        if (endpointIndex == -1) {
          endpoints.add(new ServiceEndpointPair(endpoint));
          /* SYNC: See run(){5} */
          synchronized (mrsoaRunMonitor) {
            // Notify all waiting remote monitors that a change has occurred
            globalEventCount += 1;
            mrsoaRunMonitor.notifyAll();
          }
        } else {
          ServiceEndpointPair endpointPair = endpoints.get(endpointIndex);
          endpointPair.increment();
        }
      }
    }
    
    /* SYNC: See run(){4} */
    synchronized (liveServices) {
      ServiceEndpointPair endpointPair = liveServices.get(endpoint);
      if (endpointPair == null) {
        endpointPair = new ServiceEndpointPair(endpoint);
        liveServices.put(endpointPair, endpointPair);
        /* SYNC: See run(){5} */
        synchronized (mrsoaRunMonitor) {
          // Notify all waiting remote monitors that a change has occurred
          globalEventCount += 1;
          mrsoaRunMonitor.notifyAll();
        }
      } else
        endpointPair.increment();
    }
  }
  
  /**
   * Unregisters a service from the global registry.
   * 
   * @param endpoint
   *          The service
   */
  public void deregisterService(ServiceEndpoint endpoint) {
    /* SYNC: See run(){2} */
    synchronized (serviceInterfaceToServices) {
      Iterator<Entry<String,List<ServiceEndpointPair>>> entriesIt = serviceInterfaceToServices.entrySet().iterator();
      while (entriesIt.hasNext()) {
        Entry<String,List<ServiceEndpointPair>> entry = entriesIt.next();
        List<ServiceEndpointPair> endpoints = entry.getValue();
        int endpointIndex = endpoints.indexOf(endpoint);
        if (endpointIndex != -1) {
          ServiceEndpointPair endpointPair = endpoints.get(endpointIndex);
          if (endpointPair.decrement() == 0) {
            endpoints.remove(endpoint);
            if (endpoints.isEmpty())
              entriesIt.remove();
          }
        }
      }
    }
    
    /* SYNC: See run(){4} */
    synchronized (liveServices) {
      ServiceEndpointPair endpointPair = liveServices.get(endpoint);
      if (endpointPair != null) {
        if (endpointPair.decrement() == 0)
          liveServices.remove(endpoint);
      }
    }
  }
  
  /**
   * Registers a listener to receive an event when the provided interface is
   * implemented by at least one service.
   * 
   * @param listener
   *          The listener to register
   * @param serviceInterfaceName
   *          The interface that will trigger an event
   * @see #deregisterInterfaceListener(IMrSoaServiceMonitorListener)
   */
  public void registerInterfaceListener(IMrSoaServiceMonitorListener listener, String serviceInterfaceName) {
    /* SYNC: See run(){1} */
    synchronized (interfaceListeners) {
      List<IMrSoaServiceMonitorListener> listeners = interfaceListeners.get(serviceInterfaceName);
      if (listeners == null) {
        listeners = new ArrayList<IMrSoaServiceMonitorListener>();
        interfaceListeners.put(serviceInterfaceName, listeners);
      }
      listeners.add(listener);
    }
    /* SYNC: See run(){5} */
    synchronized (mrsoaRunMonitor) {
      localEventCount -= 1;
      mrsoaRunMonitor.notifyAll();
    }
  }
  
  /**
   * Unregisters a listener so that it will no longer receive an event when the
   * provided interface is implemented by a service.
   * 
   * @param listener
   *          The listener
   * @see #registerInterfaceListener(IMrSoaServiceMonitorListener, String)
   */
  public void deregisterInterfaceListener(IMrSoaServiceMonitorListener listener) {
    /* SYNC: See run(){1} */
    synchronized (interfaceListeners) {
      Iterator<Entry<String,List<IMrSoaServiceMonitorListener>>> entryIt = interfaceListeners.entrySet().iterator();
      while (entryIt.hasNext()) {
        Entry<String,List<IMrSoaServiceMonitorListener>> entry = entryIt.next();
        List<IMrSoaServiceMonitorListener> listeners = entry.getValue();
        listeners.remove(listener);
        if (listeners.isEmpty())
          entryIt.remove();
      }
    }
  }

  /**
   * Returns a list of endpoints currently implementing the provided interface.
   * 
   * @param interfaceName
   *          The interface
   * @return A list of endpoints
   */
  public List<? extends ServiceEndpoint> getEndpointsImplementingInterface(String interfaceName) {
    /* SYNC: run(){2} */
    synchronized (serviceInterfaceToServices) {
      List<ServiceEndpointPair> endpoints = serviceInterfaceToServices.get(interfaceName);
      return endpoints;
    }
  }
  
  /**
   * Registers a listener to receive an event when the provided service becomes
   * available.
   * 
   * @param listener
   *          The listener to receive the event
   * @param service
   *          The service to watch for
   * @see #deregisterServiceListener(IMrSoaServiceMonitorListener)
   */
  public void registerServiceListener(IMrSoaServiceMonitorListener listener, ServiceEndpoint service) {
    /* SYNC: See run(){3} */
    synchronized (serviceListeners) {
      List<IMrSoaServiceMonitorListener> listeners = serviceListeners.get(service);
      if (listeners == null) {
        listeners = new ArrayList<IMrSoaServiceMonitorListener>();
        serviceListeners.put(service, listeners);
      }
      listeners.add(listener);
    }
    /* SYNC: See run(){5} */
    synchronized (mrsoaRunMonitor) {
      localEventCount -= 1;
      mrsoaRunMonitor.notifyAll();
    }
  }

  /**
   * Unregisters a listener so that it no longer receives an event when the
   * provided service becomes available.
   * 
   * @param listener
   *          The listener to unregister
   * @see #registerServiceListener(IMrSoaServiceMonitorListener, ServiceEndpoint)
   */
  public void deregisterServiceListener(IMrSoaServiceMonitorListener listener) {
    /* SYNC: See run(){3} */
    synchronized (serviceListeners) {
      Iterator<Entry<ServiceEndpoint,List<IMrSoaServiceMonitorListener>>> entryIt = serviceListeners.entrySet().iterator();
      while (entryIt.hasNext()) {
        Entry<ServiceEndpoint,List<IMrSoaServiceMonitorListener>> entry = entryIt.next();
        List<IMrSoaServiceMonitorListener> listeners = entry.getValue();
        listeners.remove(listener);
        if (listeners.isEmpty())
          entryIt.remove();
      }
    }
  }
}


/**
 * Extension of {@link ServiceEndpoint} that provides a count that may be
 * incremented and decremented. Note that the count is initialized to one.
 * 
 * @author Matt
 */
class ServiceEndpointPair extends ServiceEndpoint {
  private static final long serialVersionUID = -4981277836350930886L;
  
  private int count = 1;
  
  public ServiceEndpointPair(ServiceEndpoint endpoint) {
    String ns = endpoint.getNamespace();
    super.namespace = (DEFAULT_NAMESPACE.equals(ns) ? null : ns);
    super.serviceName = endpoint.getServiceName();
    super.version = endpoint.getVersion();
  }

  /**
   * Increments the endpoint's count
   * 
   * @return The count value after performing an increment operation
   */
  protected int increment() {
    return this.count += 1;
  }

  /**
   * Decrements the endpoint's count
   * 
   * @return The count value after performing a decrement operation
   */
  protected int decrement() {
    return this.count -= 1;
  }
}
