package com.meinc.mrsoa.service;


public class ServiceCallStackRow extends ServiceEndpoint implements Cloneable {
  
  private static final long serialVersionUID = 7885246541432860993L;
  
  private String methodName;
  
  private transient ClassLoader serviceClassLoader;
  
  public ServiceCallStackRow(ServiceEndpoint endpoint, String methodName, ClassLoader serviceLoader) {
    super(endpoint.getNamespace(),
        endpoint.getServiceName(),
        endpoint.getVersion());
    this.methodName = methodName;
    this.serviceClassLoader = serviceLoader;
  }
  
  public String getMethodName() {
    return methodName;
  }
  
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public ClassLoader getServiceClassLoader() {
    return serviceClassLoader;
  }

  public void setServiceClassLoader(ClassLoader callerClassLoader) {
    this.serviceClassLoader = callerClassLoader;
  }
  
  @Override
  public String toString() {
    return "["+getServiceName()+"]["+methodName+"]";
  }
  
  public ServiceCallStackRow clone() {
    try {
      return (ServiceCallStackRow) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
