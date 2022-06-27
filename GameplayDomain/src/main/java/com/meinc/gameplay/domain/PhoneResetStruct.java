package com.meinc.gameplay.domain;

import java.io.Serializable;

public class PhoneResetStruct
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public int subscriberId;
    public String phone;
    public String code;
    public boolean playViaText;
    
    public PhoneResetStruct()
    {
    }
    
    public PhoneResetStruct(int subscriberId, String phone, String code, boolean playViaText)
    {
        this.subscriberId = subscriberId;
        this.phone = phone;
        this.code = code;
        this.playViaText = playViaText;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        
        buf.append("sID: " + this.subscriberId);
        buf.append(", phone: " + this.phone);
        buf.append(", code: " + this.code);
        buf.append(", playViaText: " + this.playViaText);
        
        return buf.toString();
    }
}
