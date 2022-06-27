package com.meinc.webdatastore.store;

import static com.meinc.webdatastore.service.IWebDataStoreCallback.WebDataStoreCallbackType.SUCCESS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.service.IWebDataStoreCallback.WebDataStoreCallbackType;
import com.meinc.webdatastore.service.WebDataStoreException;

public class CallbackRunnable implements Runnable {
    private static final Log log = LogFactory.getLog(CallbackRunnable.class);
    
    private WebDataStoreObject object;
    private WebDataStoreCallbackType callbackType;
    private WebDataStoreException error;

    public CallbackRunnable(WebDataStoreObject object) {
        this.object = object;
        this.callbackType = SUCCESS;
    }

    public CallbackRunnable(WebDataStoreObject object, WebDataStoreCallbackType callbackType) {
        this.object = object;
        this.callbackType = callbackType;
    }

    public CallbackRunnable(WebDataStoreObject object, WebDataStoreCallbackType callbackType, WebDataStoreException error) {
        this.object = object;
        this.callbackType = callbackType;
        this.error = error;
    }

    @Override
    public void run() {
        try {
            if (log.isDebugEnabled())
                log.debug("Calling back service for object " + object.getPath() + " result=" + callbackType + " error=" + error);
            ServiceMessage.send(object.getServiceCallback(), "onWdsCallback", object, callbackType, error);
        } catch (Exception e) {
            log.error("Error during web data store callback: " + e.getMessage(), e);
        }
    }
}