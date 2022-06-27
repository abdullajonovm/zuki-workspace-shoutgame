package com.meinc.webdatastore.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.webdatastore.domain.WebDataStoreObject;

public interface IWebDataStoreDaoMapper
{
    @Insert(" INSERT INTO webdatastore.object (path, expiration_date, internal_type, internal_id,       " +
            "                                  service_callback,                                        " +
            "                                  callback_passthrough, create_date, update_date)          " +
            "      VALUES (#{path}, #{expirationDate}, #{internalObjectType}, #{internalObjectId},      " +
            "              #{serviceCallbackString}, #{callbackPassthrough}, NOW(), NOW())              " )
    public void createObject(WebDataStoreObject object);
    
    @Insert(" INSERT INTO webdatastore.object (path, expiration_date, internal_type, internal_id,       " +
            "                                  service_callback,                                        " +
            "                                  callback_passthrough, create_date, update_date)          " +
            "      VALUES (#{path}, #{expirationDate}, #{internalObjectType}, #{internalObjectId},      " +
            "              #{serviceCallbackString}, #{callbackPassthrough}, NOW(), NOW())              " +
            " ON DUPLICATE KEY UPDATE expiration_date      = #{expirationDate},                         " +
            "                         service_callback     = #{serviceCallbackString},                  " +
            "                         callback_passthrough = #{callbackPassthrough},                    " +
            "                         update_date          = NOW()                                      " )
    public int createOrUpdateObject(WebDataStoreObject object);
    
    @Select("SELECT 'RESIDENT' AS `type`, `object`.* FROM webdatastore.object WHERE internal_type=#{0} AND internal_id=#{1}")
    @Results({
        @Result(column="internal_type", property="internalObjectType"),
        @Result(column="internal_id",   property="internalObjectId")
    })
    public WebDataStoreObject getObjectByInternalId(String internalObjectType, Long internalObjectId);

    @Select("SELECT 'RESIDENT' AS `type`, `object`.* FROM webdatastore.object WHERE path=#{0}")
    @Results({
        @Result(column="internal_type", property="internalObjectType"),
        @Result(column="internal_id",   property="internalObjectId")
    })
    public WebDataStoreObject getObjectByPath(String objectPath);

    @Update(" UPDATE webdatastore.object SET " +
            "        expiration_date = #{expirationDate}, " +
            "        service_callback = #{serviceCallbackString}, " +
            "        callback_passthrough = #{callbackPassthrough}, " +
            "        update_date = NOW() " +
            "  WHERE id = #{id} ")
    public void updateObject(WebDataStoreObject object);
}
