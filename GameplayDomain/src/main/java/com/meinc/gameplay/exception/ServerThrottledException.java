package com.meinc.gameplay.exception;

public class ServerThrottledException extends Exception {
    private static final long serialVersionUID = 635242945220715362L;

    public ServerThrottledException() { }
    
    public ServerThrottledException(String throttledMessage) {
        super(throttledMessage);
    }
}
