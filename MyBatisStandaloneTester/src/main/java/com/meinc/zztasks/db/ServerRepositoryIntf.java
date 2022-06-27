package com.meinc.zztasks.db;

public interface ServerRepositoryIntf
{
    public void nukeAllTasks(int subscriberId);

    public void nukeAllNotes(int subscriberId);

    public void nukeAllRoles(int subscriberId);

}
