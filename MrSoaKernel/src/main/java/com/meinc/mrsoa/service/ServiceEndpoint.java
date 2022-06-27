package com.meinc.mrsoa.service;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceEndpoint implements Serializable {
	private static final long serialVersionUID = 9L;
	
  public static final String DEFAULT_NAMESPACE = "default";
	
  public static String delimitServiceString(String serviceString) {
    return serviceString
      // These characters are reserved characters in Mr.SOA service names
      .replaceAll("[\\[\\]]", "|");
  }
	
  protected String namespace;
  protected String serviceName;
  protected String version;
	
  public ServiceEndpoint() {
  }
  
  public ServiceEndpoint(String namespace, String serviceName, String version) {
    this.namespace = namespace;
    this.serviceName = serviceName;
    this.version = version;
  }
  
  /**
   * @param serviceEndpointString a string as returned by {@link ServiceEndpoint#toString()}
   */
  public ServiceEndpoint(String serviceEndpointString) {
      fromString(serviceEndpointString);
  }
  
  public String getNamespace() {
    if (namespace == null) return DEFAULT_NAMESPACE;
    else                   return namespace;
	}
	public void setNamespace(String namespace) {
	  if (namespace != null)
	    this.namespace = delimitServiceString(namespace);
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = delimitServiceString(serviceName);
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = delimitServiceString(version);
	}
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ServiceEndpoint) {
      ServiceEndpoint ep = (ServiceEndpoint)obj;
      if ((namespace == null || DEFAULT_NAMESPACE.equals(namespace)) != (ep.namespace == null || DEFAULT_NAMESPACE.equals(ep.namespace))
          || (serviceName == null) != (ep.serviceName == null)
          || (version == null) != (ep.version == null))
        return false;
      
      if ((namespace == null || ep.namespace == null || namespace.equals(ep.namespace))
          && (serviceName == null || serviceName.equals(ep.serviceName))
          && (version == null || version.equals(ep.version))) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    return "["+getNamespace()+"]["+getServiceName()+"]" + (getVersion()==null?"":"["+getVersion()+"]");
  }
  
  private final Pattern serviceEndpointStringPattern = Pattern.compile("\\[([^\\]]+)\\]");
  public void fromString(String serviceEndpointString) {
      Matcher m = serviceEndpointStringPattern.matcher(serviceEndpointString);
      int idx = 0;
      for (int i = 0; i < 3; i++) {
          switch (i) {
          case 0:
              if (!m.find(idx))
                  throw new IllegalArgumentException("invalid service endpoint string: " + serviceEndpointString);
              idx = m.end(1);
              setNamespace(m.group(1));
              break;
          case 1:
              if (!m.find(idx))
                  throw new IllegalArgumentException("invalid service endpoint string: " + serviceEndpointString);
              idx = m.end(1);
              setServiceName(m.group(1));
              break;
          case 2:
              if (m.find(idx))
                  setVersion(m.group(1));
              break;
          }
      }
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public void clear() {
    namespace = null;
    serviceName = null;
    version = null;
  }
}
