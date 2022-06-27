package com.meinc.trigger.service;

import java.util.Set;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.trigger.domain.Trigger;

public interface ITriggerService
{
    void start();
    void stop();
    
    /**
     * Allow filters to dynamically register with the trigger service.
     * 
     * @param endpoint
     * @param methodName
     * @return
     */
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName);
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName, String route);
    public boolean unregisterCallback(ServiceEndpoint endpoint);
    
    /**
     * Asynchronously process the given payload.  It will be passed through the filter interface chain and appropriate action will be taken.
     * <br/>
     * Clients who call this should fire and forget.
     */
    public void process(Trigger trigger);
    public void enqueue(Trigger trigger);
    public void enqueue(String triggerKey, Object serializedTriggerPayload, String route, String source, Set<String> bundleIds, int contextId);
}
