package com.meinc.webdatastore.service;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.webdatastore.domain.WebDataStoreObject;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint;
import com.meinc.webdatastore.domain.WebDataStoreObjectOperation;


/**
 * Provides an interface to a WebStore where data is stored in a hierarchical
 * fashion for consumption by web clients.
 * 
 * @author mpontius
 */
public interface IWebDataStoreService {

    /**
     * Pre-cache and verify the object in all cache servers, or if the object is assigned a partition, in all cache
     * servers in the partition, so that the object is immediately available for speedy download.
     */
    public static final int OPFLAG_PRECACHE = 0x2;
    /**
     * Pre-cache and verify the object in all cache servers, or if the object is assigned a partition, in all cache
     * servers in the partition, so that the object is immediately available for speedy download.
     */
    public static final int OPFLAG_PURGE    = 0x4;

    void onServiceStart();
    void onServiceStop();

    /**
     * Creates a new object in the web data store.
     * <p/>
     * This method will not return until the new object is confirmed to be available for client access.
     * <p/>
     * The following attributes of the provided object must be non-null
     * <ul>
     * <li>{@link WebDataStoreObject#name} or {@link WebDataStoreObject#path}</li>
     * <li>{@link WebDataStoreObject#internalObjectType}</li>
     * <li>{@link WebDataStoreObject#internalObjectId}</li>
     * <li>{@link WebDataStoreObject#data}</li>
     * <li>{@link WebDataStoreObject#expirationDate}</li>
     * <li>{@link WebDataStoreObject#serviceCallback} if object is {@link WebDataStoreObject.Type#RESIDENT RESIDENT}</li>
     * </ul>
     * <p/>
     * If objectName is non-null but not objectPath is null, the object is created at a random path. The following
     * attributes of the provided object may be <em>optionally</em> non-null
     * <ul>
     * <li>{@link WebDataStoreObject#callbackPassthrough}</li>
     * </ul>
     * All other attributes of the provided object are ignored.
     * <p/>
     * Note that both the object and callback information are persisted before this method returns.
     * <p/>
     * Note that if {@link WebDataStoreObject#setEncryptWithKey(String) encryptWithKey} is non-null,
     * {@link WebDataStoreObject#getData() data} will be replaced with an encrypted version.
     * <p/>
     * A service callback is required to support {@link IWebDataStoreCallback.WebDataStoreCallbackType#REGENERATE
     * REGENERATE} functionality.
     * 
     * @param object
     *            the object to create
     * @param createFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     * @return the resulting object path to the newly created object
     * @throws WebDataStoreException
     * @throws InterruptedException
     */
    String createObjectSync(WebDataStoreObject object, int createFlags) throws WebDataStoreException, InterruptedException;

    /**
     * Creates a new object in the web data store.
     * <p/>
     * This method will return before the new object has been created. The provided callback will be invoked when the
     * new object has been confirmed to be available for client access.
     * <p/>
     * The following attributes of the provided object must be non-null
     * <ul>
     * <li>{@link WebDataStoreObject#name} or {@link WebDataStoreObject#path}</li>
     * <li>{@link WebDataStoreObject#internalObjectType}</li>
     * <li>{@link WebDataStoreObject#internalObjectId}</li>
     * <li>{@link WebDataStoreObject#data}</li>
     * <li>{@link WebDataStoreObject#expirationDate}</li>
     * <li>{@link WebDataStoreObject#serviceCallback} if object is {@link WebDataStoreObject.Type#RESIDENT RESIDENT}</li>
     * </ul>
     * <p/>
     * If objectName is non-null but not objectPath is null, the object is created at a random path. The following
     * attributes of the provided object may be <em>optionally</em> non-null
     * <ul>
     * <li>{@link WebDataStoreObject#callbackPassthrough}</li>
     * </ul>
     * All other attributes of the provided object are ignored.
     * <p/>
     * Note that both the object and callback information are persisted before this method returns.
     * <p/>
     * Note that if {@link WebDataStoreObject#setEncryptWithKey(String) encryptWithKey} is non-null,
     * {@link WebDataStoreObject#getData() data} will be replaced with an encrypted version.
     * <p/>
     * A service callback is required to support {@link IWebDataStoreCallback.WebDataStoreCallbackType#REGENERATE
     * REGENERATE} functionality.
     * 
     * @param object
     *            the object to create
     * @param createFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     */
    void createObjectAsync(WebDataStoreObject object, int createFlags);

    /**
     * Replaces an existing {@link WebDataStoreObject.Type#RESIDENT resident} object in the web data store. This method
     * will not return until the updated object is confirmed to be available for client access.
     * <p/>
     * At least one of the following object attributes must be present. The attributes are examined in the following
     * order until a non-null value is found.
     * <ol>
     * <li>{@link WebDataStoreObject#internalObjectType} and {@link WebDataStoreObject#internalObjectId}</li>
     * <li>{@link WebDataStoreObject#path}</li>
     * </ol>
     * <p/>
     * The following attributes of the provided object may be <em>optionally</em> non-null
     * <ul>
     * <li>{@link WebDataStoreObject#data}</li>
     * <li>{@link WebDataStoreObject#expirationDate}</li>
     * <li>{@link WebDataStoreObject#serviceCallback}</li>
     * <li>{@link WebDataStoreObject#callbackPassthrough}</li>
     * </ul>
     * All other attributes of the provided object are ignored. If one of these attributes is null, it's value is not
     * changed in the web data store except when serviceCallback is non-null and callbackPassthrough is null.
     * <p/>
     * Note that both the object and callback information are persisted before this method returns.
     * <p/>
     * Note that if {@link WebDataStoreObject#setEncryptWithKey(String) encryptWithKey} is non-null,
     * {@link WebDataStoreObject#getData() data} will be replaced with an encrypted version.
     * <p/>
     * A service callback is required to support {@link IWebDataStoreCallback.WebDataStoreCallbackType#REGENERATE
     * REGENERATE} functionality.
     * 
     * @param updateFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     * @throws WebDataStoreException
     * @throws IllegalArgumentException
     *             if the object is not {@link WebDataStoreObject.Type#RESIDENT resident}.
     * @throws InterruptedException
     */
    void updateObjectSync(WebDataStoreObject object, int updateFlags) throws InterruptedException, WebDataStoreException;

    /**
     * Replaces an existing {@link WebDataStoreObject.Type#RESIDENT resident} object in the web data store. This method
     * will not return until the updated object is confirmed to be available for client access.
     * <p/>
     * This method will return before the object has been updated.
     * <p/>
     * At least one of the following object attributes must be present. The attributes are examined in the following
     * order until a non-null value is found.
     * <ol>
     * <li>{@link WebDataStoreObject#internalObjectType} and {@link WebDataStoreObject#internalObjectId}</li>
     * <li>{@link WebDataStoreObject#path}</li>
     * </ol>
     * <p/>
     * The following attributes of the provided object may be <em>optionally</em> non-null
     * <ul>
     * <li>{@link WebDataStoreObject#data}</li>
     * <li>{@link WebDataStoreObject#expirationDate}</li>
     * <li>{@link WebDataStoreObject#serviceCallback}</li>
     * <li>{@link WebDataStoreObject#callbackPassthrough}</li>
     * </ul>
     * All other attributes of the provided object are ignored. If one of these attributes is null, it's value is not
     * changed in the web data store except when serviceCallback is non-null and callbackPassthrough is null.
     * <p/>
     * Note that both the object and callback information are persisted before this method returns.
     * <p/>
     * Note that if {@link WebDataStoreObject#setEncryptWithKey(String) encryptWithKey} is non-null,
     * {@link WebDataStoreObject#getData() data} will be replaced with an encrypted version.
     * 
     * @param updateFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     * @throws IllegalArgumentException
     *             if the object is not {@link WebDataStoreObject.Type#RESIDENT resident}.
     */
    void updateObjectAsync(WebDataStoreObject object, int updateFlags);
    
    /**
     * Attempts to find the provided {@link WebDataStoreObject.Type#RESIDENT resident} object in the web data store. If
     * it is found, the object is updated. Otherwise, the object is created.
     * 
     * @param object
     *            the object to create or update
     * @param createOrUpdateFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     * @throws InterruptedException
     * @throws WebDataStoreException
     * @throws IllegalArgumentException
     *             if the object is not {@link WebDataStoreObject.Type#RESIDENT resident}.
     * @see IWebDataStoreService#findObject(String)
     * @see IWebDataStoreService#findObject(String, long)
     * @see IWebDataStoreService#createObjectSync(WebDataStoreObject, int)
     * @see IWebDataStoreService#updateObjectSync(WebDataStoreObject, int)
     */
    void createOrUpdateObjectSync(WebDataStoreObject object, int createOrUpdateFlags) throws WebDataStoreException, InterruptedException;

    /**
     * Attempts to find the provided {@link WebDataStoreObject.Type#RESIDENT resident} object in the web data store. If
     * it is found, the object is updated. Otherwise, the object is created.
     * <p/>
     * This method will return before the object has been created or updated.
     * 
     * @param object
     *            the object to create or update
     * @param createOrUpdateFlags
     *            optional operation flags such as {@link IWebDataStoreService#OPFLAG_PRECACHE OPFLAG_PRECACHE} or
     *            {@link IWebDataStoreService#OPFLAG_PURGE OPFLAG_PURGE} bitwise OR'd together.
     * @return the objectPath if a new object was created, otherwise null
     * @throws IllegalArgumentException
     *             if the object is not {@link WebDataStoreObject.Type#RESIDENT resident}.
     * @see IWebDataStoreService#findObject(String)
     * @see IWebDataStoreService#findObject(String, long)
     * @see IWebDataStoreService#createObjectAsync(WebDataStoreObject)
     * @see IWebDataStoreService#updateObjectAsync(WebDataStoreObject)
     */
    void createOrUpdateObjectAsync(WebDataStoreObject object, int createOrUpdateFlags);

    //void deleteObjectSync(String objectPath);
    //void deleteObjectAsync(String objectPath, ServiceEndpoint deleteCallback, String callbackPassthrough);
    
    WebDataStoreObject readObject(String objectPath) throws WebDataStoreException, InterruptedException;
    WebDataStoreObject readObject(String objectPath, Long partitionDividend) throws WebDataStoreException, InterruptedException;
    
    void operateObjectSync(Endpoint objectEndpoint, WebDataStoreObjectOperation...operations) throws WebDataStoreException, InterruptedException;
    void operateObjectAsync(Endpoint objectEndpoint, ServiceEndpoint serviceCallback, String callbackPassthrough, WebDataStoreObjectOperation...operations);
}
