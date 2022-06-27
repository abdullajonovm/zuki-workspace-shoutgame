package com.meinc.identity.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataIntegrityViolationException;

import com.meinc.identity.domain.FacebookIdentityInfo;
import com.meinc.identity.domain.ForeignHostIdentityInfo;
import com.meinc.identity.domain.NicknameContext;
import com.meinc.identity.domain.Subscriber;
import com.meinc.identity.domain.Subscriber.ROLE;
import com.meinc.identity.domain.SubscriberAddress;
import com.meinc.identity.domain.SubscriberEmail;
import com.meinc.identity.domain.SubscriberIdAndLanguageCode;
import com.meinc.identity.domain.SubscriberSession;

public interface ISubscriberDaoMapper
{
    @Insert(
        "INSERT INTO gameplay.s_subscriber (" +
        "     context_id, firstname, lastname, nickname, nickname_set, facebook_user_flag, photo_url, photo_url_small, photo_url_large, " +
        "     email, primary_identifier, passwd, passwd_set, encrypt_key, email_sha256_hash, email_hash_prefix, language_code, " +
        "     phone, phone_verified, from_country_code, region, date_of_birth, is_adult_flag, create_date, update_date) " +
        "VALUES (" +
        "     #{contextId}, #{firstname}, #{lastname}, #{nickname}, #{nicknameSet}, #{facebookUserFlag}, #{photoUrl}, #{photoUrlSmall}, #{photoUrlLarge}, " +
        "     #{email}, #{primaryId}, password(#{passwd}), #{passwdSet}, #{encryptKey}, #{emailSha256Hash}, #{emailHashPrefix}, #{languageCode}, " +
        "     #{phone}, 0, #{fromCountryCode}, #{region}, #{dateOfBirth}, #{adultFlag}, NOW(), NOW())"
    )
    @Options(useGeneratedKeys=true, keyProperty="subscriberId", keyColumn="subscriber_id")
    int addSubscriberFromSignup(Subscriber s) throws DataIntegrityViolationException;

    @Insert(
        "INSERT INTO gameplay.s_subscriber (" +
        "     context_id, firstname, lastname, nickname, nickname_set, facebook_user_flag, photo_url, photo_url_small, photo_url_large, " +
        "     email, primary_identifier, passwd, passwd_set, encrypt_key, email_sha256_hash, email_hash_prefix, language_code, " +
        "     phone, phone_verified, from_country_code, region, date_of_birth, is_adult_flag, create_date, update_date) " +
        "VALUES (" +
        "     #{contextId}, #{firstname}, #{lastname}, #{nickname}, #{nicknameSet}, #{facebookUserFlag}, #{photoUrl}, #{photoUrlSmall}, #{photoUrlLarge}, " +
        "     #{email}, #{primaryId}, #{passwd}, #{passwdSet}, #{encryptKey}, #{emailSha256Hash}, #{emailHashPrefix}, #{languageCode}, " +
        "     #{phone}, 0, #{fromCountryCode}, #{region}, #{dateOfBirth}, #{adultFlag}, NOW(), NOW())"
    )
    @Options(useGeneratedKeys=true, keyProperty="subscriberId", keyColumn="subscriber_id")
    int addSubscriberFromSignupUsingCleartextPassword(Subscriber s) throws DataIntegrityViolationException;

    @Insert(
        "INSERT INTO gameplay.s_subscriber_session (" +
        "     subscriber_id, context_id, device_id, session_key, " +
        "     device_model, device_name, device_version," +
        "     os_name, os_type," +
        "     app_id, app_version, " +
        "     added_date, last_authenticated_date) " +
        "VALUES (" +
        "     #{subscriberId}, #{contextId}, #{deviceId}, #{sessionKey}, " +
        "     #{deviceModel}, #{deviceName}, #{deviceVersion}, " +
        "     #{osName}, #{osType}, " +
        "     #{appId}, #{appVersion}, " +
        "     #{addedDate}, #{lastAuthenticatedDate})"
    )
    @Options(useGeneratedKeys=false)
    int addSubscriberSession(SubscriberSession ss);

    @Update(
        "UPDATE gameplay.s_subscriber_session " +
        "   SET session_key = #{sessionKey}, last_authenticated_date = #{lastAuthenticatedDate}, " +
        "       device_model = #{deviceModel}, device_name = #{deviceName}, device_version = #{deviceVersion}, " +
        "       os_name = #{osName}, os_type = #{osType}, " +
        "       app_id = #{appId}, app_version = #{appVersion} " +
        " WHERE subscriber_id = #{subscriberId} AND device_id = #{deviceId}"
    )
    void updateSubscriberSession(SubscriberSession ss);

    @Select(
        "SELECT count(*) " +
        "  FROM gameplay.s_subscriber " +
        " WHERE subscriber_id = #{0} AND passwd = CONVERT(password(#{1}) USING latin1)"
    )
    boolean isSubscriberOldstylePasswordValid(long subscriberId, String password);

    @Insert(
        "INSERT INTO gameplay.s_subscriber_nickname_history (" +
        "     subscriber_id, old_nickname, new_nickname, change_date) " +
        "VALUES (" +
        "     #{0}, #{1}, #{2}, NOW())"
    )
    @Options(useGeneratedKeys=false)
    int addSubscriberNicknameHistory(long subscriberId, String oldNickname, String newNickname);

    @Select("SELECT * FROM gameplay.s_subscriber WHERE email = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> getSubscribersByEmail(String email);

    @Select("SELECT s.* FROM gameplay.s_subscriber s WHERE s.context_id = #{0} AND s.email = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberByEmail(int contextId, String email);

    @Select("SELECT s.* FROM gameplay.s_subscriber s, gameplay.s_identity_fb fb where fb.facebook_id = #{0} AND s.subscriber_id = fb.subscriber_id")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> getSubscribersByFacebookId(String facebookId);

    @Select("SELECT * FROM gameplay.s_subscriber WHERE phone = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberByPhone(String phone);

    @Select(
        "SELECT s.* " +
        "  FROM gameplay.s_subscriber s, gameplay.s_subscriber_session ss " +
        " WHERE ss.session_key = #{0} AND ss.subscriber_id = s.subscriber_id"
    )
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberBySessionKey(String sessionKey);

    @Select("SELECT nickname, context_id from gameplay.s_subscriber")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.nicknameContextMap")
    List<NicknameContext> getAllNicknames();

    @Select("SELECT * FROM gameplay.s_identity_fb WHERE subscriber_id = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.facebookIdentityInfoMap")
    List<FacebookIdentityInfo> getFacebookIdentityInfo(long subscriberId);

    @Select("SELECT * FROM gameplay.s_identity_fb WHERE subscriber_id = #{0} and facebook_app_id = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.facebookIdentityInfoMap")
    FacebookIdentityInfo getFacebookIdentityInfoForFacebookApp(long subscriberId, String facebookAppId);

    @Select("SELECT subscriber_id from gameplay.s_identity_fb WHERE facebook_id = #{facebookId} AND context_id = #{contextId}")
    Long getSubscriberIdFromFacebookId(@Param("facebookId") String facebookId, @Param("contextId") int contextId);

    @Delete("DELETE FROM gameplay.s_identity_fb WHERE facebook_id = #{facebookId} AND context_id = #{contextId}")
    void deleteFacebookMapping(@Param("facebookId") String facebookId, @Param("contextId")int contextId);

    @Delete("UPDATE gameplay.s_identity_fb set facebook_id = #{facebookId} WHERE subscriber_id = #{subscriberId}")
    void updateFacebookMappingForSubscriber(@Param("subscriberId") long subscriberId, @Param("facebookId") String facebookId);

    @Delete("DELETE FROM gameplay.s_identity_fb WHERE facebook_id = #{facebookId}")
    void deleteAllFacebookMappingsForFacebookId(@Param("facebookId") String facebookId);

    @Insert("INSERT INTO gameplay.s_identity_fb (subscriber_id, facebook_id, facebook_app_id, context_id) VALUES (#{subscriberId}, #{facebookId}, #{facebookAppId}, #{contextId})")
    @Options(useGeneratedKeys=false)
    int addIdentityMappingFacebook(@Param("subscriberId") long subscriberId, @Param("facebookId")String facebookId, @Param("facebookAppId") String facebookAppId, @Param("contextId")int contextId);

    @Select("SELECT * FROM gameplay.s_identity_fb")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.facebookIdentityInfoMap")
    List<FacebookIdentityInfo> getFacebookSubscriberIds();

    @Select("SELECT subscriber_id FROM gameplay.s_identity_realmadrid WHERE realmadrid_id = #{0}")
    Integer getSubscriberIdFromRealMadridId(String realMadridId);

    @Insert("INSERT INTO gameplay.s_identity_realmadrid (subscriber_id, realmadrid_id) VALUES (#{0}, #{1})")
    void addIdentityMappingRealMadrid(int subscriberId, String realMadridId);

    @Select("SELECT * FROM gameplay.s_subscriber WHERE subscriber_id = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberById(long subscriberId);

    @Select("SELECT s.* FROM gameplay.s_subscriber s WHERE s.email_sha256_hash = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberByPrimaryIdHash(String primaryIdHash);

    @Select("SELECT * from gameplay.s_subscriber WHERE primary_identifier = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberByPrimaryId(String primaryId);

//    @Select("select * from gameplay.s_subscriber where FIND_IN_SET( subscriber_id, #{0}) <> 0")
//    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
//    List<Subscriber> getSubscribers(String subscriberIdsSet);

    @Select("SELECT s.* FROM gameplay.s_subscriber s WHERE s.context_id = #{0} AND s.nickname = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    Subscriber getSubscriberByNickname(int contextId, String nickname);

    @Select("SELECT * FROM gameplay.s_subscriber_session WHERE subscriber_id = #{0} AND device_id = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberSessionMap")
    SubscriberSession getSubscriberSession(long subscriberId, String deviceId);

    @Select("SELECT * FROM gameplay.s_subscriber_session WHERE subscriber_id = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberSessionMap")
    List<SubscriberSession> getSubscriberSessions(long subscriberId);

    @Select("SELECT subscriber_id FROM gameplay.s_subscriber_session WHERE device_id = #{0}")
    List<Long> getSubscriberIdsUsingAnOldDeviceId(String deviceId);

    @Delete("DELETE FROM gameplay.s_subscriber_session WHERE device_id = #{0} AND subscriber_id = #{1}")
    void removeDeviceSessionsForThisDeviceAndThisSubscriber(String deviceId, long subscriberId);

    @Update("UPDATE gameplay.s_subscriber SET mint_parent_subscriber_id = #{1} WHERE subscriber_id = #{0}")
    void setSubscriberMintParentId(long subscriberId, long mintParentId);

    @Update("UPDATE gameplay.s_subscriber SET nickname = #{1}, nickname_set = 1 WHERE subscriber_id = #{0}")
    void setSubscriberNickname(long subscriberId, String nickname);

    @Update("UPDATE gameplay.s_subscriber SET email_verified=1 WHERE subscriber_id = #{0} AND email = #{1}")
    void setSubscriberEmailVerified(long subscriberId, String email);

    @Update("UPDATE gameplay.s_subscriber SET email_verified=0 WHERE subscriber_id = #{0}")
    void setSubscriberEmailNotVerified(long subscriberId);

    @Update(
        "UPDATE gameplay.s_subscriber SET firstname = #{firstname}, lastname = #{lastname}, email = #{email}, primary_identifier = #{primaryId}, email_sha256_hash = #{emailSha256Hash}, " +
        "     email_hash_prefix = #{emailHashPrefix}, photo_url = #{photoUrl}, photo_url_small = #{photoUrlSmall}, photo_url_large = #{photoUrlLarge}, " +
        "     role = #{role}, admin_role = #{adminRole}, phone = #{phone}, eula_flag = #{eulaFlag}, is_adult_flag = #{adultFlag}, " +
        "     language_code = #{languageCode}, currency_code = #{currencyCode}, from_country_code = #{fromCountryCode}, ship_country_code = #{shipCountryCode}, " +
        "     ring1_subscriber_id = #{ring1SubscriberId}, ring2_subscriber_id = #{ring2SubscriberId}, ring3_subscriber_id = #{ring3SubscriberId}, ring4_subscriber_id = #{ring4SubscriberId}, " +
        "     date_of_birth = #{dateOfBirth}, update_date = NOW() " +
        "WHERE subscriber_id = #{subscriberId}"
    )
    void updateSubscriber(Subscriber subscriber);

    @Update("UPDATE gameplay.s_subscriber SET passwd = #{1}, passwd_set = 1, change_password = 0, update_date = NOW() WHERE subscriber_id = #{0}")
    void updateSubscriberPassword(long subscriberId, String encryptedPassword);

    @Update(
        "UPDATE gameplay.s_subscriber_session " +
        "   SET app_id = #{2}, app_version = #{3} " +
        " WHERE subscriber_id = #{0} AND device_id = #{1}"
    )
    void updateSessionAppInfo(int subscriberId, String deviceId, String applicationId, String applicationVersion);

    @Select(
        "select distinct(s.subscriber_id) " +
        "  from gameplay.s_subscriber s, gameplay.s_subscriber_session ss " +
        " where s.subscriber_id = ss.subscriber_id and s.active_flag = 1 and device_model is not null"
    )
    List<Long> getActiveSubscriberIdsWithDevice();

    @Select(
        "select distinct(s.subscriber_id), s.language_code " +
        "  from gameplay.s_subscriber s, gameplay.s_subscriber_session ss " +
        " where s.subscriber_id = ss.subscriber_id and s.active_flag = 1 and device_model is not null"
    )
    List<SubscriberIdAndLanguageCode> getActiveSubscriberIdAndLanguageCodesWithDevice();

    @Select(
        "select count(*) " +
        "  from gameplay.s_subscriber_session " +
        " where subscriber_id = #{0} " +
        "   and device_model is not null"
    )
    int getNumberOfActiveDevicesForSubscriber(long subscriberId);

    @Select("SELECT max(subscriber_id) FROM gameplay.s_subscriber")
    int getLargestSubscriberId();

    @Select("SELECT * FROM gameplay.s_subscriber s WHERE s.email LIKE #{0} OR s.nickname LIKE #{0} ORDER BY s.nickname ASC LIMIT #{1},#{2}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> searchSubscribersByEmailOrNickname(String searchTerm, int startIndex, int size);

    @Select("SELECT * FROM gameplay.s_subscriber s WHERE s.email LIKE #{0} AND context_id = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> searchSubsByEmail(String partialEmail, int contextId);

    @Update("UPDATE gameplay.s_subscriber SET language_code = #{1} WHERE subscriber_id = #{0}")
    void setSubscriberLanguageCode(long subscriberId, String languageCode);

    @Select("select `nickname` from gameplay.s_invalid_nicknames")
    List<String> getInvalidNicknames();

    @Select (
        " select count(*) " +
        " from gameplay.s_subscriber s " +
        "where s.subscriber_id > 6 and s.active_flag = 1 " +
        "  and s.email <> 'NYI' " +
        "  and s.email not like '%shoutgp.com%' " +
        "  and s.create_date <= #{0}"
     )
     long getTotalSubscriberCountAsOfDate(Date date);

    @Select(
        "select s.`nickname` " +
        "  from gameplay.s_subscriber s " +
        " where s.create_date >= #{0} and s.create_date <= #{1}"
    )
    List<String> getUsernamesInDateRange(Date startDate, Date stopDate);

    @Update("UPDATE gameplay.s_subscriber_address SET current_flag = 0 WHERE subscriber_id = #{0} AND addr_type = #{1}")
    void inactivateSubscriberAddressesOfType(long subscriberId, SubscriberAddress.ADDRESS_TYPE type);

    @Insert(
        "INSERT INTO gameplay.s_subscriber_address" +
        "     (subscriber_id, addr_type, addr1, addr2, city, state, zip, country_code, current_flag, create_date, update_date)" +
        "VALUES (" +
        "     #{subscriberId}, #{type}," +
        "     #{addr1}, #{addr2}," +
        "     #{city}, #{state}, #{zip}, #{countryCode}, " +
        "     #{current}, " +
        "     NOW(), NOW()" +
        ")"
    )
    @Options(useGeneratedKeys=true, keyProperty="addressId", keyColumn="address_id")
    void addAddress(SubscriberAddress address);

    @Select("SELECT * FROM gameplay.s_subscriber_address WHERE subscriber_id = #{0} AND current_flag = 1")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberAddressMap")
    List<SubscriberAddress> getSubscriberAddresses(long subscriberId);

    @Select("select subscriber_id from gameplay.s_subscriber where context_id = #{0}")
    List<Long> getAllSubscribersForContext(int contextId);

    @Select("SELECT subscriber_id FROM gameplay.s_subscriber WHERE `role` = #{0}")
    List<Long> getSubscriberIdsForRole(ROLE role);

    @Update("UPDATE gameplay.s_subscriber SET facebook_user_flag = #{1}, photo_url = #{2} WHERE subscriber_id = #{0}")
    void setSubscriberFacebookInfo(long subscriberId, boolean isFacebookUser, String photoUrl);

    @Update("UPDATE gameplay.s_subscriber SET phone_verified=1 WHERE subscriber_id = #{0}")
    void markPhoneAsVerified(long subscriberId);

    @Update("UPDATE gameplay.s_subscriber SET phone_verified=0, phone=#{1} WHERE subscriber_id = #{0}")
    void updatePhone(long subscriberId, String phone);

    @Select("select nickname from gameplay.s_subscriber where nickname like #{0} order by nickname")
    List<String> findPartialNicknameMatches(String partialNickname);

    /// FOREIGN HOST SUBSCRIBER support ///

    @Select(" SELECT s.* "
            + " FROM gameplay.s_subscriber s "
            + " JOIN gameplay.s_identity_foreign_host fh ON s.subscriber_id = fh.subscriber_id"
            + "WHERE fh.foreign_host_subscriber_id = #{0} ")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> getSubscribersByForeignHostId(String foreignHostSubscriberId);

    @Select(" SELECT * "
            + " FROM gameplay.s_identity_foreign_host "
            + "WHERE subscriber_id = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.foreignHostIdentityInfoMap")
    List<ForeignHostIdentityInfo> getForeignHostIdentityInfoList(long subscriberId);

    @Select(" SELECT * "
            + " FROM gameplay.s_identity_foreign_host "
            + "WHERE subscriber_id = #{0} and foreign_host_app_id = #{1}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.foreignHostIdentityInfoMap")
    ForeignHostIdentityInfo getForeignHostIdentityInfoForForeignHostApp(long subscriberId, String foreignHostAppId);

    @Select(" SELECT subscriber_id "
            + " FROM gameplay.s_identity_foreign_host "
            + "WHERE foreign_host_subscriber_id = #{0} "
            + "  AND context_id = #{1}")
    Long getSubscriberIdFromForeignHostId(String foreignHostSubscriberId, int contextId);

    @Delete(" DELETE "
            + " FROM gameplay.s_identity_foreign_host "
            + "WHERE foreign_host_subscriber_id = #{0} "
            + "  AND context_id = #{1}")
    void deleteForeignHostMapping(String foreignHostSubscriberId, int contextId);

    @Delete(" UPDATE gameplay.s_identity_foreign_host "
            + "  SET foreign_host_subscriber_id = #{0} "
            + "WHERE subscriber_id = #{1}")
    void updateForeignHostMappingForSubscriber(long subscriberId, String foreignHostSubscriberId);

    @Delete(" DELETE "
            + " FROM gameplay.s_identity_foreign_host "
            + "WHERE ForeignHost_id = #{0}")
    void deleteAllForeignHostMappingsForForeignHostId(String foreignHostSubscriberId);

    @Insert("  INSERT IGNORE "
            + "  INTO gameplay.s_identity_foreign_host "
            + "  (subscriber_id, foreign_host_subscriber_id, foreign_host_app_id, context_id) "
            + "VALUES "
            + "  (#{0}, #{1}, #{2}, #{3})")
    @Options(useGeneratedKeys=false)
    int addIdentityMappingForeignHost(long subscriberId, String foreignHostSubscriberId, String foreignHostAppId, int contextId);

    @Select("SELECT * "
            + "FROM gameplay.s_identity_foreign_host")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.foreignHostIdentityInfoMap")
    List<ForeignHostIdentityInfo> getForeignHostSubscriberIds();

    @Insert(
        "INSERT INTO gameplay.s_subscriber_email (subscriber_id, email, email_type, verified, create_date, verified_date) " +
        "VALUES (#{subscriberId}, #{email}, #{emailType}, #{verified}, NOW(), #{verifiedDate})"
    )
    void addSubscriberEmail(SubscriberEmail subscriberEmail);

    @Update("UPDATE gameplay.s_subscriber_email SET verified = 1, verified_date = NOW() WHERE subscriber_id = #{0} AND email = #{1}")
    void verifySubscriberEmail(long subscriberId, String email);

    @Select("SELECT * FROM gameplay.s_subscriber_email WHERE subscriber_id = #{0}")
    List<SubscriberEmail> getSubscriberEmails(long subscriberId);

    @Select("SELECT COUNT(*) FROM gameplay.s_subscriber_role WHERE subscriber_id = #{0} AND FIND_IN_SET(role, #{1}) <> 0")
    boolean hasRole(long subscriberId, String validRolesAsCommaDelimitedList);

    @Select("SELECT COUNT(*) FROM gameplay.s_subscriber_role WHERE subscriber_id = #{0} AND `role` = 'SUPERUSER'")
    boolean isSuperuser(int subscriberId);

    @Insert("INSERT IGNORE INTO gameplay.s_subscriber_role (subscriber_id, `role`, create_date) VALUES (#{0}, #{1}, NOW())")
    void addRole(long subscriberId, String role);

    @Delete("DELETE FROM gameplay.s_subscriber_role WHERE subscriber_id = #{0} AND `role` = #{1}")
    void removeRole(long subscriberId, String role);

    @Select("SELECT `role` FROM gameplay.s_subscriber_role WHERE subscriber_id = #{0}")
    List<String> getSubscriberRoles(long subscriberId);

    @Select("SELECT subscriber_id FROM gameplay.s_subscriber_role WHERE `role` = #{0}")
    List<Long> getSubscriberIdsWithRole(String role);

    @Select("SELECT * FROM gameplay.s_subscriber WHERE mint_parent_subscriber_id = #{0}")
    @ResultMap(value="com.meinc.identity.dao.IXmlSubscriberDaoMapper.subscriberMap")
    List<Subscriber> getMintChildren(long subscriberId);
}