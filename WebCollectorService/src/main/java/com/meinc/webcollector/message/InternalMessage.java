package com.meinc.webcollector.message;

import java.io.File;

class InternalMessage extends CollectorMessage {
    private static final long serialVersionUID = 1L;
    File messageFile;
    
    public InternalMessage(CollectorMessage message) {
        super(message);
    }
}