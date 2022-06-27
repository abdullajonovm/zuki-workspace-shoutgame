package com.meinc.webcollector.message.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private static final Log logger = LogFactory.getLog(BadRequestException.class);
    
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    private Integer errorStatusCode;
    private String errorResponseContentType;
    private String errorResponseBody;
    private Map<String,Object> errorResponseJsonMap;

    public BadRequestException() {
        super();
    }

    public BadRequestException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BadRequestException(String arg0) {
        super(arg0);
    }

    public BadRequestException(Throwable arg0) {
        super(arg0);
    }

    public Integer getErrorStatusCode() {
        return errorStatusCode;
    }

    public BadRequestException withErrorStatusCode(Integer errorStatusCode) {
        this.errorStatusCode = errorStatusCode;
        return this;
    }

    public String getErrorResponseContentType() {
        return errorResponseContentType;
    }

    public BadRequestException withErrorResponseContentType(String errorResponseContentType) {
        this.errorResponseContentType = errorResponseContentType;
        return this;
    }

    public String getErrorResponseBody() {
        if (errorResponseBody != null)
            return errorResponseBody;
        if (errorResponseJsonMap != null)
            try {
                errorResponseBody = jsonMapper.writeValueAsString(errorResponseJsonMap);
            } catch (Exception e) {
                logger.error("Failed to serialize map to json: " + errorResponseJsonMap, e);
                return null;
            }
        return errorResponseBody;
    }

    public BadRequestException withErrorResponseBody(String errorResponseBody) {
        if (errorResponseJsonMap != null)
            throw new IllegalStateException("json response already defined");
        this.errorResponseBody = errorResponseBody;
        return this;
    }
    
    public Map<String,Object> getErrorResponseBodyJsonMap() {
        return errorResponseJsonMap;
    }

    public BadRequestException withErrorResponseBodyJsonKeyValue(String key, Object value) {
        if (errorResponseBody != null)
            throw new IllegalStateException("response body already defined");
        if (errorResponseJsonMap == null)
            errorResponseJsonMap = new LinkedHashMap<String,Object>();
        errorResponseJsonMap.put(key, value);
        return this;
    }
}
