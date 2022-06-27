package com.meinc.mrsoa.net.inbound;

import java.util.List;

import com.meinc.mrsoa.service.ServiceEndpoint;


/**
 * Represents a MrSOA service application that may be invoked via the
 * {@link LocalServiceRegistry}.
 * 
 * @author Matt
 */
public interface IMrSoaService {
  
  public void start() throws Exception;
  
  public void stop() throws Exception;

  /**
   * Invokes a method on this service application.
   * 
   * @param request
   *          The request object
   * @param args
   *          The arguments to be passed to the method
   * @return The object returned by the method.
   * @throws Throwable
   *           Any exception thrown by the method
   */
  public Object invokeMethod(MrSoaRequest request, Object[] args) throws Throwable;

  public ServiceEndpoint getEndpoint();
  
  /**
   * Whether this MrSOA service is started. Note that MrSOA services may be
   * started independently of the parent OSGi bundle.
   * 
   * @return Whether this service is started.
   */
  public boolean isStarted();

  /**
   * Whether this MrSOA service is stopped. Note that MrSOA services may be
   * stopped independently of the parent OSGi bundle.
   * 
   * @return Whether this service is stopped.
   */
  public boolean isStopped();

  /**
   * Returns a list of service methods that must be available in order for this
   * service to stop.
   * 
   * @return List of required service endpoints.
   */
  public List<ServiceEndpoint> getOnStopDependencies();
}
