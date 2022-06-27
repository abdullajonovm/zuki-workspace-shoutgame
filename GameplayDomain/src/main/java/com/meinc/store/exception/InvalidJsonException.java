package com.meinc.store.exception;

public class InvalidJsonException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String json;
    
    public InvalidJsonException(String json, Exception e) {
        super(e);
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
