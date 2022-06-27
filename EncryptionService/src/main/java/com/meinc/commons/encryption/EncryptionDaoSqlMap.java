package com.meinc.commons.encryption;

import java.util.Date;
import java.util.List;


public class EncryptionDaoSqlMap
{
    private IEncryptionDaoMapper _mapper;
    
    public String insertMungeValue(String namespace, String origValue, String mungeValue, Date expiresDate)
    {
        List<String> oldMungedValues = _mapper.getMungedValue(namespace, origValue);
        if (oldMungedValues != null && !oldMungedValues.isEmpty()) {
            for (String oldMungedValue : oldMungedValues)
                deleteMungedValue(namespace, oldMungedValue);
        }

        _mapper.insertMungeValue(namespace, origValue, mungeValue, expiresDate);
        
        return mungeValue;
    }

    public String getUnMungedValue(String namespace, String mungeValue)
    {
        return _mapper.getUnMungedValue(namespace, mungeValue);
    }

    public String getMungedValue(String namespace, String origValue)
    {
        List<String> mungedValues = _mapper.getMungedValue(namespace, origValue);
        if (mungedValues == null || mungedValues.isEmpty())
            return null;
        return mungedValues.get(0);
    }

    public IEncryptionDaoMapper getMapper()
    {
        return _mapper;
    }

    public void setMapper(IEncryptionDaoMapper mapper)
    {
        _mapper = mapper;
    }

    public void deleteMungedValue(String namespace, String mungeValue) {
        _mapper.deleteMungedValue(namespace, mungeValue);
    }

    public void deleteUnMungedValue(String namespace, String origValue) {
        _mapper.deleteUnMungedValue(namespace, origValue);
    }
}