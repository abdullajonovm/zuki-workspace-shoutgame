package com.meinc.mrsoa.service;

import com.meinc.mrsoa.net.inbound.MrSoaRequest;

public abstract class ServiceMethod {
    protected long logId;
    
    public static String getMethodKey(String methodName, int arity) {
        return methodName + "[" + arity + "]";
    }
    
    public abstract Object invoke(MrSoaRequest request, Object... args) throws Throwable;

    public void setLogId(long logId) {
        this.logId = logId;
    }

    @Override
    public abstract String toString();
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
