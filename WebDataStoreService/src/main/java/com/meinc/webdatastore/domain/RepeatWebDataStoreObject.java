package com.meinc.webdatastore.domain;




public class RepeatWebDataStoreObject extends WebDataStoreObject {
    private static final long serialVersionUID = 1L;

    private String hostname;
    
    public RepeatWebDataStoreObject(WebDataStoreObject source, String hostname) {
        super(source);
        this.hostname = hostname;
    }

    public RepeatWebDataStoreObject(RepeatWebDataStoreObject source) {
        super(source);
        this.hostname = source.hostname;
    }

    public RepeatWebDataStoreObject(Endpoint endpoint, String hostname) {
        super(new WebDataStoreObject(endpoint));
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }
    
    @Override
    public String toString() {
        return super.toString().replaceFirst("}$", ",\"hostname\":\""+hostname+"\"}");
    }
}