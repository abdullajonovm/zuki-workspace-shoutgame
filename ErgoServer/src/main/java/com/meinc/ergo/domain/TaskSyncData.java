package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskSyncData 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<String> deleted = new ArrayList<String>();
    private List<Task> tasks = new ArrayList<Task>();
    
    public List<String> getDeleted()
    {
        return deleted;
    }
    public void setDeleted(List<String> deleted)
    {
        this.deleted = deleted;
    }
    public void addDeleted(String deletedRoleId)
    {
        this.deleted.add(deletedRoleId);
    }
    public List<Task> getTasks()
    {
        return tasks;
    }
    public void setTasks(List<Task> tasks)
    {
        this.tasks = tasks;
    }
    public void addTask(Task modifiedOrNewTask)
    {
        this.tasks.add(modifiedOrNewTask);
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("TASKS:");
        for (Task task : tasks) {
            buf.append("\n\t").append(task);
        }
        buf.append("\nDELETED:");
        for (String sid : deleted) {
            buf.append("\n\t").append(sid);
        }

        return buf.toString();
    }
}
