package com.meinc.zztasks.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.meinc.zztasks.domain.Rfc2445;
import com.meinc.zztasks.domain.Task;

public class DbToDomainConverter
{
    public static Task getTaskFromDbTask(DbTask dbTask)
    {
        Task task = new Task();
        task.setProviderType(dbTask.getProviderType());
        task.setId(dbTask.getId());
        task.setUuid(dbTask.getUuid());
        task.setSubscriberId(dbTask.getSubscriberId());
        task.setDescription(dbTask.getDescription());
        task.setDueDate(dbTask.getDueDate());
        task.setPriority(dbTask.getPriority());
        task.setReminderMinBefore(dbTask.getReminderMinBefore());
        task.setReminded(dbTask.isReminded());
        task.setNote(dbTask.getNote());
        task.setNextTaskCreated(dbTask.getNextTaskCreated());
        task.setOrder(dbTask.getOrder());
        task.setPriorityOrder(dbTask.getPriorityOrder());
        task.setRoleOrder(dbTask.getRoleOrder());
        task.setRoleId(dbTask.getRoleId());
        task.setRoleUuid(dbTask.getRoleUuid());
        task.setReminder(dbTask.getReminder());
        task.setPrivateFlag(dbTask.isPrivateFlag());
        task.setCompletedDate(dbTask.getCompletedDate());
        task.setTimezoneId(dbTask.getTimezoneId());
        task.setCreateDate(dbTask.getCreateDate());
        task.setLastUpdate(dbTask.getLastUpdate());
        task.setDeleteDate(dbTask.getDeleteDate());
        task.setServerId(dbTask.getServerId());
        task.setProviderUuid(dbTask.getProviderUuid());
        task.setLastServerSyncTime(dbTask.getLastServerSyncTime());

        String statusVal = dbTask.getDbStatus();
        String val = dbTask.getDbRecurringRRule();
        Date recurringStartDate = dbTask.getDbRecurringStartDate();
        Boolean dbRecurringRegenerativeFlag = dbTask.getDbRecurringRegenerativeFlag();

        if (statusVal != null) {
            Task.STATUS status = Task.STATUS.valueOf(statusVal);
            if (status == Task.STATUS.COMPLETE)
                task.setComplete(task.getCompletedDate() != null ? task.getCompletedDate() : task.getLastUpdate());
            else
                task.setStatus(status);
        }

        if (val != null) {
            Rfc2445 rfc2445 = new Rfc2445(val, recurringStartDate, dbRecurringRegenerativeFlag);
            task.setRfc2445(rfc2445);
        }

        return task;
    }

    public static List<Task> getTasksFromDbTasks(List<DbTask> dbTasks)
    {
        List<Task> tasks = new ArrayList<>();

        if (dbTasks != null) {
            for (DbTask dbTask : dbTasks) {
                tasks.add(getTaskFromDbTask(dbTask));
            }
        }

        return tasks;
    }
}