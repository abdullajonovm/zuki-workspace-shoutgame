package com.meinc.identity.domain;

import java.io.Serializable;

public class NicknameContext 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private int _contextId;
    private String _nickname;
    
    public int getContextId()
    {
        return _contextId;
    }
    public void setContextId(int contextId)
    {
        _contextId = contextId;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
}
