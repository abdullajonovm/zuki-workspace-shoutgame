package com.meinc.mrsoa.net.osgi;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.meinc.mrsoa.net.inbound.IMrSoaService;
import com.meinc.mrsoa.net.inbound.LocalServiceRegistry;
import com.meinc.mrsoa.net.inbound.MrSoaReceiver;
import com.meinc.mrsoa.net.inbound.MrSoaResponderPool;
import com.meinc.mrsoa.service.ServiceEndpoint;

/**
 * OSGI Activator for the local MrSOA server. This activator will create a set
 * of {@link MrSoaReceiver} objects ready to receive MrSOA requests.
 * 
 * @author Matt
 */
@SuppressWarnings("restriction")
public class MrSoaActivator implements BundleActivator {
  private static final Log log = LogFactory.getLog(MrSoaActivator.class);
  
//  static {
//    TerracottaAdaptor.init(false);
//  }
  
  private MrSoaReceiver receiver;
  private MrSoaResponderPool mrSoaResponderPool;
  private BundleContext context;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public synchronized void start(BundleContext context) throws Exception {
    String startupLog = format("\n\n" +
                               "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓\n" +
                               "┃  Starting MrSOA Server %d.%d.%d%s  ┃\n" +
                               "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n"
                               , 3, 6, 1, "  ");
    log.info(startupLog);
    
    this.context = context;
    
    mrSoaResponderPool = MrSoaResponderPool.getInstance();
    receiver = new MrSoaReceiver(mrSoaResponderPool, null);
    
    // TODO using Signal and SignalHandler triggers a 'proprietary' warning at build time but there isn't another way to accomplish
    // this. A work around is to use reflection to load these classes.
    Signal.handle(new Signal("ALRM"), new ShutdownHandler());
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public synchronized void stop(BundleContext context) throws Exception {
    receiver.shutdown();
    receiver.join();
    
    mrSoaResponderPool.close();
  }
  
  private class ShutdownHandler implements SignalHandler {
    OnStopDependencyCalculator onStopCalculator = new OnStopDependencyCalculator();
    
    public void handle(Signal signal) {
      log.info("Received shutdown signal");
      try {
          shutdown();
      } catch (Throwable t) {
        log.error(t.getMessage(), t);
        System.exit(0);
      }
    }
    
    private void shutdown() {
      Map<String, IMrSoaService> services = LocalServiceRegistry.getServices();
      for (IMrSoaService mrsoaService : services.values()) {
        onStopCalculator.initService(mrsoaService);
      }
      
      IMrSoaService nextStop;
      while ((nextStop = onStopCalculator.nextStop()) != null) {
        try {
          nextStop.stop();
        } catch (Exception e) {
          log.error("Error while stopping service " + nextStop.getEndpoint(), e);
        }
      }
        
      synchronized (MrSoaActivator.this) {
        log.info("Stopping MrSOA");
        if (context == null) {
          System.exit(0);
        } else {
          Bundle systemBundle = context.getBundle(0);
          try {
            systemBundle.stop();
          } catch (BundleException e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
  }
  
  private static class OnStopDependencyCalculator {
    private Map<ServiceEndpoint,IMrSoaService> endpointToServiceMap = new HashMap<ServiceEndpoint,IMrSoaService>();
    private Map<ServiceEndpoint,Integer> endpointDependentsCountMap = new HashMap<ServiceEndpoint,Integer>();
    private Map<ServiceEndpoint,List<ServiceEndpoint>> endpointDependenciesMap = new HashMap<ServiceEndpoint,List<ServiceEndpoint>>();
    boolean firstNextStop = true;

    private void addOnStopDependency(IMrSoaService fromService, ServiceEndpoint toService) {
      List<ServiceEndpoint> dependencies = endpointDependenciesMap.get(fromService.getEndpoint());
      if (dependencies == null) {
        dependencies = new ArrayList<ServiceEndpoint>();
        endpointDependenciesMap.put(fromService.getEndpoint(), dependencies);
      }
      dependencies.add(toService);
      
      Integer dependentCount = endpointDependentsCountMap.get(toService);
      if (dependentCount == null) {
        dependentCount = 0;
        endpointDependentsCountMap.put(toService, dependentCount);
      }
      dependentCount += 1;
      endpointDependentsCountMap.put(toService, dependentCount);
    }

    public void initService(IMrSoaService mrsoaService) {
      endpointToServiceMap.put(mrsoaService.getEndpoint(), mrsoaService);
      
      Integer dependentCount = endpointDependentsCountMap.get(mrsoaService.getEndpoint());
      if (dependentCount == null) {
        dependentCount = 0;
        endpointDependentsCountMap.put(mrsoaService.getEndpoint(), dependentCount);
      }
      
      List<ServiceEndpoint> onStopDependencies = mrsoaService.getOnStopDependencies();
      for (ServiceEndpoint onStopDependency : onStopDependencies)
        addOnStopDependency(mrsoaService, onStopDependency);
    }

    public IMrSoaService nextStop() {
      if (firstNextStop) {
        logDependencyNames();
        logDependencyCounts();
        firstNextStop = false;
      }
      
      for (Entry<ServiceEndpoint,Integer> endpointDependentsCount : endpointDependentsCountMap.entrySet()) {
        ServiceEndpoint endpoint = endpointDependentsCount.getKey();
        Integer dependentsCount = endpointDependentsCount.getValue();
        if (dependentsCount == 0) {
          List<ServiceEndpoint> dependencies = endpointDependenciesMap.get(endpoint);
          if (dependencies != null) {
            for (ServiceEndpoint dependency : dependencies) {
              Integer dependencyDependentsCount = endpointDependentsCountMap.get(dependency);
              dependencyDependentsCount -= 1;
              endpointDependentsCountMap.put(dependency, dependencyDependentsCount);
            }
          }
          IMrSoaService service = endpointToServiceMap.remove(endpoint);
          endpointDependenciesMap.remove(endpoint);
          endpointDependentsCountMap.remove(endpoint);
          return service;
        }
      }
      if (!endpointDependentsCountMap.isEmpty())
        log.warn("*** Cycle detected in OnStop depends graph!  Remaining services: " + endpointDependentsCountMap.keySet());
      return null;
    }

    private void logDependencyNames() {
      if (!log.isDebugEnabled())
        return;
      
      for (Entry<ServiceEndpoint,List<ServiceEndpoint>> endpointDependencies : endpointDependenciesMap.entrySet()) {
        for (ServiceEndpoint dependency : endpointDependencies.getValue()) {
          log.debug(endpointDependencies.getKey().getServiceName() + " >depends> " + dependency.getServiceName());
        }
      }
    }
    
    private void logDependencyCounts() {
      if (!log.isDebugEnabled())
        return;
      
      for (Entry<ServiceEndpoint,Integer> dependentsCount : endpointDependentsCountMap.entrySet()) {
        log.debug(format("%s <depends< %d services", dependentsCount.getKey().getServiceName(), dependentsCount.getValue()));
      }
    }
  }
}
