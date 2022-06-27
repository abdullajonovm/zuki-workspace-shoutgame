package com.meinc.commons.proxy;

import java.io.Serializable;

import com.meinc.commons.application.IApplication;
import com.meinc.commons.helper.Parameters;
import com.meinc.commons.helper.Result;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;

public class ApplicationProxy implements IApplication, Serializable {

	private static final long serialVersionUID = -3900894156741294254L;
	
	private ServiceEndpoint _endpoint;
	
	public ApplicationProxy(ServiceEndpoint endpoint) {
    ServiceMessage.waitForServiceRegistration(endpoint);
		_endpoint = endpoint;
	}

	public boolean canProcessCommand(int cmd) {
		return (Boolean) ServiceMessage.send(_endpoint, "canProcessCommand", cmd);
	}

	public boolean isAuthorized(int cmd, Parameters params) {
		return (Boolean) ServiceMessage.send(_endpoint, "isAuthorized", cmd, params);
	}

	public Result processCommand(int cmd, Parameters params) {
		return (Result) ServiceMessage.send(_endpoint, "processCommand", cmd, params);
	}
}
