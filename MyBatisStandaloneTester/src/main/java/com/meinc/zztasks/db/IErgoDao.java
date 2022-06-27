package com.meinc.zztasks.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.meinc.zztasks.domain.ImportId;
import com.meinc.zztasks.domain.ImportStatus;
import com.meinc.zztasks.domain.Note;
import com.meinc.zztasks.domain.Role;
import com.meinc.zztasks.domain.Tag;
import com.meinc.zztasks.domain.Task;

import tv.shout.util.Tuple;

public interface IErgoDao
extends ServerRepositoryIntf
{
    //
    // IMPORT
    //

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.task WHERE subscriber_id = #{0}")
    void nukeAllTasks(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.note WHERE subscriber_id = #{0}")
    void nukeAllNotes(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.role WHERE subscriber_id = #{0}")
    void nukeAllRoles(int subscriberId);

    @Select(
        "SELECT * " +
        "  FROM ergo.import_progress_map " +
        " WHERE transaction_uuid = #{0}"
    )
    @Results({
        @Result(property="origRoleUuid", column="orig_role_uuid"),
        @Result(property="newRoleUuid", column="new_role_uuid"),
        @Result(property="newRoleId", column="new_role_id"),
        @Result(property="noteUuid", column="note_uuid"),
        @Result(property="taskUuid", column="task_uuid")
    })
    List<ImportId> getImportedIds(String transactionUuid);

    @Update("update ergo.`import` SET start_date = NOW(), `status`='INPROGRESS', total_items = #{1} WHERE transaction_uuid = #{0}")
    void beginImport(String transactionUuid, int totalItems);

    @Insert(
        "INSERT INTO ergo.import_progress_map (transaction_uuid, orig_role_uuid, new_role_uuid, new_role_id, note_uuid, task_uuid) " +
        "VALUES (#{0}, #{1}, #{2}, #{3}, #{4}, #{5})")
    void addImportId(String transactionUuid, String origRoleUuid, String newRoleUuid, int newRoleId, String noteUuid, String taskUuid);

    @Update("UPDATE ergo.`import` SET complete_items = #{1} WHERE transaction_uuid = #{0}")
    void updateImportProgress(String transactionUuid, int completeItems);

    @Update("UPDATE ergo.`import` SET end_date = NOW(), `status` = #{1}, message = #{2}, failed_op_ids = #{3}, successful_op_ids = #{4} WHERE transaction_uuid = #{0}")
    void setImportEndStatus(String transactionUuid, String status, String message, String failedOpIds, String successfulOpIds);

    @Insert(
        "INSERT INTO ergo.`import` (transaction_uuid, subscriber_id, start_date, `status`, total_items, from_provider_uuid, to_provider_uuid) " +
        "VALUES (#{0}, #{1}, NOW(), 'NOTSTARTED', 0, #{2}, #{3})"
    )
    void setImportReceivedButNotStarted(String transactionUuid, int subscriberId, String fromProviderUuid, String toProviderUuid);

    @Select("SELECT * FROM ergo.`import` WHERE transaction_uuid = #{0}")
    @Results({
        @Result(property="status", column="status"),
        @Result(property="completeItems", column="complete_items"),
        @Result(property="totalItems", column="total_items"),
        @Result(property="message", column="message"),
        @Result(property="failedOpIdsRaw", column="failed_op_ids"),
        @Result(property="successfulOpIdsRaw", column="successful_op_ids")
    })
    ImportStatus getImportStatus(String transactionUuid);

    //
    // ROLE
    //

    @Insert(
        "INSERT INTO ergo.role (role_uuid, subscriber_id, name, `color`, `icon`, `order`, create_date, update_date) " +
        "VALUES (#{role.uuid}, #{0}, #{role.name}, #{role.color}, #{role.icon}, #{role.order}, #{role.createDate}, #{role.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="role.id")
    void addRole(final int subscriberId, @Param("role") final Role role);

    @Select("SELECT 'ERGO' as provider, role.* FROM ergo.role WHERE role_uuid = #{1} and subscriber_id = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="name", column="name"),
        @Result(property="color", column="color"),
        @Result(property="icon", column="icon"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="order", column="order")
    })
    Role getRole(int subscriberId, String roleUuid);

    @Select(
        "SELECT 'ERGO' as provider, role.* " +
        "  FROM ergo.role " +
        " WHERE subscriber_id = #{0} " +
        "   AND update_date >= #{1} " +
        " ORDER BY `order`"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="name", column="name"),
        @Result(property="color", column="color"),
        @Result(property="icon", column="icon"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="order", column="order")
    })
    List<Role> getRoles(int subscriberId, Date lastSyncTime);

    @Update(
        "UPDATE ergo.role " +
        "   SET name = #{name}, `color` = #{color}, `icon` = #{icon}, `order` = #{order}, update_date = #{lastUpdate} " +
        " WHERE role_id = #{id}"
    )
    void updateRole(Role role);

    @Update("UPDATE ergo.role SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE role_id = #{id}")
    void deleteRole(Role role);

    @Select(
        "SELECT 'ERGO' as provider, note.* " +
        "  FROM ergo.note " +
        " WHERE role_id = #{0}"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date")
    })
    List<Note> getNotesForRole(int roleId);

    @Select(
        "SELECT 'ERGO' as provider, task.* " +
        "  FROM ergo.task " +
        " WHERE role_id = #{0} "
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="description"),
        @Result(property="dueDate", column="due_date"),
        @Result(property="priority", column="priority"),
        @Result(property="reminderMinBefore", column="reminder_minutes_before"),
        @Result(property="note", column="note"),
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getTasksForRole(int roleId);

    //
    // NOTE
    //

    @Insert(
        "INSERT INTO ergo.note (note_uuid, subscriber_id, note, private_flag, role_id, role_uuid, `order`, create_date, update_date) " +
        "VALUES (#{note.uuid}, #{0}, #{note.note}, #{note.privateFlag}, #{note.roleId}, #{note.roleUuid}, #{note.order}, #{note.createDate}, #{note.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="note.id")
    void addNote(final int subscriberId, @Param("note") final Note note);

    @Select("SELECT 'ERGO' as provider, note.* FROM ergo.note WHERE note_uuid = #{1} and subscriber_id = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date")
    })
    Note getNote(int subscriberId, String noteUuid);

    @Select(
        "SELECT 'ERGO' as provider, note.* " +
        "  FROM ergo.note " +
        " WHERE subscriber_id = #{0} " +
        "   AND update_date >= #{1} " +
        " ORDER BY `order`"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date")
    })
    List<Note> getNotes(int subscriberId, Date lastSyncTime);

    @Update(
        "UPDATE ergo.note " +
        "  SET note = #{note}, private_flag = #{privateFlag}, role_id = #{roleId}, role_uuid = #{roleUuid}, `order` = #{order}, update_date = #{lastUpdate} " +
        "WHERE note_id = #{id}"
    )
    void updateNote(Note note);

    @Update("UPDATE ergo.note SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE note_id = #{id}")
    void deleteNote(Note note);

    @Select("select t.tag_uuid from ergo.tag t, ergo.note_tag gnt where gnt.note_id = #{0} and gnt.tag_id = t.tag_id")
    List<String> getTagIdsForNote(int noteId);

    @Delete("DELETE FROM ergo.note_tag WHERE note_id = #{0}")
    void removeAllTagsFromNote(int noteId);

    //
    // TASK
    //

    @Insert(
        "INSERT INTO ergo.task (" +
        "   task_uuid, subscriber_id, description, due_date, " +
        "   `priority`, role_id, role_uuid, reminder_minutes_before, " +
        "   reminder, private_flag, note, `status`, " +
        "   completed_date, recurring_start_date, recurring_rrule, recurring_regenerative_flag, " +
        "   `order`, priority_order, role_order, timezone, " +
        "   create_date, update_date) VALUES (" +
        "   #{task.uuid}, #{0}, #{task.description}, #{task.dueDate}, " +
        "   #{task.priority}, #{task.roleId}, #{task.roleUuid}, #{task.reminderMinBefore}, " +
        "   #{task.reminder}, #{task.privateFlag}, #{task.note}, #{task.status}, " +
        "   #{task.completedDate}, #{2}, #{3}, #{4}, " +
        "   #{task.order}, #{task.priorityOrder}, #{task.roleOrder}, #{task.timezoneId}, " +
        "   #{task.createDate}, #{task.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="task.id")
    void addTask(final int subscriberId, @Param("task") final Task task, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative);

    @Select("SELECT 'ERGO' as provider, task.* FROM ergo.task WHERE task_uuid = #{1} and subscriber_id = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="description"),
        @Result(property="dueDate", column="due_date"),
        @Result(property="priority", column="priority"),
        @Result(property="reminderMinBefore", column="reminder_minutes_before"),
        @Result(property="note", column="note"),
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    DbTask getTask(int subscriberId, String taskUuid);

    @Select(
        "SELECT 'ERGO' as provider, task.* " +
        "  FROM ergo.task " +
        " WHERE subscriber_id = #{0} " +
        "   AND update_date >= #{1} " +
        " ORDER BY `order`"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="description"),
        @Result(property="dueDate", column="due_date"),
        @Result(property="priority", column="priority"),
        @Result(property="reminderMinBefore", column="reminder_minutes_before"),
        @Result(property="note", column="note"),
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getTasks(int subscriberId, Date lastSyncTime);

    @Update(
        "UPDATE ergo.task " +
        "  SET  description = #{task.description}, due_date = #{task.dueDate}, `priority` = #{task.priority}, role_id = #{1}, role_uuid = #{task.roleUuid}, " +
        "       reminder_minutes_before = #{task.reminderMinBefore}, reminder = #{task.reminder}, private_flag = #{task.privateFlag}, note = #{task.note}, `status` = #{task.status}, " +
        "       completed_date = #{task.completedDate}, recurring_start_date = #{2}, recurring_rrule = #{3}, recurring_regenerative_flag = #{4}, " +
        "       next_task_created = #{task.nextTaskCreated}, `order` = #{task.order}, priority_order = #{task.priorityOrder}, role_order = #{task.roleOrder}, " +
        "       update_date = #{task.lastUpdate}, reminded = #{task.reminded}, timezone = #{task.timezoneId} " +
        "WHERE task_id = #{task.id}"
    )
    void updateTask(@Param("task") Task task, Integer roleId, Date rfc2445StartDate, String iCalString, Boolean regenerative);

    @Update("UPDATE ergo.task SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE task_id = #{id}")
    void deleteTask(Task task);

    //uses reminder_minutes_before
    @Deprecated
    @Select(
        "SELECT 'ERGO' as provider, task.* " +
        "  FROM ergo.task " +
        " WHERE delete_date IS NULL " +
        "  AND status <> 'COMPLETE' " +
        "  AND due_date IS NOT NULL " +
        "  AND reminder_minutes_before IS NOT NULL " +
        "  AND reminder_minutes_before <> 0 " +
        "  AND reminded = 0 " +
        "  AND NOW() >= DATE_SUB(due_date, INTERVAL reminder_minutes_before MINUTE)"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="description"),
        @Result(property="dueDate", column="due_date"),
        @Result(property="priority", column="priority"),
        @Result(property="reminderMinBefore", column="reminder_minutes_before"),
        @Result(property="note", column="note"),
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks();

    //uses reminder date field
    @Select(
        "SELECT 'ERGO' as provider, task.* " +
        "  FROM ergo.task " +
        " WHERE delete_date IS NULL " +
        "  AND status <> 'COMPLETE' " +
        "  AND reminder IS NOT NULL " +
        "  AND reminded = 0 " +
        "  AND NOW() >= reminder"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="description"),
        @Result(property="dueDate", column="due_date"),
        @Result(property="priority", column="priority"),
        @Result(property="reminderMinBefore", column="reminder_minutes_before"),
        @Result(property="note", column="note"),
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks2();

    @Update("UPDATE ergo.task SET reminded = 1 WHERE task_id = #{0}")
    void setTaskReminded(int taskId);

    //
    // TAG
    //

    @Insert("INSERT INTO ergo.tag (tag_uuid, `name`, `description`, create_date, update_date) VALUES (#{uuid}, #{name}, #{description}, #{createDate}, #{lastUpdate})")
    @Options(useGeneratedKeys=true, keyProperty="id")
    void addTag(final Tag tag);

    @Select("SELECT * from ergo.tag WHERE tag_uuid = #{0}")
    @Results({
        @Result(property="id", column="tag_id"),
        @Result(property="uuid", column="tag_uuid"),
        @Result(property="name", column="name"),
        @Result(property="description", column="description"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    Tag getTag(String tagUuid);

    @Select("SELECT t.* FROM ergo.tag t, ergo.subscriber_tag st WHERE t.tag_id = st.tag_id AND st.subscriber_id = #{0}")
    @Results({
        @Result(property="id", column="tag_id"),
        @Result(property="uuid", column="tag_uuid"),
        @Result(property="name", column="name"),
        @Result(property="description", column="description"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    List<Tag> getTags(int subscriberId);

    @Update("UPDATE ergo.tag SET name = #{name}, description = #{description}, update_date = #{lastUpdate} WHERE tag_id = #{id}")
    void updateTag(Tag tag);

    @Delete("DELETE FROM ergo.tag WHERE tag_id = #{0}")
    void deleteTag(int tagId);

    @Insert("INSERT INTO ergo.subscriber_tag VALUES (#{0}, #{1})")
    void addTagToSubscriber(int tagId, int subscriberId);

    @Delete("DELETE FROM ergo.subscriber_tag WHERE tag_id = #{0}")
    void removeTagSubscriberJoin(int tagId);

    @Insert(
        "insert into ergo.note_tag (tag_id, note_id) " +
        "  select tag_id, #{0} " +
        "    from ergo.tag " +
        "    where tag_uuid = #{1}"
    )
    void addTagToNote(int noteId, String tagUuid);

    @Delete("DELETE FROM ergo.note_tag WHERE tag_id = #{0}")
    void removeNoteTagJoins(int tagId);

    //
    // NON EXPIRING SUBSCRIBERS
    //

    static class ArbitrarySqlProvider
    {
        public String allNonExpiringPremiumSubscriberIdsSql(String sql)
        {
            return sql;
        }
    }

    @SelectProvider(type=ArbitrarySqlProvider.class, method="allNonExpiringPremiumSubscriberIdsSql")
    List<Integer> getAllNonExpiringPremiumSubscriberIds(String sql);

    @Delete("delete from store.subscriber_entitlement where subscriber_id = #{0} and subscriber_entitlement_id > 0")
    void deleteSubscriberPremiumEntitlements(int subscriberId);

    @Delete("delete from store.receipt where subscriber_id = #{0}")
    void deleteSubscriberReceipts(int subscriberId);

    @Insert(
        "insert into store.receipt (uuid, type, item_uuid, subscriber_id, payload, expiration_date, skip_verify, created_date, updated_date) " +
        "values (#{0}, 'INTERNAL', #{1}, #{2}, #{3}, #{4}, 0, NOW(), NOW())"
    )
    void addSubscriberPremiumReceipt(String uuid1, String premiumItemUuid, int subscriberId, String receipt, Date newExpireDate);

    @Select("SELECT receipt_id from store.receipt where subscriber_id = #{0} order by receipt_id desc limit 1")
    int getSubscriberMostRecentReceiptId(int subscriberId);

    @Insert(
        "INSERT INTO `store`.`subscriber_entitlement` (`uuid`, `subscriber_id`, `entitlement_id`, `receipt_id`) " +
        "SELECT UUID(), #{0}, ie.entitlement_id, #{1} " +
        "FROM `store`.`entitlement` e, `store`.`item_entitlement` ie " +
        "WHERE ie.item_id = #{2} AND ie.entitlement_id = e.entitlement_id"
    )
    void addSubscriberPremiumEntitlements(int subscriberId, int receiptId, int premiumItemId);

    @Update("update ergo.subscriber set state = 'PREMIUM', state_expiration_date = #{0} where subscriber_id = #{1}")
    void updateSubscriberPremiumState(Date newExpireDate, int subscriberId);

    //
    // OTHER
    //

    @Select("SELECT pref_name, pref_value FROM ergo.prefs WHERE subscriber_id = #{0}")
    @Results({
        @Result(property="key", column="pref_name"),
        @Result(property="val", column="pref_value")
    })
    List<Tuple<String>> getPrefs(int subscriberId);

    @Insert(
        "INSERT INTO ergo.prefs (subscriber_id, pref_name, pref_value) VALUES (#{0}, #{1}, #{2}) " +
        "ON DUPLICATE KEY UPDATE pref_value = #{2}"
    )
    void setPref(int subscriberId, String prefName, String prefValue);

    @Insert("INSERT INTO `ergo`.`affiliate_access` (affiliate_id, access_count) VALUES (#{0}, 1) ON DUPLICATE KEY UPDATE access_count = access_count+1")
    void incrementAffiliateAccess(String affiliateId);

    @Select("SELECT NOW()")
    Date getDbNow();

}
