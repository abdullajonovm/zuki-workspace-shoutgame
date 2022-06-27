    package com.meinc.webdatastore.service;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.store.Store;

@Service(
    name=WebMediaStoreService.WEBSTORE_SERVICE,
    interfaces=WebMediaStoreService.WEBSTORE_INTERFACE,
    exposeAs=IWebDataStoreService.class)
public class WebMediaStoreService extends WebDataStoreService implements IWebDataStoreService {
    public static final String WEBSTORE_SERVICE = "WebMediaStoreService";
    public static final String WEBSTORE_INTERFACE = "IWebMediaStoreService";

    @ServiceMethod
    @Override
    public void onServiceStart() {
        super.onServiceStart();
    }

    @ServiceMethod
    @Override
    public void onServiceStop() {
        super.onServiceStop();
    }

    @ServiceMethod
    @Override
    public String createObjectSync(WebDataStoreObject object, int createFlags) throws WebDataStoreException, InterruptedException {
        return super.createObjectSync(object, createFlags);
    }

    @ServiceMethod
    @Override
    public void createObjectAsync(WebDataStoreObject object, int createFlags) {
        super.createObjectAsync(object, createFlags);
    }

    @ServiceMethod
    @Override
    public void updateObjectSync(WebDataStoreObject object, int updateFlags) throws InterruptedException, WebDataStoreException {
        super.updateObjectSync(object, updateFlags);
    }

    @ServiceMethod
    @Override
    public void updateObjectAsync(WebDataStoreObject object, int updateFlags) {
        super.updateObjectAsync(object, updateFlags);
    }

    @ServiceMethod
    @Override
    public void createOrUpdateObjectSync(WebDataStoreObject object, int createOrUpdateFlags)
        throws WebDataStoreException, InterruptedException {
        super.createOrUpdateObjectSync(object, createOrUpdateFlags);
    }

    @ServiceMethod
    @Override
    public void createOrUpdateObjectAsync(WebDataStoreObject object, int createOrUpdateFlags) {
        super.createOrUpdateObjectAsync(object, createOrUpdateFlags);
    }

    @ServiceMethod
    @Override
    public WebDataStoreObject readObject(String objectPath) throws WebDataStoreException, InterruptedException {
        return super.readObject(objectPath);
    }

    @ServiceMethod
    @Override
    public WebDataStoreObject readObject(String objectPath, Long partitionDividend) throws WebDataStoreException, InterruptedException {
        return super.readObject(objectPath, partitionDividend);
    }

    @ServiceMethod
    @Override
    public void operateObjectSync(Endpoint objectEndpoint, WebDataStoreObjectOperation... operations)
        throws WebDataStoreException, InterruptedException {
        super.operateObjectSync(objectEndpoint, operations);
    }

    @ServiceMethod
    @Override
    public void operateObjectAsync(Endpoint objectEndpoint, ServiceEndpoint serviceCallback, String callbackPassthrough,
                                   WebDataStoreObjectOperation... operations) {
        super.operateObjectAsync(objectEndpoint, serviceCallback, callbackPassthrough, operations);
    }

    @Override
    public void setStore(Store store) {
        super.setStore(store);
    }
}