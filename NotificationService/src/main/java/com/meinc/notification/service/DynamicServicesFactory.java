package com.meinc.notification.service;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.push.service.IPushService;

public class DynamicServicesFactory
{
    private static Logger _logger = Logger.getLogger(DynamicServicesFactory.class);
    
    public DynamicServicesFactory()
    {
    }
    
    public static IPushService pushServiceFactory()
    {
        _logger.debug("getting push service from global properties file (key: gameplay.push.client)");
        Properties props = ServerPropertyHolder.getProps();
        String pushClass = props.getProperty("gameplay.push.client");
        IPushService service = null;
        try {
            Class<?> c = Class.forName(pushClass);
            service = (IPushService) c.newInstance();
        } catch (Exception e) {
            _logger.error("unable to instantiate push service", e);
        }
        
        return service;
    }
}
