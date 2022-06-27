package com.meinc.commons.appregistry;

import java.io.Serializable;

import com.meinc.commons.application.IApplication;

public class ApplicationWrapper implements Serializable {
	private static final long serialVersionUID = -2067230887552368825L;
	
	private IApplication app;
	private String namespace;
	private String appName;
	private String version;
	
	public IApplication getApp() {
		return app;
	}
	public void setApp(IApplication app) {
		this.app = app;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
