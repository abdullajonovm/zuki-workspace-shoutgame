package com.meinc.webdatastore.store;

import java.util.HashMap;
import java.util.Map;

import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.service.WebDataStoreException;

public abstract class Store {
    public static enum Type {
        SHOUT_WEB
    }
    
    public static enum UploadType {
        CREATE_OR_UPDATE,
        CREATE_ONLY,
        UPDATE_ONLY
    }
    
    private static Map<Type,Store> stores = new HashMap<Store.Type,Store>();
    
    public static Store getStore(Type storeType) {
        return stores.get(storeType);
    }

    Store() {
        stores.put(getType(), this);
    }

    abstract Type getType();

    public abstract WebDataStoreObject read(String fromWds, Root fromRoot, String path, Long partitionDividend) throws WebDataStoreException, InterruptedException;
    
    public abstract void upload(WebDataStoreObject object, boolean async, int uploadFlags, UploadType uploadType)
    throws InterruptedException, WebDataStoreException;

    public abstract void operate(WebDataStoreObject.Endpoint endpoint, boolean async, WebDataStoreObjectOperation[] operations)
    throws WebDataStoreException, InterruptedException;
}
