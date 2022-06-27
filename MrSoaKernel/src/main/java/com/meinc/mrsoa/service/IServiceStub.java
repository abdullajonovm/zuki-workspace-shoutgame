package com.meinc.mrsoa.service;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.exception.AdaptorException;


public interface IServiceStub {
	
	public void onLoad();
	public boolean isLoaded();
  public String[] getInterfaceNames();
	public Object invokeMethod(ServiceEndpoint endpoint, String methodName, Object... args) throws AdaptorException;

}
