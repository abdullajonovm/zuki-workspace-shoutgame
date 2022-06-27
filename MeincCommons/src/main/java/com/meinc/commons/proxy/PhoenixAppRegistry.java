package com.meinc.commons.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.commons.application.IApplication;
import com.meinc.commons.application.UnsupportedApplicationVersion;
import com.meinc.commons.appregistry.ApplicationWrapper;
import com.meinc.commons.appregistry.IAppRegistry;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;

public class PhoenixAppRegistry
implements IAppRegistry
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(PhoenixAppRegistry.class);
	
	private HashMap<String, IApplication> _appCache = new HashMap<String, IApplication>();

	public List<ApplicationWrapper> getApplicationVersions(String namespace, String appName) {
		List<ApplicationWrapper> apps = new ArrayList<ApplicationWrapper>();
		List<ServiceEndpoint> endpoints = ServiceMessage.getEndpointsImplementingInterface("IApplication");
		for (ServiceEndpoint endpoint : endpoints) {
			if (appName.equals(endpoint.getServiceName())) {
				ApplicationProxy app = new ApplicationProxy(endpoint);
				ApplicationWrapper wrapper = new ApplicationWrapper();
				wrapper.setNamespace(endpoint.getNamespace());
				wrapper.setAppName(endpoint.getServiceName());
				wrapper.setVersion(endpoint.getVersion());
				wrapper.setApp(app);
				apps.add(wrapper);
			}
		}
		return apps;
	}

	public IApplication getApplication(String namespace, String appName, String version) 
	throws UnsupportedApplicationVersion 
	{
		boolean appFound = false;
		//check the cache
		String ns = namespace + ":" + appName + ":" + version;
//log.debug("getApplication; ns [ns:appName:ver]: " + ns);
		IApplication app = _appCache.get(ns);
		if (app != null)
		{
//log.debug("getApplication; cache hit: " + app);
			return app;
		}

		List<ServiceEndpoint> endpoints = ServiceMessage.getEndpointsImplementingInterface("IApplication");
		for (ServiceEndpoint endpoint : endpoints)
		{
//log.debug("evaluating endpoint '" + endpoint.getServiceName() + "' ...");			
			if (appName.equals(endpoint.getServiceName()))
			{
				//special case: don't check for version on app 0 (base service)
				if ("0".equals(appName))
				{
//log.debug("matched app 0");					
					//store in cache and return
					app = new ApplicationProxy(endpoint);
					_appCache.put(ns, app);
					return app;
				}
//else log.debug("did not match app 0");
				
				if (version.equals(endpoint.getVersion()) || "_test_".equals(version))
				{
//log.debug("matched '_test_' or version: " + version);					
					//store in cache and return
					app = new ApplicationProxy(endpoint);
					_appCache.put(ns, app);
					return app;
				}
//else log.debug("did not match but found an app");

				//a version of the app was found that didn't match the incoming version
				appFound = true;
			}
		}

		if (appFound)
			throw new UnsupportedApplicationVersion();

//log.debug("*** returning null from phoenix app registry");		
		return null;
	}

	public IApplication waitForApplication(String namespace, String appName, String version) throws UnsupportedApplicationVersion {
		IApplication app = null;
		while (true) {
			app = getApplication(namespace, appName, version);
			if (app == null)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			else
				return app;
		}
	}
}
