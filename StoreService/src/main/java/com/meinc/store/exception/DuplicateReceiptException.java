package com.meinc.store.exception;

import java.io.Serializable;

public class DuplicateReceiptException extends Exception implements Serializable {
    
    private static final long serialVersionUID = -4354899914304591437L;
    
    protected String _storeUid;

    public DuplicateReceiptException() {
        super();
    }

    public DuplicateReceiptException(String message, Throwable exception) {
        super(message, exception);
    }

    public DuplicateReceiptException(String message) {
        super(message);
    }

    public DuplicateReceiptException(Throwable exception) {
        super(exception);
    }
    
    public DuplicateReceiptException(String message, String storeUid, Throwable exception) {
        super(message, exception);
        _storeUid = storeUid;
    }

    public String getStoreUid() {
        return _storeUid;
    }

    public void setStoreUid(String storeUid) {
        _storeUid = storeUid;
    }
    
    

}
