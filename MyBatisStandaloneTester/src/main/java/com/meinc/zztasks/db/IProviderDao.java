package com.meinc.zztasks.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.zztasks.domain.AsyncProviderAddStatus;
import com.meinc.zztasks.domain.Provider;

public interface IProviderDao
{
    @Select(
        "SELECT count(*) " +
        "  FROM ergo.`provider` " +
        " WHERE subscriber_id = #{0} AND provider_type = 'ERGO'"
    )
    boolean doesDefaultProviderExist(int subscriberId);

    @Select("SELECT * FROM ergo.`provider` WHERE provider_uuid = #{1} AND subscriber_id = #{0}")
    @Results({
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="displayName", column="display_name"),
        @Result(property="type", column="provider_type"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="accessDate", column="access_date"),
        @Result(property="userName", column="username"),
        @Result(property="email", column="email"),
        @Result(property="oAuthAuthToken", column="oauth_auth_token"),
        @Result(property="password", column="passwd"),
        @Result(property="domain", column="domain"),
        @Result(property="server", column="server"),
        @Result(property="webServer", column="web_server"),
        @Result(property="serverVersion", column="serverVersion"),
        @Result(property="authMethod", column="authMethod"),
        @Result(property="syncStateTasks", column="sync_state_tasks"),
        @Result(property="syncStateNotes", column="sync_state_notes"),
        @Result(property="resetSync", column="reset_sync"),
        @Result(property="ssl", column="ssl")
    })
    Provider getProvider(int subscriberId, String providerUuid);

    @Select("SELECT * FROM ergo.`provider` WHERE subscriber_id = #{0}")
    @Results({
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="displayName", column="display_name"),
        @Result(property="type", column="provider_type"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="accessDate", column="access_date"),
        @Result(property="userName", column="username"),
        @Result(property="email", column="email"),
        @Result(property="oAuthAuthToken", column="oauth_auth_token"),
        @Result(property="password", column="passwd"),
        @Result(property="domain", column="domain"),
        @Result(property="server", column="server"),
        @Result(property="webServer", column="web_server"),
        @Result(property="serverVersion", column="serverVersion"),
        @Result(property="authMethod", column="authMethod"),
        @Result(property="syncStateTasks", column="sync_state_tasks"),
        @Result(property="syncStateNotes", column="sync_state_notes"),
        @Result(property="resetSync", column="reset_sync"),
        @Result(property="ssl", column="ssl")
    })
    List<Provider> getProviders(int subscriberId);

    @Insert(
        "INSERT INTO ergo.`provider` ( " +
        "   provider_uuid, subscriber_id, provider_type, " +
        "   display_name, `username`, email, `passwd`," +
        "   oauth_auth_token, `domain`, `server`, `web_server`, `authMethod`," +
        "  `serverVersion`, `sync_state_tasks`, `sync_state_notes`, `reset_sync`, `ssl`, " +
        "   create_date, update_date) VALUES ( " +
        "       #{1.providerUuid}, #{0}, #{1.type}, " +
        "       #{1.displayName}, #{1.userName}, #{1.email}, #{1.password}, " +
        "       #{1.oAuthAuthToken}, #{1.domain}, #{1.server}, #{1.webServer}, #{1.authMethod}, " +
        "       #{1.serverVersion}, #{1.syncStateTasks}, #{1.syncStateNotes}, #{1.resetSync}, #{1.ssl}, " +
        "       #{1.createDate}, #{1.lastUpdate})"
    )
    void addProvider(int subscriberId, Provider provider);

    @Delete("DELETE FROM ergo.`provider` WHERE provider_uuid = #{0}")
    void removeProvider(String providerUuid);

    @Update(
        "UPDATE ergo.`provider` SET " +
        "   display_name = #{provider.displayName}, `username` = #{provider.userName}, email = #{provider.email}, `passwd` = #{provider.password}, " +
        "   `oauth_auth_token` = #{provider.oAuthAuthToken}, `domain` = #{provider.domain}, `server` = #{provider.server}, " +
        "   `web_server` = #{provider.webServer}, `authMethod` = #{provider.authMethod}, `serverVersion` = #{provider.serverVersion}, " +
        "   `sync_state_tasks` = #{provider.syncStateTasks}, `sync_state_notes` = #{provider.syncStateNotes}, `reset_sync` = #{provider.resetSync}, " +
        "   `ssl` = #{provider.ssl}, update_date = #{provider.lastUpdate} " +
        " WHERE provider_uuid = #{provider.providerUuid}"
    )
    void updateProvider(@Param("provider") Provider provider, boolean makeSureToEncryptFirstIfNecessary_seeBaseDataManager_resetSync); //TODO: remove boolean after conversion is complete

    @Insert(
        "INSERT INTO ergo.`provider_add_async_transaction` (transaction_uuid, subscriber_id, provider_type, provider_uuid, `status`, user_status, update_date) VALUES (" +
        "   #{0}, #{1}, #{2}, #{3}, #{4}, null, NOW())"
    )
    void beginAddProviderAsync(String transactionId, int subscriberId, String providerType, String providerUuid, String status);

    @Select("SELECT user_status FROM ergo.`provider_add_async_transaction` WHERE transaction_uuid = #{0}")
    String getAddProviderAsyncUserStatus(String transactionId);

    @Update("UPDATE ergo.`provider_add_async_transaction` SET `status` = #{1}, update_date = NOW() WHERE transaction_uuid = #{0}")
    void finishAddProviderAsync(String transactionId, String status);

    @Select("SELECT `status`, provider_uuid FROM ergo.`provider_add_async_transaction` WHERE transaction_uuid = #{0}")
    @Results({
        @Result(property="status", column="status"),
        @Result(property="providerId", column="provider_uuid")
    })
    AsyncProviderAddStatus getProviderAsyncAddStatus(String transactionId);

    @Update("UPDATE ergo.`provider_add_async_transaction` SET `user_status` = 'CANCEL', `status` = 'USER_CANCELLED', update_date = NOW() WHERE transaction_uuid = #{0}")
    void cancelProviderAsyncAdd(String transactionId);

    @Select("SELECT * FROM ergo.`provider` WHERE subscriber_id = #{0} AND access_date IS NOT NULL ORDER BY access_date DESC LIMIT 1")
    @Results({
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="displayName", column="display_name"),
        @Result(property="type", column="provider_type"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="accessDate", column="access_date"),
        @Result(property="userName", column="username"),
        @Result(property="email", column="email"),
        @Result(property="oAuthAuthToken", column="oauth_auth_token"),
        @Result(property="password", column="passwd"),
        @Result(property="domain", column="domain"),
        @Result(property="server", column="server"),
        @Result(property="webServer", column="web_server"),
        @Result(property="serverVersion", column="serverVersion"),
        @Result(property="authMethod", column="authMethod"),
        @Result(property="syncStateTasks", column="sync_state_tasks"),
        @Result(property="syncStateNotes", column="sync_state_notes"),
        @Result(property="resetSync", column="reset_sync"),
        @Result(property="ssl", column="ssl")
    })
    Provider getRecentProvider(int subscriberId);

    @Select("SELECT * FROM ergo.`provider` WHERE subscriber_id = #{0} AND provider_type = 'ERGO'")
    @Results({
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="displayName", column="display_name"),
        @Result(property="type", column="provider_type"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="accessDate", column="access_date"),
        @Result(property="userName", column="username"),
        @Result(property="email", column="email"),
        @Result(property="oAuthAuthToken", column="oauth_auth_token"),
        @Result(property="password", column="passwd"),
        @Result(property="domain", column="domain"),
        @Result(property="server", column="server"),
        @Result(property="webServer", column="web_server"),
        @Result(property="serverVersion", column="serverVersion"),
        @Result(property="authMethod", column="authMethod"),
        @Result(property="syncStateTasks", column="sync_state_tasks"),
        @Result(property="syncStateNotes", column="sync_state_notes"),
        @Result(property="resetSync", column="reset_sync"),
        @Result(property="ssl", column="ssl")
    })
    Provider getDefaultProvider(int subscriberId);

    @Update(
        "UPDATE ergo.`provider` " +
        "SET `access_date` = NOW() " +
        "WHERE provider_uuid = #{0}"
    )
    void touchProvider(String providerUuid);
}
