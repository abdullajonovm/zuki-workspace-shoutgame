package com.meinc.webcollector.message;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CollectorMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String PARM_TO_WDS = "toWds";

    private String messageId;
    private String messageType;
    private String remoteIpAddress;
    private long timestamp;
    private Map<String,String> properties;
    private Map<String,String> parameters;
    private byte[] body;
    
    public CollectorMessage() { }
    
    public CollectorMessage(CollectorMessage message) {
        messageId = message.messageId;
        messageType = message.messageType;
        remoteIpAddress = message.remoteIpAddress;
        timestamp = message.timestamp;
        properties = message.properties;
        parameters = message.parameters;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public void setRemoteIpAddress(InetAddress remoteIpAddress) {
        this.remoteIpAddress = (remoteIpAddress == null) ? null : remoteIpAddress.getHostAddress();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getProperty(String name) {
        if (properties == null)
            return null;
        return properties.get(name);
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String,String> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String key, String value) {
        if (properties == null)
            properties = new HashMap<String,String>();
        properties.put(key, value);
    }

    public Map<String,String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String,String> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, String value) {
        if (parameters == null)
            parameters = new HashMap<String,String>();
        parameters.put(key, value);
    }
    
    public String getToWds() {
        return (parameters == null) ? null : parameters.get(PARM_TO_WDS);
    }

    public void setToWds(String toWds) {
        addParameter(PARM_TO_WDS, toWds);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("Message id=%s (%s) ts=%s ip=%s props=%s parms=%s", messageId, messageType, new Date(timestamp), remoteIpAddress, properties, parameters);
    }
}
