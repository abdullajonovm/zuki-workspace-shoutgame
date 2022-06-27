package com.meing.http.eps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.meinc.commons.proxy.PhoenixCommonsProxy;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;

@Service(
	name="EpsHttpConnectorService",
	namespace=PhoenixCommonsProxy.PHOENIX_NAMESPACE,
	interfaces="IHttpConnector"
)
public class EpsHttpConnectorService {
	//TODO: move this into a persistent store so this service can be distributed automatically
	private static Map<String, HttpCallback> callbacks;
	private static Logger _logger = Logger.getLogger(EpsHttpConnectorService.class);
	
	@OnStart
	public void load() {
	    if (callbacks == null)
	        callbacks = new ConcurrentHashMap<String, HttpCallback>();
	}
	
	@ServiceMethod
	public boolean registerHttpCallback(ServiceEndpoint callbackEndpoint,
			String remoteGetMethod, String remotePostMethod,
			String requestPathPattern, String callbackValue) {
		HttpCallback callback = new HttpCallback(callbackEndpoint,
				remoteGetMethod, remotePostMethod, requestPathPattern,
				callbackValue);
		if (callbacks.containsKey(requestPathPattern))
			return false;
		callbacks.put(requestPathPattern, callback);
		_logger.info(String.format("Registered HTTP Callback: %s -> %s:%s/%s", requestPathPattern, callbackEndpoint, remoteGetMethod, remotePostMethod));
		return true;
	}
	
	@ServiceMethod
	public boolean unregisterHttpCallback(String requestPathPattern) {
	    if (callbacks.containsKey(requestPathPattern)) {
	        callbacks.remove(requestPathPattern);
	        return true;
	    }
	    return false;
	}
	
	@ServiceMethod
	public HttpResponse handleGetRequest(String requestPath, HttpRequest request) {
		HttpCallback callback = callbacks.get(requestPath);
		if (callback == null) {
			_logger.warn("no callback found for requestPath '" + requestPath + "'");
			return new HttpResponse();
		}
		return (HttpResponse) ServiceMessage.send(callback
				.getCallbackEndpoint(), callback.getRemoteGetMethod(), request);
	}
	
	@ServiceMethod
	public HttpResponse handlePostRequest(String requestPath, HttpRequest request) {
		HttpCallback callback = callbacks.get(requestPath);
		if (callback == null) {
			_logger.warn("no callback found for requestPath '" + requestPath + "'");
			return new HttpResponse();
		}
		return (HttpResponse) ServiceMessage.send(callback
				.getCallbackEndpoint(), callback.getRemotePostMethod(), request);
	}
}
