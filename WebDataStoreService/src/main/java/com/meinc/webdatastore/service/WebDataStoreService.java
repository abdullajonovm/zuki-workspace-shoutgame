    package com.meinc.webdatastore.service;

import static com.meinc.webdatastore.domain.WebDataStoreObject.TransferEncoding.BASE64;
import static com.meinc.webdatastore.store.Store.UploadType.CREATE_ONLY;
import static com.meinc.webdatastore.store.Store.UploadType.CREATE_OR_UPDATE;
import static com.meinc.webdatastore.store.Store.UploadType.UPDATE_ONLY;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.commons.encryption.EncryptUtils;
import com.meinc.commons.encryption.HexUtils;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;
import com.meinc.webdatastore.service.IWebDataStoreCallback.WebDataStoreCallbackType;
import com.meinc.webdatastore.store.CallbackRunnable;
import com.meinc.webdatastore.store.Store;
import com.meinc.webdatastore.store.Store.UploadType;

@Service(
    name=WebDataStoreService.WEBSTORE_SERVICE,
    interfaces=WebDataStoreService.WEBSTORE_INTERFACE,
    exposeAs=IWebDataStoreService.class)
public class WebDataStoreService implements IWebDataStoreService {
    public static final String WEBSTORE_SERVICE = "WebDataStoreService";
    public static final String WEBSTORE_INTERFACE = "IWebDataStoreService";

    private static Log log = LogFactory.getLog(WebDataStoreService.class);

//    @Autowired
//    private PlatformTransactionManager transactionManager;

    @Autowired
    private ExecutorService wdsExecutor;

    private Store store;

    @Override
    @ServiceMethod
    @OnStart
    public void onServiceStart() {
        log.info("WebDataStoreService started");
    }

    @Override
    @ServiceMethod
    @OnStop
    public void onServiceStop() {

    }

    @Override
    @ServiceMethod
    public String createObjectSync(WebDataStoreObject object, int createFlags) throws WebDataStoreException, InterruptedException {
        log.debug("createObjectSync->"+object);

        createWdsObject(object);

        uploadToStore(object, false, createFlags, CREATE_ONLY);

        return object.getPath();
    }

    private void uploadToStore(WebDataStoreObject object, boolean async, int uploadFlags, UploadType uploadType) throws InterruptedException, WebDataStoreException {
        if (object.getData() != null) {
            if (object.getUploadCounter() == 0 && object.getEncryptWithKey() != null) {
                byte[] encryptedData = EncryptUtils.aes256Encode(object.getData(), object.getEncryptWithKey());
                object.setData(encryptedData);
            }
            if (object.getUploadCounter() == 0 && object.getTransferEncoding() == BASE64) {
                byte[] base64Data = HexUtils.bytesToBase64Bytes(object.getData());
                object.setData(base64Data);
            }
        }
        object.incrementUploadCounter();
        store.upload(object, async, uploadFlags, uploadType);
    }

    @Override
    @ServiceMethod
    public void createObjectAsync(WebDataStoreObject object, int createFlags) {
        log.debug("createObjectAsync->"+object);

        try {
            createWdsObject(object);
        } catch (WebDataStoreException e) {
            wdsExecutor.execute(new CallbackRunnable(object, WebDataStoreCallbackType.FAILURE, e));
        }

        try {
            uploadToStore(object, true, createFlags, CREATE_ONLY);
        } catch (InterruptedException e) {
        } catch (WebDataStoreException e) {
        }
    }

//    private void createOrUpdateWdsObject(WebDataStoreObject object) {
//        if (object.getExpirationDate() == null)
//            throw new IllegalArgumentException("object must have expiration date");
//        if (object.getServiceCallback() == null)
//            throw new IllegalArgumentException("object must have service callback");
//        if (object.getPath() == null)
//            throw new IllegalArgumentException("object must have name or path");
//    }

    private void createWdsObject(WebDataStoreObject object) throws WebDataStoreException {
        if (object.getPatchInsertBeforePattern() != null)
            throw new IllegalArgumentException("object with patch parameter may not be created");
        if (object.getExpirationDate() == null)
            throw new IllegalArgumentException("object must have expiration date");
        if (object.getServiceCallback() == null)
            throw new IllegalArgumentException("object must have service callback");
        if (object.getPath() == null)
            throw new IllegalArgumentException("object must have name or path");
    }

    @Override
    @ServiceMethod
    public void updateObjectSync(WebDataStoreObject object, int updateFlags) throws InterruptedException, WebDataStoreException {
        log.debug("updateObjectSync->"+object);
        uploadToStore(object, false, updateFlags, UPDATE_ONLY);
    }

    @Override
    @ServiceMethod
    public void updateObjectAsync(WebDataStoreObject object, int updateFlags) {
        log.debug("updateObjectAsync->"+object);

        try {
            uploadToStore(object, true, updateFlags, UPDATE_ONLY);
        } catch (InterruptedException e) {
        } catch (WebDataStoreException e) {
        }
    }

    @Override
    @ServiceMethod
    public void createOrUpdateObjectSync(WebDataStoreObject object, int createOrUpdateFlags)
    throws WebDataStoreException, InterruptedException
    {
        if (log.isInfoEnabled()) {
            String data = "[binary]";
            try {
                data = new String(object.getData());
                if (data.length() > 150) data = data.substring(0, 150) + " ...";
            } catch (Exception ignored) {}
            log.info(MessageFormat.format(
                "createOrUpdateObjectSync, path: {0}\n\t{1}",
                object.getPath(), data
            ));
        }
        uploadToStore(object, false, createOrUpdateFlags, CREATE_OR_UPDATE);
    }

    @Override
    @ServiceMethod
    public void createOrUpdateObjectAsync(WebDataStoreObject object, int createOrUpdateFlags) {
        log.debug("createOrUpdateObjectAsync");
        try {
            uploadToStore(object, true, createOrUpdateFlags, CREATE_OR_UPDATE);
        } catch (InterruptedException e) {
        } catch (WebDataStoreException e) {
        }
    }

    @Override
    @ServiceMethod
    public WebDataStoreObject readObject(String objectPath) throws WebDataStoreException, InterruptedException {
        log.debug("readObject->"+objectPath);
        try {
            return store.read(null, null, objectPath, null);
        } catch (WebDataStoreException e) {
            if (e.getHttpStatus() == 404)
                return null;
            throw e;
        }
    }

    @Override
    @ServiceMethod
    public WebDataStoreObject readObject(String objectPath, Long partitionDividend) throws WebDataStoreException, InterruptedException {
        log.debug("readObject->"+objectPath+"/"+partitionDividend);
        try {
            return store.read(null, null, objectPath, partitionDividend);
        } catch (WebDataStoreException e) {
            if (e.getHttpStatus() == 404)
                return null;
            throw e;
        }
    }

    @Override
    @ServiceMethod
    public void operateObjectSync(Endpoint objectEndpoint, WebDataStoreObjectOperation... operations) throws WebDataStoreException, InterruptedException {
        if (log.isDebugEnabled()) {
            StringBuffer ops = new StringBuffer();
            for (WebDataStoreObjectOperation op : operations)
                ops.append(op.getType().name()).append(",");
            if (ops.length() > 0)
                ops.delete(ops.length()-1, ops.length());
            log.debug("operateObjectSync->"+objectEndpoint.getToWds()+"/"+objectEndpoint.getRoot().getName()+"/"+objectEndpoint.getPath()+":"+ops);
        }

        store.operate(objectEndpoint, false, operations);
    }

    @Override
    @ServiceMethod
    public void operateObjectAsync(Endpoint objectEndpoint, ServiceEndpoint serviceCallback, String callbackPassthrough, WebDataStoreObjectOperation... operations) {
        throw new UnsupportedOperationException();
        //if (log.isDebugEnabled()) {
        //    StringBuffer ops = new StringBuffer();
        //    for (WebDataStoreObjectOperation op : operations)
        //        ops.append(op.getType().name()).append(",");
        //    if (ops.length() > 0)
        //        ops.delete(ops.length()-1, ops.length());
        //    log.debug("operateObjectAsync->"+objectEndpoint.getToWds()+"/"+objectEndpoint.getRoot().getName()+"/"+objectEndpoint.getPath()+":"+ops);
        //}

        //store.operate(objectEndpoint, true, operations);
    }

    public void setStore(Store store) {
        this.store = store;
    }
}