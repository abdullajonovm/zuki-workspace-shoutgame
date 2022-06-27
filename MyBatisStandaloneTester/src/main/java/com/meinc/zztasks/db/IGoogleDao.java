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

public interface IGoogleDao
extends ServerRepositoryIntf
{
    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.google_task WHERE subscriber_id = #{0}")
    void nukeAllTasks(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.google_note WHERE subscriber_id = #{0}")
    void nukeAllNotes(int subscriberId);

    //ServerRepositoryIntf
    @Override
    @Delete("DELETE FROM ergo.google_role WHERE subscriber_id = #{0}")
    void nukeAllRoles(int subscriberId);

    //
    // ROLE
    //

    @Insert(
        "INSERT INTO ergo.`google_role` (server_id, role_uuid, subscriber_id, provider_uuid, `name`, `icon`, `color`, `order`, create_date, update_date) " +
        "VALUES(#{role.serverId}, #{role.uuid}, #{0}, #{role.providerUuid}, #{role.name}, #{role.icon}, #{role.color}, #{role.order}, #{role.createDate}, #{role.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="role.id")
    void addRole(final int subscriberId, @Param("role") final Role role);

    @Select("SELECT 'GOOGLE' as provider, google_role.* FROM ergo.`google_role` WHERE role_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="name", column="name"),
        @Result(property="color", column="color"),
        @Result(property="icon", column="icon"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="order", column="order"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    Role getRole(String roleUuid);

    @Select("SELECT 'GOOGLE' as provider, google_role.* FROM ergo.`google_role` WHERE provider_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="name", column="name"),
        @Result(property="color", column="color"),
        @Result(property="icon", column="icon"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="order", column="order"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Role> getRoles(String providerUuid);

    @Update(
        "UPDATE ergo.`google_role` " +
        "   SET `order` = #{order}, `name` = #{name}, `icon` = #{icon}, `color` = #{color}, `update_date` = #{lastUpdate}, `last_server_sync_time` = null " +
        " WHERE `role_id` = #{id}"
    )
    void updateRole(final Role role);

    @Update(
        "UPDATE ergo.`google_role` " +
        "   SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE role_id = #{id}"
    )
    void deleteRole(final Role role);

    @Select(
        "SELECT 'GOOGLE' as provider, google_note.* " +
        "  FROM ergo.google_note " +
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
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    List<Note> getNotesForRole(int roleId);

    @Select("SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task WHERE role_id = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getTasksForRole(int roleId);

    @Select("SELECT 'GOOGLE' as provider, google_role.* FROM ergo.`google_role` WHERE provider_uuid = #{0} AND delete_date >= #{1}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="role_id"),
        @Result(property="uuid", column="role_uuid"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="name", column="name"),
        @Result(property="color", column="color"),
        @Result(property="icon", column="icon"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="order", column="order"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time")
    })
    List<Role> getDeletedRoles(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.google_role SET last_server_sync_time = #{1} WHERE role_id = #{0}")
    void updateRoleLastSyncTime(int roleId, Date lastServerSyncTime);

    //
    // NOTE
    //

    @Insert(
        "INSERT INTO ergo.google_note (note_uuid, subscriber_id, provider_uuid, note, private_flag, role_id, role_uuid, `order`, create_date, update_date) " +
        "VALUES (#{note.uuid}, #{0}, #{note.providerUuid}, #{note.note}, #{note.privateFlag}, #{2}, #{note.roleUuid}, #{note.order}, #{note.createDate}, #{note.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="note.id")
    void addNote(final int subscriberId, @Param("note") final Note note, Integer roleId);

    @Select("SELECT 'GOOGLE' as provider, google_note.* FROM ergo.google_note WHERE note_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    Note getNote(String noteUuid);

    @Select(
        "SELECT 'GOOGLE' as provider, google_note.* " +
        "  FROM ergo.google_note " +
        " WHERE provider_uuid = #{0} " +
        "   AND update_date >= #{1} " +
        " ORDER BY `order`"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    List<Note> getNotes(String providerUuid, Date lastSyncTime);

    @Update(
        "UPDATE ergo.google_note " +
        "  SET note = #{note.note}, private_flag = #{note.privateFlag}, role_id = #{1}, role_uuid = #{note.roleUuid}, `order` = #{note.order}, update_date = #{note.lastUpdate} " +
        "WHERE note_id = #{note.id}"
    )
    void updateNote(@Param("note") Note note, Integer roleId);

    @Update("UPDATE ergo.google_note SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE note_id = #{id}")
    void deleteNote(Note note);

    @Select("select t.tag_uuid from ergo.tag t, ergo.google_note_tag gnt where gnt.note_id = #{0} and gnt.tag_id = t.tag_id")
    List<String> getTagIdsForNote(int noteId);

    @Insert(
        "insert into ergo.google_note_tag (tag_id, note_id) " +
        "  select tag_id, #{0} " +
        "    from ergo.tag " +
        "    where tag_uuid = #{1}"
    )
    void addTagToNote(int noteId, String tagUuid);

    @Delete("DELETE FROM ergo.google_note_tag WHERE tag_id = #{0}")
    void removeNoteTagJoins(int tagId);

    @Delete("DELETE FROM ergo.google_note_tag WHERE note_id = #{0}")
    void removeAllTagsFromNote(int noteId);

    @Select(
        "SELECT 'GOOGLE' as provider, google_note.* FROM ergo.google_note " +
        " WHERE provider_uuid = #{0} AND delete_date >= #{1}"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="note_id"),
        @Result(property="uuid", column="note_uuid"),
        @Result(property="order", column="order"),
        @Result(property="note", column="private_note"),
        @Result(property="privateFlag", column="private_flag"),
        @Result(property="roleId", column="role_id"),
        @Result(property="roleUuid", column="role_uuid"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
    })
    List<Note> getDeletedNotes(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.google_note SET last_server_sync_time = #{1} WHERE note_id = #{0}")
    void updateNoteLastSyncTime(int noteId, Date lastServerSyncTime);

    //
    // TASK
    //

    @Insert(
        "INSERT INTO ergo.google_task (" +
            "task_uuid, subscriber_id, provider_uuid, server_id, etag, " +
            "private_flag, private_description, private_note, private_due_date, " +
            "`priority`, role_id, role_uuid, reminder_minutes_before, reminder, reminded, `status`, completed_date, `order`, " +
            "recurring_start_date, recurring_rrule, recurring_regenerative_flag, " +
            "priority_order, role_order, timezone, create_date, update_date) " +
        "VALUES (" +
            "#{task.uuid}, #{0}, #{task.providerUuid}, #{task.serverId}, #{task.etag}, " +
            "#{task.privateFlag}, #{task.description}, #{task.note}, #{task.dueDate}, " +
            "#{task.priority}, #{2}, #{task.roleUuid}, #{task.reminderMinBefore}, #{task.reminder}, #{task.reminded}, #{task.status}, #{task.completedDate}, #{task.order}, " +
            "#{3}, #{4}, #{5}, " +
            "#{task.priorityOrder}, #{task.roleOrder}, #{task.timezoneId}, #{task.createDate}, #{task.lastUpdate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="task.id")
    void addTask(final int subscriberId, @Param("task") final Task task, Integer roleId, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative);

    @Select("SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task WHERE task_uuid = #{0}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    DbTask getTask(String taskUuid);

    @Select(
        "SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task " +
        " WHERE provider_uuid = #{0} " +
        "   AND update_date >= #{1} " +
        " ORDER BY `order`"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getTasks(String providerUuid, Date lastSyncTime);

    @Update(
        "UPDATE ergo.google_task " +
        "   SET role_id = #{1}, server_id = #{task.serverId}, etag = #{task.etag}, role_uuid = #{task.roleUuid}, `order` = #{task.order}, " +
        "       private_flag = #{task.privateFlag}, update_date = #{task.lastUpdate}, " +
        "       private_description = #{task.description}, private_due_date = #{task.dueDate}, `priority` = #{task.priority}, " +
        "       reminder_minutes_before = #{2}, reminder = #{task.reminder}, private_note = #{3}, `status` = #{task.status}, completed_date = #{task.completedDate}, last_server_sync_time = null, " +
        "       recurring_start_date = #{4}, recurring_rrule = #{5}, recurring_regenerative_flag = #{6}, next_task_created = #{task.nextTaskCreated}, reminded = #{task.reminded}, " +
        "       priority_order = #{task.priorityOrder}, role_order = #{task.roleOrder}, timezone = #{task.timezoneId} " +
        " WHERE task_id = #{task.id}"
    )
    void updateTask(@Param("task") Task task, Integer roleId, Integer reminderMinutesBefore, String note, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative);

    @Update(
        "UPDATE ergo.`google_task` " +
        "   SET delete_date = #{deleteDate}, update_date = #{lastUpdate} WHERE task_id = #{id}"
    )
    void deleteTask(final Task task);

    @Update("UPDATE ergo.google_task SET server_id = #{1} WHERE task_id = #{0}")
    void setTaskServerId(int taskId, String serverId);

    @Update(
        "UPDATE ergo.google_task " +
        "   SET `order` = #{order}, " +
        "       update_date = #{lastUpdate}, " +
        "       priority_order = #{priorityOrder}, role_order = #{roleOrder} " +
        " WHERE task_id = #{id}"
    )
    void updateTaskOrderFields(Task task);

    @Select("SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task WHERE provider_uuid = #{0} AND delete_date >= #{1}")
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getDeletedTasks(String providerUuid, Date lastSyncTime);

    @Update("UPDATE ergo.google_task SET last_server_sync_time = #{1} WHERE task_id = #{0}")
    void updateTaskLastSyncTime(int taskId, Date lastServerSyncTime);

    //uses reminder_minutes_before
    @Deprecated
    @Select(
        "SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task " +
        " WHERE delete_date IS NULL " +
        "  AND status <> 'COMPLETE' " +
        "  AND private_due_date IS NOT NULL " +
        "  AND reminder_minutes_before IS NOT NULL " +
        "  AND reminder_minutes_before <> 0 " +
        "  AND reminded = 0 " +
        "  AND NOW() >= DATE_SUB(private_due_date, INTERVAL reminder_minutes_before MINUTE)"
    )
    @Results({
        @Result(property="providerType", column="provider"),
        @Result(property="id", column="task_id"),
        @Result(property="uuid", column="task_uuid"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks();

    //uses reminder date field
    @Select(
        "SELECT 'GOOGLE' as provider, google_task.* FROM ergo.google_task " +
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
        @Result(property="description", column="private_description"),
        @Result(property="dueDate", column="private_due_date"),
        @Result(property="priority", column="priority"),
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
        @Result(property="nextTaskCreated", column="next_task_created"),
        @Result(property="createDate", column="create_date"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="deleteDate", column="delete_date"),
        @Result(property="timezoneId", column="timezone"),
        @Result(property="serverId", column="server_id"),
        @Result(property="providerUuid", column="provider_uuid"),
        @Result(property="etag", column="etag"),
        @Result(property="lastServerSyncTime", column="last_server_sync_time"),
        @Result(property="dbStatus", column="status"),
        @Result(property="dbRecurringRRule", column="recurring_rrule"),
        @Result(property="dbRecurringStartDate", column="recurring_start_date"),
        @Result(property="dbRecurringRegenerativeFlag", column="recurring_regenerative_flag")
    })
    List<DbTask> getToBeRemindedTasks2();

    @Update("UPDATE ergo.google_task SET reminded = 1 WHERE task_id = #{0}")
    void setTaskReminded(int taskId);

}
