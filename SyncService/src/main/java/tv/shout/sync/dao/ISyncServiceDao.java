package tv.shout.sync.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import tv.shout.sync.domain.SyncMessage;

public interface ISyncServiceDao
{
    @Insert(
        "INSERT INTO sync.message " +
        "   (id, subscriber_id, message_type, contextual_id, engine_key, payload, create_date) " +
        "VALUES (#{id}, #{subscriberId}, #{messageType}, #{contextualId}, #{engineKey}, #{payload}, NOW(6))"
    )
    void insertSyncMessage(SyncMessage message);

    @Select(
        "SELECT * " +
        "  FROM sync.message " +
        " WHERE contextual_id = #{0} AND subscriber_id = #{1} AND create_date >= #{2}"
    )
    List<SyncMessage> getSyncMessagesWithContext(String contextualId, long subscriberId, Date since);

    @Select(
        "SELECT * " +
        "  FROM sync.message " +
        " WHERE subscriber_id = #{0} AND create_date >= #{1}"
    )
    List<SyncMessage> getSyncMessages(long subscriberId, Date since);
}
