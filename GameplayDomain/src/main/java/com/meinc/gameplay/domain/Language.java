package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.text.MessageFormat;

public class Language 
implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int classId = 1012;

    private String _languageCode;
    private boolean _default;
    
    public String getLanguageCode()
    {
        return _languageCode;
    }
    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }
    public boolean isDefault()
    {
        return _default;
    }
    public void setDefault(boolean default1)
    {
        _default = default1;
    }
    
    @Override
    public String toString()
    {
        return MessageFormat.format("code: {0}, default: {1}", _languageCode, _default);
    }
}
