package com.meinc.gameplay.domain;

import java.io.Serializable;

public class ExternalEventMapping 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public int appId;
    public int eventId;
    public String externalEventId;
}
