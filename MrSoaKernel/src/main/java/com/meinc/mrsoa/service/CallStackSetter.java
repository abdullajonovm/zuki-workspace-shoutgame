package com.meinc.mrsoa.service;

import java.util.List;

/**
 * A hidden backdoor to setting the callstack.  Don't use this unless you are 
 * calling from the service-adaptor layer.
 */
public class CallStackSetter {
  public static void setCallStack(List<ServiceCallStackRow> callStack) {
    ServiceCallStack.setCallStack(callStack);
  }

  public static void setCurrentService(ServiceEndpoint currentEndpoint, ClassLoader serviceClassLoader) {
    ServiceCallStack.setCurrentService(currentEndpoint, serviceClassLoader);
  }
}
