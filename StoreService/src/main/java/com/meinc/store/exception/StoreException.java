package com.meinc.store.exception;

import java.util.Map;

public class StoreException extends Exception {

    private static final long serialVersionUID = 1L;
    private Map<String, Object> _codeMsgMap;

    public StoreException() {
        super();
    }

    public StoreException(String msg) {
        super(msg);
    }

    public StoreException(String msg, Exception exception) {
        super(msg, exception);
    }

    /**
     * To pass more granular detail about what went wrong. For example, the map might contain:
     * [
     *      {"81715", "Credit Card Number is Invalid"},
     *      {"81709", "Credit Card Expiration Date is Required},
     *      ...
     * ]
     *
     * @param codeMsgMap
     */
    public StoreException(Map<String, Object> codeMsgMap)
    {
        super();
        _codeMsgMap = codeMsgMap;
    }

    public Map<String, Object> getCodeMsgMap()
    {
        return _codeMsgMap;
    }

}
