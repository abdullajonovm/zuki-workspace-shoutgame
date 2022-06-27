package com.meinc.webcollector.message.handler;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.meinc.webcollector.message.CollectorMessage;
import com.meinc.webcollector.message.handler.SyncRequest.HttpResponse;

public class CollectorMessageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private CollectorMessage message;
    private Map<String,Object> jsonKeyValueMap;
    private String responseBodyContentType;
    private byte[] responseBody;
    private HttpResponse entireResponse;
    
    public CollectorMessageResult() { }
    
    public CollectorMessageResult(CollectorMessage message) {
        setMessage(message);
    }
    
    public CollectorMessage getMessage() {
        return message;
    }

    public void setMessage(CollectorMessage message) {
        this.message = message;
    }

    public Map<String,Object> getJsonKeyValueMap() {
        return jsonKeyValueMap;
    }
    
    public CollectorMessageResult withJsonKeyValueResponse(String key, Object value) {
        if (    value != null &&
                !(value instanceof String) &&
                !(value instanceof Integer) &&
                !(value instanceof Long) &&
                !(value instanceof Float) &&
                !(value instanceof Double) &&
                !(value instanceof Boolean))
            throw new IllegalArgumentException("invalid json value: " + value.toString());

        if (jsonKeyValueMap == null)
            jsonKeyValueMap = new LinkedHashMap<String,Object>();
        jsonKeyValueMap.put(key, value);

        return this;
    }

    public String getResponseBodyContentType() {
        return responseBodyContentType;
    }

    public void setResponseBodyContentType(String responseBodyContentType) {
        this.responseBodyContentType = responseBodyContentType;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBodyBytes) {
        this.responseBody = responseBodyBytes;
    }

    /**
     * @return optional response in its entirety - takes precedence if not null
     */
    public HttpResponse getEntireResponse() {
        return entireResponse;
    }

    public void setEntireResponse(HttpResponse httpResponse) {
        this.entireResponse = httpResponse;
    }
}