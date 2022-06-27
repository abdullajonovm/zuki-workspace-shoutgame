package com.meinc.zztasks;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.DbTask;
import com.meinc.zztasks.db.IGoogleDao;
import com.meinc.zztasks.domain.Note;
import com.meinc.zztasks.domain.Role;
import com.meinc.zztasks.domain.Task;

public class GoogleTester
extends BaseTester
implements IGoogleDao
{
    @Override
    public void nukeAllTasks(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.nukeAllTasks(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
        // TODO Auto-generated method stub
    }

    @Override
    public void nukeAllNotes(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.nukeAllNotes(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
        // TODO Auto-generated method stub
    }

    @Override
    public void nukeAllRoles(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.nukeAllRoles(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
        // TODO Auto-generated method stub
    }

    @Override
    public void addRole(int subscriberId, Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.addRole(subscriberId, role);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Role getRole(String roleUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getRole(roleUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Role> getRoles(String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getRoles(providerUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateRole(Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateRole(role);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteRole(Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.deleteRole(role);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getNotesForRole(int roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getNotesForRole(roleId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getTasksForRole(int roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getTasksForRole(roleId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Role> getDeletedRoles(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getDeletedRoles(providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateRoleLastSyncTime(int roleId, Date lastServerSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateRoleLastSyncTime(roleId, lastServerSyncTime);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addNote(int subscriberId, Note note, Integer roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.addNote(subscriberId, note, roleId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Note getNote(String noteUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getNote(noteUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getNotes(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getNotes(providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateNote(Note note, Integer roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateNote(note, roleId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteNote(Note note)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.deleteNote(note);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<String> getTagIdsForNote(int noteId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getTagIdsForNote(noteId);

        } finally {
            session.close();
        }
    }

    @Override
    public void addTagToNote(int noteId, String tagUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.addTagToNote(noteId, tagUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void removeNoteTagJoins(int tagId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.removeNoteTagJoins(tagId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void removeAllTagsFromNote(int noteId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.removeAllTagsFromNote(noteId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getDeletedNotes(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getDeletedNotes(providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateNoteLastSyncTime(int noteId, Date lastServerSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateNoteLastSyncTime(noteId, lastServerSyncTime);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTask(int subscriberId, Task task, Integer roleId, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.addTask(subscriberId, task, roleId, rfc2445StartDate, rfc2445iCalString, rfc2445IsRegenerative);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public DbTask getTask(String taskUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getTask(taskUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getTasks(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getTasks(providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateTask(Task task, Integer roleId, Integer reminderMinutesBefore, String note, Date rfc2445StartDate,
            String rfc2445iCalString, Boolean rfc2445IsRegenerative)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateTask(task, roleId, reminderMinutesBefore, note, rfc2445StartDate, rfc2445iCalString, rfc2445IsRegenerative);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteTask(Task task)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.deleteTask(task);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void setTaskServerId(int taskId, String serverId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.setTaskServerId(taskId, serverId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateTaskOrderFields(Task task)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateTaskOrderFields(task);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getDeletedTasks(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getDeletedTasks(providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateTaskLastSyncTime(int taskId, Date lastServerSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.updateTaskLastSyncTime(taskId, lastServerSyncTime);;

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getToBeRemindedTasks()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getToBeRemindedTasks();

        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getToBeRemindedTasks2()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            return mapper.getToBeRemindedTasks2();

        } finally {
            session.close();
        }
    }

    @Override
    public void setTaskReminded(int taskId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IGoogleDao mapper = session.getMapper(IGoogleDao.class);
            mapper.setTaskReminded(taskId);

            session.commit();
        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        GoogleTester tester = new GoogleTester();

        int SHAWKER = 358;
        String googleProviderUuid = "f5a28db8-c1ec-4d97-80eb-3285bed68ae4";

        String r1Uuid = "4cc9563c-ba04-11e9-85b9-22000a66be75";
//        int r1RoleId = 87656;
//        Role r1 = new Role();
//        r1.setServerId("server_id_a1");
//        r1.setUuid(r1Uuid);
//        r1.setProviderUuid(googleProviderUuid);
//        r1.setName("role_name");
//        r1.setIcon("art");
//        r1.setColor("f49454");
//        r1.setOrder(3);
//        r1.setCreateDate(new Date());
//        r1.setLastUpdate(new Date());
//        tester.addRole(SHAWKER, r1);

//        Role r1 = tester.getRole(r1Uuid);
//        System.out.println(r1);

//        List<Role> roles = tester.getRoles(googleProviderUuid);
//        roles.forEach(System.out::println);

//        Role r1 = tester.getRole(r1Uuid);
//        r1.setOrder(5);
//        r1.setName(r1.getName()+"x");
//        r1.setIcon("family");
//        r1.setColor("000000");
//        r1.setLastUpdate(new Date());
//        tester.updateRole(r1);

        Role r1 = tester.getRole(r1Uuid);
//        r1.setDeleteDate(new Date());
//        r1.setLastUpdate(new Date());
//        tester.deleteRole(r1);

//        int roleIdWithNote = 3613;
//        List<Note> notes = tester.getNotesForRole(roleIdWithNote);
//        notes.forEach(System.out::println);

//        int roleIdWithTasks = 3348;
//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getTasksForRole(roleIdWithTasks));
//        tasks.forEach(System.out::println);

//        List<Role> roles = tester.getDeletedRoles(googleProviderUuid, new Date(0));
//        roles.forEach(System.out::println);

//        tester.updateRoleLastSyncTime(r1RoleId, new Date());

//        Note n1 = new Note();
        String n1Uuid = "e45e7e59-ba1b-11e9-85b9-22000a66be75";
//        n1.setUuid(n1Uuid);
//        n1.setProviderUuid(googleProviderUuid);
//        n1.setNote("the note");
//        n1.setPrivateFlag(true);
//        n1.setRoleUuid(r1.getUuid());
//        n1.setOrder(8);
//        n1.setCreateDate(new Date());
//        n1.setLastUpdate(new Date());
//        tester.addNote(SHAWKER, n1, r1.getId());

        Note n1 = tester.getNote(n1Uuid);
//        System.out.println(n1);

//        List<Note> notes = tester.getNotes(googleProviderUuid, new Date(0));
//        notes.forEach(System.out::println);

//        n1.setNote(n1.getNote() + " x");
//        n1.setPrivateFlag(false);
//        n1.setRoleUuid(r1Uuid);
//        n1.setOrder(4);
//        n1.setLastUpdate(new Date());
//        tester.updateNote(n1, r1.getId());

//        n1.setDeleteDate(new Date());
//        n1.setLastUpdate(new Date());
//        tester.deleteNote(n1);

//        tester.addTagToNote(n1.getId(), "6cf91447-b955-11e9-85b9-22000a66be75");

//        List<String> tagIdsForNote = tester.getTagIdsForNote(n1.getId());
//        tagIdsForNote.forEach(System.out::println);

//        tester.removeAllTagsFromNote(n1.getId());

//        List<Note> deletedNotes = tester.getDeletedNotes(googleProviderUuid, new Date(0));
//        deletedNotes.forEach(System.out::println);

//        tester.updateNoteLastSyncTime(n1.getId(), new Date());

        //String roleUuid = "d03d0327-5bb4-431c-81f3-405a70ef08c8";
        String taskUuid = "bed10187-ba1e-11e9-85b9-22000a66be75";
//        String taskServerId = "server_id_a1";
//        Task task = new Task();
//        task.setUuid(taskUuid);
//        task.setProviderUuid(googleProviderUuid);
//        task.setServerId(taskServerId);
//        task.setEtag("etag");
//        task.setPrivateFlag(true);
//        task.setDescription("task description");
//        task.setNote("the task note");
//        task.setDueDate(new Date());
//        task.setPriority(Task.PRIORITY.A);
//        task.setRoleUuid(r1.getUuid());
//        task.setReminderMinBefore(15);
//        task.setReminder(new Date());
//        task.setReminded(false);
//        task.setStatus(Task.STATUS.NORMAL);
//        task.setOrder(3);
//        task.setPriorityOrder(4);
//        task.setRoleOrder(5);
//        task.setTimezoneId("America/Denver");
//        task.setCreateDate(new Date());
//        task.setLastUpdate(new Date());
//
//        System.out.println(task);
//        tester.addTask(SHAWKER, task, r1.getId(), new Date(), "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE,FR", true);
//        System.out.println(task);

        Task task = tester.getTask(taskUuid);
//        System.out.println(task);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getTasks(googleProviderUuid, new Date(0)));
//        tasks.forEach(System.out::println);

//        task.setEtag(task.getEtag()+"x");
//        task.setOrder(6);
//        task.setPrivateFlag(false);
//        task.setLastUpdate(new Date());
//        task.setPriority(Task.PRIORITY.C);
//        task.setReminder(new Date());
//        task.setStatus(Task.STATUS.DELEGATED);
//        task.setCompletedDate(new Date());
//        tester.updateTask(task, r1.getId(), 13, task.getNote()+" x", new Date(), "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE,FR", true);

//        task.setLastUpdate(new Date());
//        task.setDeleteDate(new Date());
//        tester.deleteTask(task);

//        tester.setTaskServerId(task.getId(), "new server id");

//        task.setLastUpdate(new Date());
//        task.setOrder(1);
//        task.setPriorityOrder(2);
//        task.setRoleOrder(3);
//        tester.updateTaskOrderFields(task);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getDeletedTasks(googleProviderUuid, new Date(0)));
//        tasks.forEach(System.out::println);

//        tester.updateTaskLastSyncTime(task.getId(), new Date());

//        tester.removeNoteTagJoins(10101);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getToBeRemindedTasks2());
//        tasks.forEach(System.out::println);
//        tester.setTaskReminded(-101);
    }

}
