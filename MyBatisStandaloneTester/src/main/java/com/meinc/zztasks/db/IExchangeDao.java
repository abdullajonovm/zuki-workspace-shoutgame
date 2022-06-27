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
import org.apache.ibatis.annotations.Update;

import com.meinc.zztasks.domain.Note;
import com.meinc.zztasks.domain.Role;
import com.meinc.zztasks.domain.Task;

public interface IExchangeDao
extends ServerRepositoryIntf
{
    //
    // IMPORT
    //

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.exchange_task WHERE subscriber_id = #{0}")
    void nukeAllTasks(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.exchange_note WHERE subscriber_id = #{0}")
    void nukeAllNotes(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.exchange_role WHERE subscriber_id = #{0}")
    void nukeAllRoles(int subscriberId);

    //
    // ROLE
    //

    @Insert(
        "INSERT INTO ergo.exchange_role (role_uuid, server_id, subscriber_id, provider_uuid, `name`, `icon`, color, exchange_color, create_date, update_date) " +
        "VALUES (#{role.uuid}, #{role.serverId}, #{0}, #{role.providerUuid}, #{role.name}, #{role.icon}, #{role.color}, #{role.providerColor}, #{role.createDate}, #{role.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="role.id")
    void addRole(final int subscriberId, @Param("role") final Role role);

    @Select(
        "SELECT 'EXCHANGE' as provider, exchange_role.* " +
        "  FROM ergo.exchange_role " +
        " WHERE role_uuid = #{0}"
    )
    @Results({
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="providerType", column="provider"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="name", column="name"),
        @Result(property="icon", column="icon"),
        @Result(property="color", column="color"),
        @Result(property="providerColor", column="exchange_color"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    Role getRole(String roleUuid);

    @Select(
        "SELECT 'EXCHANGE' as provider, exchange_role.* " +
        "  FROM ergo.exchange_role " +
        " WHERE subscriber_id = #{0} AND provider_uuid = #{1} AND update_date >= #{2}"
    )
    @Results({
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="providerType", column="provider"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="name", column="name"),
        @Result(property="icon", column="icon"),
        @Result(property="color", column="color"),
        @Result(property="providerColor", column="exchange_color"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Role> getRoles(int subscriberId, String providerUuid, Date lastSyncTime);

    @Update(
        "UPDATE ergo.exchange_role " +
        "   SET `name` = #{name}, `icon` = #{icon}, color=#{color}, exchange_color=#{providerColor}, update_date = #{lastUpdate}, last_server_sync_time = null" +
        " WHERE role_id = #{id}"
    )
    void updateRole(Role role);

    @Update("UPDATE ergo.exchange_role SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE role_id = #{id}")
    void deleteRole(Role role);

    @Select("SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task WHERE role_id = #{0} AND delete_date is null")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getCurrentTasksForRole(int roleId);

    @Select(
        "SELECT 'EXCHANGE' as provider, note_id, note_uuid, server_id, subscriber_id, provider_uuid, " +
        "       role_id, role_uuid, `order`, private_note, private_flag, " +
        "       create_date, update_date, delete_date, last_server_sync_time " +
        "  FROM ergo.exchange_note " +
        " WHERE role_id = #{0} AND delete_date IS NULL"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Note> getCurrentNotesForRole(int roleId);

    @Select(
        "SELECT 'EXCHANGE' as provider, exchange_role.* " +
        "  FROM ergo.exchange_role " +
        " WHERE provider_uuid = #{0} AND delete_date >= #{1}"
    )
    @Results({
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="providerType", column="provider"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="name", column="name"),
        @Result(property="icon", column="icon"),
        @Result(property="color", column="color"),
        @Result(property="providerColor", column="exchange_color"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Role> getDeletedRoles(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.exchange_role SET last_server_sync_time = #{1} WHERE role_id = #{0}")
    void updateRoleLastSyncTime(int roleId, Date lastServerSyncTime);

    //
    // NOTE
    //

    @Insert(
        "INSERT INTO ergo.exchange_note (" +
        "   note_uuid, server_id, subscriber_id, provider_uuid, role_id, role_uuid, `order`, private_note, private_flag, create_date, update_date) " +
        "VALUES (#{note.uuid}, #{note.serverId}, #{0}, #{note.providerUuid}, #{2}, #{note.roleUuid}, #{note.order}, #{note.note}, #{note.privateFlag}, #{note.createDate}, #{note.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="note.id")
    void addNote(final int subscriberId, @Param("note") final Note note, Integer roleId);

    @Select(
        "SELECT 'EXCHANGE' as provider, note_id, note_uuid, server_id, subscriber_id, provider_uuid, " +
        "       role_id, role_uuid, `order`, private_note, private_flag, " +
        "       create_date, update_date, delete_date, last_server_sync_time " +
        "  FROM ergo.exchange_note " +
        " WHERE note_uuid = #{0}"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    Note getNote(String noteUuid);

    @Select(
        "SELECT 'EXCHANGE' as provider, note_id, note_uuid, server_id, subscriber_id, provider_uuid, " +
        "       role_id, role_uuid, `order`, private_note, private_flag, " +
        "       create_date, update_date, delete_date, last_server_sync_time " +
        "  FROM ergo.exchange_note " +
        " WHERE provider_uuid = #{0}"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Note> getNotes(String providerUuid);

    @Select("SELECT 'EXCHANGE' as provider, exchange_note.* FROM ergo.exchange_note WHERE subscriber_id = #{0} AND BINARY server_id = #{1}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    Note getNoteByServerId(int subscriberId, String serverId);

    @Update(
        "UPDATE ergo.exchange_note " +
        "   SET server_id = #{note.serverId}, role_id = #{1}, role_uuid = #{note.roleUuid}, `order` = #{note.order}, " +
        "       private_note = #{note.note}, private_flag = #{note.privateFlag}, update_date = #{note.lastUpdate}, last_server_sync_time = null " +
        " WHERE note_id = #{note.id}"
    )
    void updateNote(@Param("note") Note note, Integer roleId);

    @Update("UPDATE ergo.exchange_note SET update_date = #{lastUpdate}, delete_date = #{deleteDate} WHERE note_id = #{id}")
    void deleteNote(Note note);

    @Delete("DELETE from ergo.exchange_note WHERE note_uuid = #{0}")
    void deleteNoteForReal(String noteUuid);

    @Select(
        "SELECT note_uuid " +
        "  FROM ergo.exchange_note " +
        " WHERE server_id = #{0}"
    )
    String getNoteUuidFromServerId(String serverId);

    @Select("select t.tag_uuid from ergo.tag t, ergo.exchange_note_tag gnt where gnt.note_id = #{0} and gnt.tag_id = t.tag_id")
    List<String> getTagIdsForNote(int noteId);

    @Insert(
        "insert into ergo.exchange_note_tag (tag_id, note_id) " +
        "  select tag_id, #{0} " +
        "    from ergo.tag " +
        "    where tag_uuid = #{1}"
    )
    void addTagToNote(int noteId, String tagUuid);

    @Delete("DELETE FROM ergo.exchange_note_tag WHERE tag_id = #{0}")
    void removeNoteTagJoins(int tagId);

    @Delete("DELETE FROM ergo.exchange_note_tag WHERE note_id = #{0}")
    void removeAllTagsFromNote(int noteId);

    @Select(
        "SELECT 'EXCHANGE' as provider, note_id, note_uuid, server_id, subscriber_id, provider_uuid, " +
        "       role_id, role_uuid, `order`, private_note, private_flag, " +
        "       create_date, update_date, delete_date, last_server_sync_time " +
        "  FROM ergo.exchange_note " +
        " WHERE provider_uuid = #{0} AND delete_date >= #{1}"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Note> getDeletedNotes(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.exchange_note SET last_server_sync_time = #{1} WHERE note_id = #{0}")
    void updateNoteLastSyncTime(int noteId, Date lastServerSyncTime);

    //
    // TASK
    //

    @Insert(
        "INSERT INTO ergo.exchange_task (" +
        "   task_uuid, server_id, subscriber_id, provider_uuid, " +
        "   role_id, role_uuid, `order`, private_flag, " +
        "   private_desc, private_due_date, private_priority, private_reminder_minutes_before, " +
        "   reminder, reminded, private_note, private_status, " +
        "   completed_date, recurring_start_date, recurring_rrule, recurring_regenerative_flag, " +
        "   priority_order, role_order, timezone, create_date, update_date) " +
        "VALUES (" +
        "   #{task.uuid}, #{task.serverId}, #{0}, #{task.providerUuid}, " +
        "   #{2}, #{task.roleUuid}, #{task.order}, #{task.privateFlag}, " +
        "   #{task.description}, #{task.dueDate}, #{task.priority}, #{task.reminderMinBefore}, " +
        "   #{task.reminder}, #{task.reminded}, #{task.note}, #{task.status}, " +
        "   #{task.completedDate}, #{3}, #{4}, #{5}, " +
        "   #{task.priorityOrder}, #{task.roleOrder}, #{task.timezoneId}, #{task.createDate}, #{task.lastUpdate}" +
        ")"
    )
    @Options(useGeneratedKeys=true, keyProperty="task.id")
    void addTask(final int subscriberId, @Param("task") final Task task, Integer roleId, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative);

    @Select("SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task WHERE task_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    DbTask getTask(String taskUuid);

    @Select("SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task WHERE subscriber_id = #{0} AND BINARY server_id = #{1}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    DbTask getTaskByServerId(int subscriberId, String serverId);

    @Select("SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task WHERE provider_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getTasks(String providerUuid);

    @Update(
        "UPDATE ergo.exchange_task " +
        "   SET role_id = #{1}, server_id = #{task.serverId}, role_uuid = #{task.roleUuid}, `order` = #{task.order}, " +
        "       private_flag = #{task.privateFlag}, update_date = #{task.lastUpdate}, " +
        "       private_desc = #{task.description}, private_due_date = #{task.dueDate}, private_priority = #{task.priority}, " +
        "       private_reminder_minutes_before = #{2}, reminder = #{task.reminder}, private_note = #{3}, private_status = #{task.status}, completed_date = #{task.completedDate}, last_server_sync_time = null, " +
        "       recurring_start_date = #{4}, recurring_rrule = #{5}, recurring_regenerative_flag = #{6}, reminded = #{task.reminded}, priority_order = #{task.priorityOrder}, role_order = #{task.roleOrder}, " +
        "       timezone = #{task.timezoneId} " +
        " WHERE task_id = #{task.id}"
    )
    void updateTask(@Param("task") Task task, Integer roleId, Integer reminderMinutesBefore, String note, Date recurringStartDate, String iCalString, Boolean regenerativeFlag);

    @Update("UPDATE ergo.exchange_task SET update_date = #{lastUpdate}, delete_date = #{deleteDate} WHERE task_id = #{id}")
    void deleteTask(Task task);

    @Delete("DELETE from ergo.exchange_task WHERE task_uuid = #{0}")
    void deleteTaskForReal(String taskUuid);

    @Select("SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task WHERE provider_uuid = #{0} AND delete_date >= #{1}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getDeletedTasks(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.exchange_task SET last_server_sync_time = #{1} WHERE task_id = #{0}")
    void updateTaskLastSyncTime(int taskId, Date lastServerSyncTime);

    //uses reminder_minutes_before
    @Deprecated
    @Select(
        "SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task " +
        " WHERE delete_date IS NULL " +
        "  AND private_status <> 'COMPLETE' " +
        "  AND private_due_date IS NOT NULL " +
        "  AND private_reminder_minutes_before IS NOT NULL " +
        "  AND private_reminder_minutes_before <> 0 " +
        "  AND reminded = 0 " +
        "  AND NOW() >= DATE_SUB(private_due_date, INTERVAL private_reminder_minutes_before MINUTE)"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks();

    @Select(
        "SELECT 'EXCHANGE' as provider, exchange_task.* FROM ergo.exchange_task " +
        " WHERE delete_date IS NULL " +
        "  AND private_status <> 'COMPLETE' " +
        "  AND reminder IS NOT NULL " +
        "  AND reminded = 0 " +
        "  AND NOW() >= reminder"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_desc"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="private_priority"),
        @Result(property="reminderMinBefore", column="private_reminder_minutes_before"),
        @Result(property="reminded", column="reminded"),
        @Result(property="note", column="private_note"),
        @Result(property="order", column="order"),
        @Result(property="priorityOrder", column="priority_order"),
        @Result(property="roleOrder", column="role_order"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="reminded", column="reminded"),
        @Result(property="reminder", column="reminder"),
        @Result(property="completedDate", column="completed_date"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="private_status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks2();

    @Update("UPDATE ergo.exchange_task SET reminded = 1 WHERE task_id = #{0}")
    void setTaskReminded(int taskId);

    //
    // MISC
    //

    @Insert(
        "INSERT INTO ergo.exchange_server_map (email_domain, exchange_server, create_date, update_date) VALUES (#{0}, #{1}, NOW(), NOW()) " +
        "ON DUPLICATE KEY UPDATE exchange_server = VALUES(exchange_server), update_date = NOW()"
    )
    void addOrUpdateEmailDomainServerMapping(String emailDomain, String exchangeServer);
}
