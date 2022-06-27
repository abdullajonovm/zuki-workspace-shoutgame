package com.meing.http.eps;

import com.meinc.mrsoa.service.ServiceEndpoint;

public class HttpCallback {

	private ServiceEndpoint _callbackEndpoint;
	private String _remoteGetMethod;
	private String _remotePostMethod;
	private String _requestPathPattern;
	private String _callbackValue;

	public HttpCallback(ServiceEndpoint callbackEndpoint,
			String remoteGetMethod, String remotePostMethod,
			String requestPathPattern, String callbackValue) {
		_callbackEndpoint = callbackEndpoint;
		_remoteGetMethod = remoteGetMethod;
		_remotePostMethod = remotePostMethod;
		_requestPathPattern = requestPathPattern;
		_callbackValue = callbackValue;
	}

	public ServiceEndpoint getCallbackEndpoint() {
		return _callbackEndpoint;
	}

	public void setCallbackEndpoint(ServiceEndpoint endpoint) {
		_callbackEndpoint = endpoint;
	}

	public String getCallbackValue() {
		return _callbackValue;
	}

	public void setCallbackValue(String value) {
		_callbackValue = value;
	}

	public String getRemoteGetMethod() {
		return _remoteGetMethod;
	}

	public void setRemoteGetMethod(String getMethod) {
		_remoteGetMethod = getMethod;
	}

	public String getRemotePostMethod() {
		return _remotePostMethod;
	}

	public void setRemotePostMethod(String postMethod) {
		_remotePostMethod = postMethod;
	}

	public String getRequestPathPattern() {
		return _requestPathPattern;
	}

	public void setRequestPathPattern(String pathPattern) {
		_requestPathPattern = pathPattern;
	}
}
