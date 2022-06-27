package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a localized string value
 */
public class LocalizedValue 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _languageCode;
    private String _value;
    
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
    
    @Override
    public String toString()
    {
        return MessageFormat.format("{0}: {1}", _languageCode, _value);
    }
    
    public static LocalizedValue fromLocalized(Localized localized)
    {
        LocalizedValue val = new LocalizedValue();
        val.setLanguageCode(localized.getLanguageCode());
        val.setValue(localized.getValue());
        return val;
    }
    
    public static List<LocalizedValue> fromLocalized(List<Localized> localized, String legacyValue)
    {
        List<LocalizedValue> list = new ArrayList<LocalizedValue>();
        
        if (localized == null) {
            LocalizedValue val = new LocalizedValue();
            val.setLanguageCode("en");
            val.setValue(legacyValue);
            list.add(val);
        } else { 
            for (Localized loc : localized) {
                list.add(fromLocalized(loc));
            }
        }
        
        return list;
    }
}
