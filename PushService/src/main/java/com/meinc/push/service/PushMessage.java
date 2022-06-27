package com.meinc.push.service;

import java.io.Serializable;
import java.util.List;

public class PushMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int ANDROID = 0;
    public static final int IOS     = 1;
    public static final int WINDOWS = 2;
    
    public int type;
    public List<String> tokens;
    public String payload;
}