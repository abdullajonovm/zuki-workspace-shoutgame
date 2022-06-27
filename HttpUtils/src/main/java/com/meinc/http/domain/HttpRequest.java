package com.meinc.http.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tv.shout.util.MultiMap;

public class HttpRequest implements Serializable {
    private static final long serialVersionUID = 2L;

    private byte[] body;
    private MultiMap<String,String> parameters;
    private String remoteAddr;
    private String contentType;
    private String method;
    private String userAgent;
    private String requestUrl;
    private String path;
    private String effectivePath;
    private String serverName;
    private Map<String,String> headers;
    private Set<String> headerNames;

    public HttpRequest() {
        headers = new HashMap<String, String>();
        headerNames = new HashSet<String>();
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public byte[] getBody() {
        return body;
    }
    
    public void setBody(byte[] body) {
        this.body = body;
    }
    
    public MultiMap<String,String> getParameters() {
        return parameters;
    }
    
    public void setParameters(MultiMap<String,String> parameters) {
        this.parameters = parameters;
    }
    
    public String getHeader(String headerName) {
        return headers.get(headerName.toLowerCase());
    }
    
    public void addHeader(String headerName, String value) {
        headerNames.add(headerName);
        headers.put(headerName.toLowerCase(), value);
    }
    
    public String getFirstParameter(String parmName) {
        return Optional.ofNullable(parameters)
                .map(parms -> parms.get(parmName))
                .map(values -> values.isEmpty() ? null : values.get(0))
                .orElse(null);
    }

    public List<String> getParameter(String parmName) {
        return parameters == null ? null : parameters.get(parmName);
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }
    
    public String getRequestURL() {
        return requestUrl;
    }
    
    public void setRequestURL(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEffectivePath() {
        return effectivePath;
    }

    public void setEffectivePath(String path) {
        this.effectivePath = path;
    }

    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
