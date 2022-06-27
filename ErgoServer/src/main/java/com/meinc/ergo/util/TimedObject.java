package com.meinc.ergo.util;

import java.io.Serializable;

import org.springframework.context.ApplicationContext;

import com.meinc.ergo.config.ApplicationContextProvider;
import com.meinc.ergo.domain.BaseEntityObject;

public class TimedObject<T extends BaseEntityObject>
implements Serializable
{
    private static final long serialVersionUID = 1L;
//    private static final Logger logger = Logger.getLogger(TimedObject.class);
    
    private T obj;
    private long lastUpdate;
    
    private int ttlImmediate = 15;
    private int ttlExchange = 300;
    private int ttlGoogle = 300;
    private int ttliCloud = 300;
    
    public TimedObject(T obj)
    {
        this.obj = obj;
        lastUpdate = System.currentTimeMillis();
//String desc = null;
//if (obj instanceof Task) desc = ((Task)obj).getDescription();
//logger.info(MessageFormat.format("*** added task {0} with lastUpdate time of {1}", desc, lastUpdate));
        
        ApplicationContext appContext = ApplicationContextProvider.getApplicationContext();
        try {
            ttlImmediate = (Integer) appContext.getBean("ttlImmediate");
        } catch (NullPointerException e) {} catch (NumberFormatException e) {}
        try {
            ttlExchange = (Integer) appContext.getBean("ttlExchange");
        } catch (NullPointerException e) {} catch (NumberFormatException e) {}
        try {
            ttlGoogle = (Integer) appContext.getBean("ttlGoogle");
        } catch (NullPointerException e) {} catch (NumberFormatException e) {}
        try {
            ttliCloud = (Integer) appContext.getBean("ttliCloud");
        } catch (NullPointerException e) {} catch (NumberFormatException e) {}
    }
    
    public T get()
    {
        return obj;
    }
    
    public long getLastUpdate()
    {
        return lastUpdate;
    }
    
    public boolean isFresh()
    {
        switch (obj.getProviderType())
        {
            case ERGO:
                return true; //never expires
            
            case EXCHANGE:
                return lastUpdate + (ttlExchange * 1000) > System.currentTimeMillis();
                
            case GOOGLE:
                return lastUpdate + (ttlGoogle * 1000) > System.currentTimeMillis();
                
            case APPLE:
                return lastUpdate + (ttliCloud * 1000) > System.currentTimeMillis();
        }
        
        return false;
    }
    
    /**
     * Determines if the data is "fresh", based on a ttl value specified in the application properties.
     * 
     * @param immediate if true, use the "immediate" ttl value rather than the normal ttl value for the given provider type.
     * @return
     */
    public boolean isFresh(boolean immediate)
    {
        switch (obj.getProviderType())
        {
            case ERGO:
                return true; //never expires
            
            case EXCHANGE:
                if (immediate)
                    return lastUpdate + (ttlImmediate * 1000) > System.currentTimeMillis();
                else
                    return lastUpdate + (ttlExchange * 1000) > System.currentTimeMillis();
                
            case GOOGLE:
                if (immediate)
                    return lastUpdate + (ttlImmediate * 1000) > System.currentTimeMillis();
                else
                    return lastUpdate + (ttlGoogle * 1000) > System.currentTimeMillis();
                
            case APPLE:
                if (immediate)
                    return lastUpdate + (ttlImmediate * 1000) > System.currentTimeMillis();
                else
                    return lastUpdate + (ttliCloud * 1000) > System.currentTimeMillis();
        }
        
        return false;
    }
    
}
