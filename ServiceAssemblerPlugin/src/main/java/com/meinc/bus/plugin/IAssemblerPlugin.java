package com.meinc.bus.plugin;


public interface IAssemblerPlugin {
	// TODO: allow plugin methods to specify whether an invocation was handled
	// or not so that pipelines of distinct plugins can be handled for a single
	// service
	public Object initService();
}