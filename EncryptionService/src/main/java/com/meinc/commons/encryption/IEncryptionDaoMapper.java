package com.meinc.commons.encryption;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface IEncryptionDaoMapper
{
    public void insertMungeValue(@Param("ns") String namespace, @Param("origValue") String origValue, @Param("mungeValue") String mungeValue, @Param("expiresDate") Date expiresDate);
    public List<String> getMungedValue(@Param("ns") String namespace, @Param("origValue") String origValue);
    public String getUnMungedValue(@Param("ns") String namespace, @Param("mungeValue") String mungeValue);
    public void deleteMungedValue(@Param("ns") String namespace, @Param("mungeValue") String mungeValue);
    public String deleteUnMungedValue(@Param("ns") String namespace, @Param("origValue") String origValue);
}
