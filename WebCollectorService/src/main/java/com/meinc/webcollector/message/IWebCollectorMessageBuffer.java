package com.meinc.webcollector.message;

import java.util.List;

public interface IWebCollectorMessageBuffer {
    public static final String SERVICE_NAME = "WebCollectorMessageBuffer";
    public static final String SERVICE_INTERFACES = "IWebCollectorMessageBuffer";

    public void removeMessages(List<CollectorMessage> messages, boolean storeProcessingTime);

    /**
     * No-op if the message was previously removed due to retry.
     */
    public void removeMessage(CollectorMessage message, boolean storeProcessingTime);
    
    /**
     * Also removes the message
     * @return 
     */
    public boolean retryMessage(CollectorMessage message, int maxRetries);

}