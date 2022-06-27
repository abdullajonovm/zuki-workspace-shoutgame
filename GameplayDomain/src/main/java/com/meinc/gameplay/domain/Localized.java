package com.meinc.gameplay.domain;

import java.io.Serializable;

public class Localized implements Serializable
{
    private static final long serialVersionUID = 4582661545954686688L;
    public static final int classId = 1001;
    
    private String _localizedUuid;
    private String _languageCode;
    private String _value;
    
    public Localized()
    {
    }
    
    public Localized(String localizedUuid, String languageCode, String value)
    {
        _localizedUuid = localizedUuid;
        _languageCode = languageCode;
        _value = value;
    }

    public String getLocalizedUuid()
    {
        return _localizedUuid;
    }

    public void setLocalizedUuid(String localizedUuid)
    {
        _localizedUuid = localizedUuid;
    }
    
    public String getLanguageCode()
    {
        return _languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        _languageCode = languageCode;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }
}
