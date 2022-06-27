package com.meinc.commons.appregistry;

import java.util.List;

import com.meinc.commons.application.IApplication;
import com.meinc.commons.application.UnsupportedApplicationVersion;

public interface IAppRegistry {
	public List<ApplicationWrapper> getApplicationVersions(String namespace, String appName);
	public IApplication getApplication(String namespace, String appName, String version) throws UnsupportedApplicationVersion;
	public IApplication waitForApplication(String namespace, String appName, String version) throws UnsupportedApplicationVersion;
}
