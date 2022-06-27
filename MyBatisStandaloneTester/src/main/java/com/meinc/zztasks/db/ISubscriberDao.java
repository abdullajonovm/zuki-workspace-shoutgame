package com.meinc.zztasks.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.zztasks.domain.ApplicationInformation;
import com.meinc.zztasks.domain.DeviceInformation;
import com.meinc.zztasks.domain.Subscriber;

public interface ISubscriberDao
{
    @Insert(
        "INSERT INTO ergo.subscriber " +
        "(subscriber_uuid, email, `state`, state_expiration_date, fb_id, timezone, affiliate, create_date, update_date) " +
        "VALUES (#{uuid}, #{email}, #{state}, #{stateExpirationDate}, #{fbId}, #{timezone}, #{affiliate}, #{createDate}, #{lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="id")
    void addSubscriber(final Subscriber s);

    @Select(
        "SELECT " +
        "       s.subscriber_id, s.subscriber_uuid, s.create_date, s.update_date, s.delete_date, " +
        "       s.email, s.state, s.state_expiration_date, s.fb_id, s.timezone, s.feature_tester, s.affiliate " +
        "  FROM ergo.subscriber s, ergo.subscriber_device_app sda " +
        " WHERE sda.session_key = #{0} " +
        "   AND sda.subscriber_id = s.subscriber_id"
    )
    @Results({
        @Result(property="id", column="subscriber_id"),
        @Result(property="uuid", column="subscriber_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="email", column="email"),
        @Result(property="state", column="state"),
        @Result(property="stateExpirationDate", column="state_expiration_date"),
        @Result(property="fbId", column="fb_id"),
        @Result(property="timezone", column="timezone"),
        @Result(property="featureTester", column="feature_tester"),
        @Result(property="affiliate", column="affiliate")
    })
    Subscriber getSubscriberFromSession(String sessionKey);

    @Select(
        "SELECT * " +
        "  FROM ergo.subscriber " +
        " WHERE email = #{0}"
    )
    @Results({
        @Result(property="id", column="subscriber_id"),
        @Result(property="uuid", column="subscriber_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="email", column="email"),
        @Result(property="state", column="state"),
        @Result(property="stateExpirationDate", column="state_expiration_date"),
        @Result(property="fbId", column="fb_id"),
        @Result(property="timezone", column="timezone"),
        @Result(property="featureTester", column="feature_tester"),
        @Result(property="affiliate", column="affiliate")
    })
    Subscriber getSubscriberViaEmail(String email);

    @Select(
        "SELECT * " +
        "  FROM ergo.subscriber " +
        " WHERE fb_id = #{0}"
    )
    @Results({
        @Result(property="id", column="subscriber_id"),
        @Result(property="uuid", column="subscriber_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="email", column="email"),
        @Result(property="state", column="state"),
        @Result(property="stateExpirationDate", column="state_expiration_date"),
        @Result(property="fbId", column="fb_id"),
        @Result(property="timezone", column="timezone"),
        @Result(property="featureTester", column="feature_tester"),
        @Result(property="affiliate", column="affiliate")
    })
    Subscriber getSubscriberViaFbId(String fbId);

    @Select(
        "SELECT * " +
        "  FROM ergo.subscriber " +
        " WHERE subscriber_id = #{0}"
    )
    @Results({
        @Result(property="id", column="subscriber_id"),
        @Result(property="uuid", column="subscriber_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="email", column="email"),
        @Result(property="state", column="state"),
        @Result(property="stateExpirationDate", column="state_expiration_date"),
        @Result(property="fbId", column="fb_id"),
        @Result(property="timezone", column="timezone"),
        @Result(property="featureTester", column="feature_tester"),
        @Result(property="affiliate", column="affiliate")
    })
    Subscriber getSubscriber(int subscriberId);

    @Update(
        "UPDATE ergo.subscriber " +
        "   SET email = #{email}, state = #{state}, state_expiration_date = #{stateExpirationDate}, fb_id = #{fbId}, timezone = #{timezone}, update_date = #{lastUpdate} " +
        " WHERE subscriber_uuid = #{uuid}")
    void updateSubscriber(final Subscriber s);

    @Select("SELECT COUNT(*) from ergo.subscriber where email = #{0} FOR UPDATE")
    boolean isUsernameTaken_locking(String username);

    @Select("SELECT mission_statement FROM ergo.subscriber WHERE subscriber_id = #{0}")
    String getSubscriberMissionStatement(int subscriberId);

    @Update("UPDATE ergo.subscriber SET mission_statement = #{1} WHERE subscriber_id = #{0}")
    void setSubscriberMissionStatement(int subscriberId, String missionStatement);

    @Select("SELECT COUNT(*) from ergo.subscriber where email = #{0}")
    boolean isUsernameTaken(String username);

    @Insert(
        "INSERT INTO ergo.subscriber_device_app (" +
        "   subscriber_id, device_id, `session_key`, " +
        "   `model`, `name`, osname, ostype, `version`, " +
        "   application_id, application_version) " +
        "VALUES (" +
        "   #{0}, #{di.deviceId}, #{1}, " +
        "   #{di.model}, #{di.name}, #{di.osName}, #{di.osType}, #{di.version}, " +
        "   #{ai.applicationId}, #{ai.applicationVersion})"
    )
    void addSubscriberDevice(int subscriberId, String sessionKey, @Param("di") DeviceInformation di, @Param("ai") ApplicationInformation ai);

    @Select("SELECT * FROM ergo.subscriber_device_app WHERE subscriber_id = #{0} AND device_id = #{1}")
    @Results({
        @Result(property="deviceId", column="device_id")
    })
    DeviceInformation getSubscriberDevice(int subscriberId, String deviceId);

    @Update("UPDATE ergo.subscriber SET fb_id = #{1}, update_date = NOW() WHERE subscriber_id = #{0}")
    void setSubscriberFbId(final int subscriberId, String fbId);

    @Insert(
        "INSERT INTO ergo.subscriber_device_app (subscriber_id, device_id, `session_key`, `model`, `name`, osname, ostype, `version`, application_id, application_version) " +
        "VALUES (#{0}, #{di.deviceId}, #{1}, #{di.model}, #{di.name}, #{di.osName}, #{di.osType}, #{di.version}, #{ai.applicationId}, #{ai.applicationVersion}) " +
        "ON DUPLICATE KEY UPDATE " +
        "   `session_key` = #{1}, `model` = #{di.model}, `name` = #{di.name}, osname = #{di.osName}, ostype = #{di.osType}, `version` = #{di.version}, " +
        "   application_id = #{ai.applicationId}, application_version = #{ai.applicationVersion}"
    )
    void updateSubscriberDevice(int subscriberId, String sessionKey, @Param("di") DeviceInformation di, @Param("ai") ApplicationInformation ai);

    @Select(
        "SELECT * " +
        "  FROM ergo.subscriber s " +
        " WHERE state_expiration_date < NOW() "
    )
    @Results({
        @Result(property="id", column="subscriber_id"),
        @Result(property="uuid", column="subscriber_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="email", column="email"),
        @Result(property="state", column="state"),
        @Result(property="stateExpirationDate", column="state_expiration_date"),
        @Result(property="fbId", column="fb_id"),
        @Result(property="timezone", column="timezone"),
        @Result(property="featureTester", column="feature_tester"),
        @Result(property="affiliate", column="affiliate")
    })
    List<Subscriber> getSubscribersWithStatePastExpiration();

    @Select(
            "SELECT * " +
            "  FROM ergo.subscriber s " +
            " WHERE s.state = 'TRIAL' " +
            "   AND s.create_date <= DATE_SUB(NOW(), INTERVAL #{0} DAY) " +
            "   AND s.create_date > DATE_SUB(NOW(), INTERVAL #{1} DAY) "
        )
        @Results({
            @Result(property="id", column="subscriber_id"),
            @Result(property="uuid", column="subscriber_uuid"),
            @Result(property="createDate", column="create_date"),
            @Result(property="lastUpdate", column="update_date"),
            @Result(property="deleteDate", column="delete_date"),
            @Result(property="email", column="email"),
            @Result(property="state", column="state"),
            @Result(property="stateExpirationDate", column="state_expiration_date"),
            @Result(property="fbId", column="fb_id"),
            @Result(property="timezone", column="timezone"),
            @Result(property="featureTester", column="feature_tester"),
            @Result(property="affiliate", column="affiliate")
        })
        List<Subscriber> getTrialSubscribersCreatedFromToDaysAgo(int fromDaysAgo, int toDaysAgo);
}
