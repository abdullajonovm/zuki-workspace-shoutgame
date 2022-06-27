package com.meinc.webdatastore.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.commons.encryption.HexUtils;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;

public class WebDataStoreObject implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(WebDataStoreObject.class);
    
    public static enum TransferEncoding {
        BASE64
    }
    
    /**
     * Convenience method to calculate a selectMethod number as specified by <a
     * href="https://shouttv.atlassian.net/wiki/display/SHGP/SHOUT+Routing+Document">srd/2.0</a>
     * 
     * @param hashSource
     *            the string used to generate a SHA-256 hash
     * @param selectMethodByteCount
     *            the number of bytes to use from the generated hash
     * @return
     */
    public static long stringToSha256SelectMethodNumber(String hashSource, int selectMethodByteCount) {
        byte[] hash = HexUtils.stringToSha256Bytes(hashSource);
        long result = 0;
        for (int i = 0; i < selectMethodByteCount; i++)
            result |= ((long)hash[i] & 0xFF) << (4-i-1)*8;
        return result;
    }
    
    public static class Endpoint implements Serializable {
        private static final long serialVersionUID = 1L;

        public static enum Root {
            /** Where user uploads are kept waiting for processing */
            USER_UPLOAD("upload"),
            /** Where objects are publicly visible/downloadable */
            WWW("www");
            
            private String name;

            private Root(String rootName) {
                this.name = rootName;
            }

            public String getName() {
                return name;
            }
        }

        private String             toWds;
        private Root               root;
        private String             path;
        private Long               partitionDividend;
        
        public Endpoint() { }
        
        public Endpoint(String toWds, Root root, String path) {
            this.toWds = toWds;
            this.root = root;
            this.path = path;
        }
        
        public Endpoint(Long partitionDividend, Root root, String path) {
            this.partitionDividend = partitionDividend;
            this.root = root;
            this.path = path;
        }
        
        public String getToWds() {
            return toWds;
        }

        public void setToWds(String toWds) {
            this.toWds = toWds;
        }

        public Root getRoot() {
            return root;
        }

        public void setRoot(Root root) {
            this.root = root;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Long getPartitionDividend() {
            return partitionDividend;
        }

        public void setPartitionDividend(Long partitionDividend) {
            this.partitionDividend = partitionDividend;
        }
    }

    private Endpoint           endpoint;
    private byte[]             data;
    private TransferEncoding   transferEncoding;
    private Map<String,Object> properties;
    private Map<String,Object> transientProperties;
    private String             encryptWithKey;
    private Date               expirationDate;
    private String             internalObjectType;
    private Long               internalObjectId;
    private ServiceEndpoint    serviceCallback;
    private String             callbackPassthrough;
    private Date               createDate;
    private Date               updateDate;
    private int                uploadCounter;
    private String             patchInsertBeforePattern;
    private int                patchTailSearchSize;
    
    public WebDataStoreObject() { }
    
    public WebDataStoreObject(WebDataStoreObject source) {
        setEndpoint(source.getEndpoint());
        setData(source.getData());
        setTransferEncoding(source.getTransferEncoding());
        setProperties(source.getProperties());
        setTransientProperties(source.getTransientProperties());
        setEncryptWithKey(source.getEncryptWithKey());
        setExpirationDate(source.getExpirationDate());
        setInternalObjectType(source.getInternalObjectType());
        setInternalObjectId(source.getInternalObjectId());
        setServiceCallbackEndpoint(source.getServiceCallback());
        setCallbackPassthrough(source.getCallbackPassthrough());
        setCreateDate(source.getCreateDate());
        setUpdateDate(source.getUpdateDate());
        for (int i = 0; i < source.getUploadCounter(); i++)
            incrementUploadCounter();
        setPatchInsertBeforePattern(source.getPatchInsertBeforePattern());
        setPatchTailSearchSize(source.getPatchTailSearchSize());
    }
    
    public WebDataStoreObject(Endpoint sourceEndpoint) {
        setEndpoint(sourceEndpoint);
    }

    public Endpoint getEndpoint() {
        if (endpoint == null)
            endpoint = new Endpoint();
        return endpoint;
    }
    
    public void setEndpoint(Endpoint endpoint) {
        getEndpoint().toWds = endpoint.toWds;
        getEndpoint().root = endpoint.root;
        getEndpoint().path = endpoint.path;
        getEndpoint().partitionDividend = endpoint.partitionDividend;
    }

    /** the destination hostname of the wds cache server for this object */
    public String getToWds() {
        return getEndpoint().toWds;
    }

    public void setToWds(String toWds) {
        getEndpoint().toWds = toWds;
    }

    public Root getRoot() {
        return getEndpoint().root;
    }

    public void setRoot(Root root) {
        getEndpoint().root = root;
    }
    
    /** The full object path to the object */
    public String getPath() {
        return getEndpoint().path;
    }

    public void setPath(String path) {
        getEndpoint().path = path;
    }

    /** The content of the object */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public TransferEncoding getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(TransferEncoding transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

    protected void addProperty(String key, Object value) {
        if (properties == null)
            properties = new HashMap<String,Object>();
        properties.put(key, value);
    }
    
    /**
     * Transient properties are in-memory only. They are never sent over the wire nor persisted in any way.
     */
    public void addTransientProperty(String key, Object value) {
        if (transientProperties == null)
            transientProperties = new HashMap<String,Object>();
        transientProperties.put(key, value);
    }
    
    public String getResponseHeader(String key) {
        return (properties == null) ? null : properties.get("RHDR-"+key).toString();
    }

    public void addResponseHeader(String key, String value) {
        addProperty("RHDR-"+key, value);
    }
    
    public void addResponseHeader(String key, Date value) {
        addProperty("RHDR-"+key, value);
    }
    
    public void setLastModifiedDate(Date value) {
        addResponseHeader("Last-Modified", value);
    }
    
    public void setLastTouchedDate(Date value) {
        addResponseHeader("Last-Touched", value);
    }
    
    public String getMetadata(String key) {
        return (properties == null) ? null : properties.get("META-"+key).toString();
    }

    public void addMetadata(String key, String value) {
        addProperty("META-"+key, value);
    }
    
    public void addMetadata(String key, Object value) {
        addProperty("META-"+key, value);
    }
    
    public Map<String,Object> getProperties() {
        return properties;
    }

    /**
     * Transient properties are in-memory only. They are never sent over the wire nor persisted in any way.
     */
    public Map<String,Object> getTransientProperties() {
        return transientProperties;
    }
    
    /**
     * Transient properties are in-memory only. They are never sent over the wire nor persisted in any way.
     */
    public Object getTransientProperty(String key) {
        if (transientProperties == null)
            return null;
        return transientProperties.get(key);
    }

    public void setProperties(Map<String,Object> properties) {
        this.properties = properties;
    }

    /**
     * Transient properties are in-memory only. They are never sent over the wire nor persisted in any way.
     */
    public void setTransientProperties(Map<String,Object> transientProperties) {
        this.transientProperties = transientProperties;
    }

    /** Optional key with which to encrypt the {@link #getData() data} */
    public String getEncryptWithKey() {
        return encryptWithKey;
    }

    public void setEncryptWithKey(String encryptWithKey) {
        this.encryptWithKey = encryptWithKey;
    }

    /** The date this object will automatically be deleted */
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * The internal object type that will be associated with this object. For instance if this object represents a Shout
     * Event, the internalObjectType might be "EVENT", and the internalObjectId might be 1129. Max of 10 characters.
     */
    public String getInternalObjectType() {
        return internalObjectType;
    }

    public void setInternalObjectType(String internalObjectType) {
        this.internalObjectType = internalObjectType;
    }

    /**
     * The internal object id that will be associated with this object. For instance if this object represents a Shout
     * Event, the internalObjectType might be "EVENT", and the internalObjectId might be 1129.
     */
    public Long getInternalObjectId() {
        return internalObjectId;
    }

    public void setInternalObjectId(Long internalObjectId) {
        this.internalObjectId = internalObjectId;
    }

    /**
     * <strong>NOTE: Do not use this property unless you know for certain the client will gracefully work around a
     * WDS object that appears to be missing because it ended up on a different WDS server than the client expected.</strong>
     * <p/>
     * The dividend to a modulo operation that calculates the object's {@link Distribution#PARTITION distribution
     * partition}. The divisor is the number of total partitions. For example, if a subscriber object can be mapped to
     * the number 20,495,839,493 and there are 4 server partitions, the subscriber object would be assigned to partition
     * 1 (the second partition) because 20,495,839,493 % 4 is 1.
     */
    public Long getPartitionDividend() {
        return getEndpoint().partitionDividend;
    }

    public void setPartitionDividend(Long partitionSeed) {
        getEndpoint().partitionDividend = partitionSeed;
    }

    /**
     * The callback to be invoked when this object is confirmed to be available for client access, and when the object
     * is expired and about to be evicted.
     */
    public ServiceEndpoint getServiceCallback() {
        return serviceCallback;
    }
    
    public String getServiceCallbackString() {
        return serviceCallback == null ? null : serviceCallback.toString();
    }
    
    public void setServiceCallbackEndpoint(ServiceEndpoint serviceEndpoint) {
        serviceCallback = serviceEndpoint;
    }
    
    public void setServiceCallback(String serviceEndpointString) {
        serviceCallback = new ServiceEndpoint(serviceEndpointString);
    }
    
    /**
     * A pass-through value provided to the {@link #getServiceCallback() service callback} method. Max of 10 characters.
     */
    public String getCallbackPassthrough() {
        return callbackPassthrough;
    }

    public void setCallbackPassthrough(String callbackPassthrough) {
        this.callbackPassthrough = callbackPassthrough;
    }

    /** The timestamp this object was created */
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /** The timestamp this object was last updated */
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public int getUploadCounter() {
        return uploadCounter;
    }

    public void incrementUploadCounter() {
        this.uploadCounter += 1;
    }
    
    public String getPatchInsertBeforePattern() {
        return patchInsertBeforePattern;
    }

    public void setPatchInsertBeforePattern(String patchInsertBeforePattern) {
        this.patchInsertBeforePattern = patchInsertBeforePattern;
    }

    public int getPatchTailSearchSize() {
        return patchTailSearchSize;
    }

    public void setPatchTailSearchSize(int patchTailSearchSize) {
        this.patchTailSearchSize = patchTailSearchSize;
    }

    @Override
    public String toString() {
        String selfJson = "{'wds':%s,'path':%s,'data':%s,'expirationDate':%s,'internalObjectType':%s,'internalObjectId':%s,'serviceCallback':%s,'callbackPassthrough':%s,'patchPattern':%s,'patchLen':%d,'partitionDividend':%s}".replaceAll("'", "\"");
        return String.format(selfJson,
                             getToWds() == null ? "null" : "\""+getToWds()+"\"",
                             getPath() == null ? "null" : "\""+getPath()+"\"",
                             getData() == null ? "null" : "\""+new String(getData())+"\"",
                             getExpirationDate() == null ? "null" : "\""+getExpirationDate().toString()+"\"",
                             getInternalObjectType() == null ? "null" : "\""+getInternalObjectType()+"\"",
                             getInternalObjectId() == null ? "null" : "\""+getInternalObjectId()+"\"",
                             getServiceCallback() == null ? "null" : "\""+getServiceCallback()+"\"",
                             getCallbackPassthrough() == null ? "null" : "\""+getCallbackPassthrough()+"\"",
                             getPatchInsertBeforePattern() == null ? "null" : "\""+getPatchInsertBeforePattern()+"\"",
                             getPatchTailSearchSize(),
                             getPartitionDividend() == null ? "null" : "\""+getPartitionDividend()+"\"");
    }
}
