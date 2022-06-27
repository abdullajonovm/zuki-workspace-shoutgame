package com.meinc.mrsoa.service;

import java.util.ArrayList;
import java.util.List;

public class ServiceCallStack {
  private static ThreadLocal<List<ServiceCallStackRow>> callStacks = new ServiceCallStackThreadLocal();
  private static ThreadLocal<ServiceCallStackRow> currentServices = new ServiceCallStackRowThreadLocal();
  public static final ClassLoader THREAD_CONTEXT_LOADER = new ClassLoader() { };
  
  public static List<ServiceCallStackRow> getCallStack() {
    List<ServiceCallStackRow> callStack = callStacks.get();
    return callStack;
  }
  
  static void setCallStack(List<ServiceCallStackRow> callStack) {
    callStacks.set(callStack);
  }
  
  // Right now this *only* ever will have a value when called from a service's on-start method
  public static ServiceCallStackRow getCurrentService() {
    return currentServices.get();
  }
  
  // Right now this *only* ever gets set for a service's on-start method
  static void setCurrentService(ServiceEndpoint currentService, ClassLoader serviceClassLoader) {
    ServiceCallStackRow currentServiceRow = new ServiceCallStackRow(currentService, null, serviceClassLoader);
    currentServices.set(currentServiceRow);
  }
  
  public static ClassLoader getCurrentServiceClassLoader() {
    List<ServiceCallStackRow> callStack = getCallStack();
    
    if (callStack == null || callStack.isEmpty()) {
      ServiceCallStackRow currentService = getCurrentService();
      if (currentService == null || currentService.getServiceClassLoader() == THREAD_CONTEXT_LOADER)
        return Thread.currentThread().getContextClassLoader();
      return currentService.getServiceClassLoader();
      
    } else {
      ServiceCallStackRow currentCallStackRow = callStack.get(callStack.size() - 1);
      ClassLoader classLoader = currentCallStackRow.getServiceClassLoader();
      return classLoader;
    }
  }
  
  // Right now this will *only* work when called from a service's on-start method
  public static void setCurrentServiceClassLoader(ClassLoader classLoader) {
      if (classLoader == null)
          throw new IllegalArgumentException("ClassLoader must not be null");
      ServiceCallStackRow currentService = getCurrentService();
      if (currentService != null)
          currentService.setServiceClassLoader(classLoader);
  }
  
  /**
   * Orphans the current thread by divorcing it from any record of a service
   * callstack. This, among other things, will cause service method invocations
   * to use the context class loader.
   */
  public static void orphanCurrentThread() {
    currentServices.set(null);
    callStacks.set(null);
  }
  
  protected static List<ServiceCallStackRow> generateInnerServiceCallStack() {
    ServiceEndpoint currentService = getCurrentService();
    if (currentService != null) {
      StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
      String className = null;
      String methodName = null;
      for (int i = 0; i < stackTrace.length; i++) {
        StackTraceElement element = stackTrace[i];
        if (!element.getClassName().startsWith("com.meinc.mrsoa.") && !element.getClassName().startsWith("clientproxy.")) {
          className = element.getClassName();
          methodName = element.getMethodName();
          break;
        }
      }
      if (className != null && methodName != null) {
        int dotIndex = className.lastIndexOf(".");
        if (dotIndex != -1)
          className = className.substring(dotIndex+1);
        ServiceCallStackRow firstRow = new ServiceCallStackRow(currentService, className+"."+methodName, getCurrentServiceClassLoader());
        ArrayList<ServiceCallStackRow> callStack = new ArrayList<ServiceCallStackRow>();
        callStack.add(firstRow);
        return callStack;
      }
    }
    return null;
  }
}

class ServiceCallStackThreadLocal extends InheritableThreadLocal<List<ServiceCallStackRow>> {
  @Override
  protected List<ServiceCallStackRow> childValue(List<ServiceCallStackRow> parentValue) {
    if (parentValue == null)
      return null;
    
    ArrayList<ServiceCallStackRow> newCallStack = new ArrayList<ServiceCallStackRow>();
    for (ServiceCallStackRow row : parentValue) {
      newCallStack.add(row.clone());
    }
    return newCallStack;
  }
}

class ServiceCallStackRowThreadLocal extends InheritableThreadLocal<ServiceCallStackRow> {
  @Override
  protected ServiceCallStackRow childValue(ServiceCallStackRow parentValue) {
    if (parentValue == null)
      return null;
    return parentValue.clone();
  } 
}
