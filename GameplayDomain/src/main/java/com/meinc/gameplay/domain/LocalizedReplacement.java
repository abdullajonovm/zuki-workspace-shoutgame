/**
 * 
 */
package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author grant
 *
 */
public class LocalizedReplacement 
implements Serializable /*, Portable */
{
    private static final long serialVersionUID = 5971863624738394329L;
//    public static final int classId = 1013;
    private List<Localized> _localized;
    private String _defaultValue;
    private String _replacementKey;
    
    public LocalizedReplacement()
    {
    }
    
    public LocalizedReplacement(List<Localized> localized, String defaultValue, String replacementKey)
    {
        _localized = localized;
        _defaultValue = defaultValue;
        _replacementKey = replacementKey;
    }
/*
    public static ClassDefinition getClassDefinition()
    {
        ClassDefinitionBuilder builder = new ClassDefinitionBuilder(1, classId);
        builder.addPortableArrayField("localized", Localized.getClassDefinition());            
        builder.addUTFField("defaultValue");
        builder.addUTFField("replacementKey");
        return builder.build();
    }
    
    @Override
    @JsonIgnore
    public int getFactoryId()
    {
        return 1;
    }
    
    @Override
    @JsonIgnore
    public int getClassId()
    {
        return classId;
    }
    
    @Override
    public void writePortable(PortableWriter writer) throws IOException
    {
        if (_localized != null)
            writer.writePortableArray("localized", _localized.toArray(new Portable[_localized.size()]));            
        else
            writer.writePortableArray("localized", null);
        
        writer.writeUTF("defaultValue", _defaultValue);
        writer.writeUTF("replacementKey", _replacementKey);
    }
    
    @Override
    public void readPortable(PortableReader reader) throws IOException
    {
        Portable[] list = reader.readPortableArray("localized");
        if (list != null && list.length > 0)
            _localized = Arrays.asList(Arrays.asList(list).toArray(new Localized[list.length]));

        _defaultValue = reader.readUTF("defaultValue");
        _replacementKey = reader.readUTF("replacementKey");
    }
*/
    public List<Localized> getLocalized()
    {
        return _localized;
    }

    public void setLocalized(List<Localized> localized)
    {
        _localized = localized;
    }

    public String getDefaultValue()
    {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        _defaultValue = defaultValue;
    }

    public String getReplacementKey()
    {
        return _replacementKey;
    }

    public void setReplacementKey(String replacementKey)
    {
        _replacementKey = replacementKey;
    }
}
