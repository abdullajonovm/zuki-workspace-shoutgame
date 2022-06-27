package com.meinc.webdatastore.service;

import com.meinc.webdatastore.domain.WebDataStoreObject;

public interface IWebDataStoreCallback {
    public enum WebDataStoreCallbackType {
        /** Signals that the recent Web Data Store operation concerning this object succeeded */
        SUCCESS,
        /** Signals that the recent Web Data Store operation concerning this object failed */
        FAILURE,
        /** Signals that this object has expired and will be deleted as soon as the callback returns */
        EXPIRED,
    }

    public void onWdsCallback(WebDataStoreObject object, WebDataStoreCallbackType callbackType, WebDataStoreException error);
}
