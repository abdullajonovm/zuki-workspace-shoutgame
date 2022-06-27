package com.meinc.webcollector.message;

import java.io.Serializable;
import java.util.List;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;

public class MessageBufferProxy implements IWebCollectorMessageBuffer, Serializable {
    private static final long serialVersionUID = 1L;

    private int secret;
    private ServiceEndpoint webCollectorService = new ServiceEndpoint(null, IWebCollectorMessageBuffer.SERVICE_NAME, null);
    
    public MessageBufferProxy(int secret) {
        this.secret = secret;
    }
    
    @Override
    public void removeMessages(List<CollectorMessage> messages, boolean storeProcessingTime) {
        ServiceMessage.sendFast(webCollectorService, "removeMessages", secret, messages, storeProcessingTime);
    }

    @Override
    public void removeMessage(CollectorMessage message, boolean storeProcessingTime) {
        ServiceMessage.sendFast(webCollectorService, "removeMessage", secret, message, storeProcessingTime);
    }

    @Override
    public boolean retryMessage(CollectorMessage message, int maxRetries) {
        return (Boolean) ServiceMessage.sendFast(webCollectorService, "retryMessage", secret, message, maxRetries);
    }
    
    public int getSecret() {
        return this.secret;
    }
}
