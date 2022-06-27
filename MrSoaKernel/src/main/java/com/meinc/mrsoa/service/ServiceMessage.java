package com.meinc.mrsoa.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.monitor.IMrSoaServiceMonitorListener;
import com.meinc.mrsoa.monitor.MrSoaServerMonitor;
import com.meinc.mrsoa.monitor.MrSoaServiceMonitor;
import com.meinc.mrsoa.net.outbound.MrSoaRequester;

public abstract class ServiceMessage {
	private static Log log = LogFactory.getLog(ServiceMessage.class);
	private static MrSoaRequester requester = MrSoaRequester.getInstance();
  private static GenericObjectPool endpointPool;
  
  static {
    EndpointFactory endpointFactory = new EndpointFactory();
    endpointPool = new GenericObjectPool(endpointFactory);
    
    // Enforce a minimum of n idle responders at any given time
    endpointPool.setMinIdle(1);
    // Allow a given responder n minutes of idleness before considering its destruction
    endpointPool.setSoftMinEvictableIdleTimeMillis(10 * 60 * 1000); // 10 minutes
    // Since we are using Soft Timeouts (see previous line) we want to disable Hard Timeouts
    // A Soft Timeout respects MinIdle, but a Hard Timeout does not
    endpointPool.setMinEvictableIdleTimeMillis(0);
    // We don't want any hard limit on the number of idle responders other than
    // when each responder times out
    endpointPool.setMaxIdle(-1);
    // Check for responders to be destroyed every n minutes
    endpointPool.setTimeBetweenEvictionRunsMillis(5 * 60 * 1000); // 5 minutes
    // Only evict a max 1/abs(n) of idle responders per eviction run
    endpointPool.setNumTestsPerEvictionRun(-2);
    // Maximum number of responders allowed in the pool at one time
    // Set to unlimited
    endpointPool.setMaxActive(-1);
  }
	
	public static Object send(ServiceEndpoint endpoint, String methodName, Object...args)	{
		return requester.invokeMethod(endpoint, methodName, false, false, args);
	}
	
	public static Object sendFast(ServiceEndpoint endpoint, String methodName, Object...args)	{
		return requester.invokeMethod(endpoint, methodName, false, true, args);
	}
	
	public static Object send(String namespace, String serviceName, String methodName, Object...args) {
		ServiceEndpoint serviceEndpoint;
    try {
      serviceEndpoint = (ServiceEndpoint) endpointPool.borrowObject();
    } catch (Exception e) {
      throw new RuntimeException("Error while obtaining service endpoint", e);
    }
		try {
		  serviceEndpoint.setNamespace(namespace);
		  serviceEndpoint.setServiceName(serviceName);
		  return send(serviceEndpoint, methodName, args);
		} finally {
		  try {
        endpointPool.returnObject(serviceEndpoint);
      } catch (Exception e) {
        throw new RuntimeException("Error while returning service endpoint", e);
      }
		}
	}

  public static Object send(String serviceName, String methodName, Object...args) {
    return send(null, serviceName, methodName, args);
  }
  
  @SuppressWarnings("unchecked")
  public static List<ServiceEndpoint> getEndpointsImplementingInterface(String interfaceName) {
    return (List<ServiceEndpoint>) MrSoaServiceMonitor.getInstance().getEndpointsImplementingInterface(interfaceName);
  }
	
  public static List<ServiceEndpoint> waitForEndpointImplementingInterface(String interfaceName) {
    final MrSoaServiceMonitor monitor = MrSoaServiceMonitor.getInstance();
    final List<ServiceEndpoint> serviceList = new ArrayList<ServiceEndpoint>();
    
    IMrSoaServiceMonitorListener listener = new IMrSoaServiceMonitorListener() {
      public void serviceEvent(List<? extends ServiceEndpoint> matchingEndpoints) {
        /* SYNC:
         * ? This block delays/blocks release of monitor -> Block does not
         *   delay/block
         * ? waitForEndpointImplementingInterface(){2} delays/blocks release of
         *   monitor -> block frequently releases monitor and does not
         *   otherwise delay */
        synchronized (serviceList) {
          serviceList.addAll(matchingEndpoints);
          serviceList.notify();
        }
      }
    };

    /* SYNC: See waitForEndpointImplementingInterface(){1} */
    synchronized (serviceList) {
      Throwable t = null;
      int waitMillis = 5000;
      String methodString = "[unknown]";
      monitor.registerInterfaceListener(listener, interfaceName);
      while (true) {
        try {
          serviceList.wait(waitMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Error while waiting for service interfaces", e);
        }
        
        if (!serviceList.isEmpty()) {
          if (t != null)
            log.info("*** METHOD " + methodString + " FOUND SERVICE OF INTERFACE " + interfaceName + " ***");
          break;
        }
        
        if (t == null) {
          t = new Throwable();
          StackTraceElement[] trace = t.getStackTrace();
          for (StackTraceElement element : trace) {
            if (!ServiceMessage.class.getName().equals(element.getClassName()) &&
                !element.getClassName().startsWith("clientproxy.")) {
              String className = element.getClassName().substring(
                  element.getClassName().lastIndexOf('.') + 1);
              methodString = className + "." + element.getMethodName();
              break;
            }
          }
        }
        
        log.info("*** METHOD " + methodString + " WAITING FOR SERVICE OF INTERFACE " + interfaceName + " ***");
        
        if (waitMillis < 30000)
          waitMillis = 30000;
        else if (waitMillis < 60000)
          waitMillis = 60000;
        else if (waitMillis < 150000)
          waitMillis = 60000;
        else if (waitMillis < 300000)
          waitMillis = 300000;
      }
    }
    
    return serviceList;
  }
	
  public static void waitForServiceRegistration(ServiceEndpoint endpoint) {
    MrSoaServerMonitor serverMonitor = MrSoaServerMonitor.getInstance();
    InetSocketAddress serverAddress = serverMonitor.getNetAddressToEndpoint(endpoint.toString());
    if (serverAddress != null || (serverAddress == null && MrSoaRequester.isLocalhostFallback()))
      return;
    
    final MrSoaServiceMonitor monitor = MrSoaServiceMonitor.getInstance();
    final List<ServiceEndpoint> serviceList = new ArrayList<ServiceEndpoint>();
    
    IMrSoaServiceMonitorListener listener = new IMrSoaServiceMonitorListener() {
      public void serviceEvent(List<? extends ServiceEndpoint> matchingEndpoints) {
        /* SYNC:
         * ? This block delays/blocks release of monitor -> Block does not
         *   delay/block
         * ? waitForServiceRegistration(){2} delays/blocks release of monitor ->
         *   block frequently releases monitor and does not otherwise delay */
        synchronized (serviceList) {
          serviceList.addAll(matchingEndpoints);
          serviceList.notify();
        }
      }
    };
    
    /* SYNC: See waitForServiceRegistration(){1} */
    synchronized (serviceList) {
      Throwable t = null;
      int waitMillis = 5000;
      String methodString = "[unknown]";
      monitor.registerServiceListener(listener, endpoint);
      while (true) {
        try {
          serviceList.wait(waitMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Error while waiting for service", e);
        }
        
        if (!serviceList.isEmpty()) {
          if (t != null)
            log.info("*** METHOD " + methodString + " FOUND SERVICE " + endpoint.getNamespace()+ ":" + endpoint.getServiceName() + " ***");
          break;
        }
        
        if (t == null) {
          ServiceCallStackRow currentService = ServiceCallStack.getCurrentService();
          if (currentService != null) {
            methodString = currentService.toString();
          } else {
            t = new Throwable();
            StackTraceElement[] trace = t.getStackTrace();
            for (StackTraceElement element : trace) {
              if (!ServiceMessage.class.getName().equals(element.getClassName()) &&
                      !element.getClassName().startsWith("com.meinc.commons.") &&  //TODO:get rid of when MeincCommons is deleted
                      !element.getClassName().startsWith("clientproxy.")) {
                String className = element.getClassName().substring(
                                                                    element.getClassName().lastIndexOf('.') + 1);
                methodString = className + "." + element.getMethodName();
                break;
              }
            }
          }
        }
        
        log.info("*** METHOD " + methodString + " WAITING FOR SERVICE " + endpoint.getNamespace()+ ":" + endpoint.getServiceName() + " ***");
        if ("true".equals(ServerPropertyHolder.getProperty("service.waiting.log.stack", "false").trim().toLowerCase())) {
            Throwable t2 = new Throwable();
            log.info("Waiting Call Stack:", t2);
        }
        
        if (waitMillis < 30000)
          waitMillis = 30000;
        else if (waitMillis < 60000)
          waitMillis = 60000;
        else if (waitMillis < 150000)
          waitMillis = 60000;
        else if (waitMillis < 300000)
          waitMillis = 300000;
      }
    }
  }

	private static class EndpointFactory implements PoolableObjectFactory {
    public void activateObject(Object obj) throws Exception { }
    public void destroyObject(Object obj) throws Exception { }
    public boolean validateObject(Object obj) { return true; }
    public Object makeObject() throws Exception {
      return new ServiceEndpoint();
    }
    public void passivateObject(Object obj) throws Exception {
      ((ServiceEndpoint)obj).clear();
    }
	}
}
