package com.meinc.zztasks;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.DbTask;
import com.meinc.zztasks.db.IExchangeDao;
import com.meinc.zztasks.domain.Note;
import com.meinc.zztasks.domain.Role;
import com.meinc.zztasks.domain.Task;

public class ExchangeTester
extends BaseTester
implements IExchangeDao
{

    @Override
    public void nukeAllTasks(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.nukeAllTasks(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void nukeAllNotes(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.nukeAllNotes(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void nukeAllRoles(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.nukeAllRoles(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addRole(int subscriberId, Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getRole(roleUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Role> getRoles(int subscriberId, String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getRoles(subscriberId, providerUuid, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateRole(Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.deleteRole(role);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getCurrentTasksForRole(int roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getCurrentTasksForRole(roleId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getCurrentNotesForRole(int roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getCurrentNotesForRole(roleId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Role> getDeletedRoles(String providerUuid, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getNote(noteUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getNotes(String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getNotes(providerUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public Note getNoteByServerId(int subscriberId, String serverId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getNoteByServerId(subscriberId, serverId);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateNote(Note note, Integer roleId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.deleteNote(note);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteNoteForReal(String noteUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.deleteNoteForReal(noteUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public String getNoteUuidFromServerId(String serverId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getNoteUuidFromServerId(serverId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<String> getTagIdsForNote(int noteId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.updateNoteLastSyncTime(noteId, lastServerSyncTime);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTask(int subscriberId, Task task, Integer roleId, Date rfc2445StartDate, String rfc2445iCalString,
            Boolean rfc2445IsRegenerative)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getTask(taskUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public DbTask getTaskByServerId(int subscriberId, String serverId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getTaskByServerId(subscriberId, serverId);

        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getTasks(String providerUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            return mapper.getTasks(providerUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateTask(Task task, Integer roleId, Integer reminderMinutesBefore, String note,
            Date recurringStartDate, String iCalString, Boolean regenerativeFlag)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.updateTask(task, roleId, reminderMinutesBefore, note, recurringStartDate, iCalString, regenerativeFlag);

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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.deleteTask(task);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteTaskForReal(String taskUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.deleteTaskForReal(taskUuid);

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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.updateTaskLastSyncTime(taskId, lastServerSyncTime);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addOrUpdateEmailDomainServerMapping(String emailDomain, String exchangeServer)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.addOrUpdateEmailDomainServerMapping(emailDomain, exchangeServer);

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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
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
            IExchangeDao mapper = session.getMapper(IExchangeDao.class);
            mapper.setTaskReminded(taskId);

            session.commit();
        } finally {
            session.close();
        }
    }

    public static void main(String[] args)
    {
        int SHAWKER = 358;
        ExchangeTester tester = new ExchangeTester();

//        String r1Uuid = "3100bcff-b940-11e9-85b9-22000a66be75";
//        String r2Uuid = "36a7c5f7-b94c-11e9-85b9-22000a66be75";
//        Role r = new Role();
//        r.setUuid(r2Uuid);
//        r.setCreateDate(new Date());
//        r.setLastUpdate(new Date());
//        r.setProviderUuid("042c9b45-f463-4fe1-ac84-023a75c912d5"); //pulled one out at random
//        r.setProviderType(Role.PROVIDER_TYPE.EXCHANGE);
//        r.setOrder(5);
//        r.setServerId("some_server_id");
//        r.setEtag("some_etag");
//        r.setLastServerSyncTime(new Date());
//        r.setSubscriberId(SHAWKER);
//        r.setName("r1");
//        r.setColor("0x9278d1");
//        r.setProviderColor("9278d1");
//        r.setIcon("art");
//
//        System.out.println("role pre: " + r);
//        tester.addRole(SHAWKER, r);
//        System.out.println("role post: " + r);

//        Role r = tester.getRole(r1Uuid);
        //System.out.println(r);

//        List<Role> roles = tester.getRoles(377, "042c9b45-f463-4fe1-ac84-023a75c912d5", new Date(0));
//        roles.forEach(System.out::println);

//        r.setName("updated name");
//        r.setIcon("house");
//        r.setColor("b0c18a");
//        r.setProviderColor("b0c18a");
//        r.setLastUpdate(new Date());
//        tester.updateRole(r);

//        r.setDeleteDate(new Date());
//        r.setLastUpdate(new Date());
//        tester.deleteRole(r);

//        int roleId = 15484;
//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getCurrentTasksForRole(roleId));
//        tasks.forEach(System.out::println);
//        int roleId = 15483;
//        List<Note> notes = tester.getCurrentNotesForRole(roleId);
//        notes.forEach(System.out::println);
//        List<Role> roles = tester.getDeletedRoles("042c9b45-f463-4fe1-ac84-023a75c912d5", new Date(0));
//        roles.forEach(System.out::println);

//        tester.updateRoleLastSyncTime(r.getId(), new Date());
//        Note n = new Note();
//        int roleId = 121123;
//
//        String note1Uuid = "bf5f7b8b-b957-11e9-85b9-22000a66be75";
//        n.setUuid(note1Uuid);
//        n.setServerId("some_server_id");
//        n.setProviderUuid("042c9b45-f463-4fe1-ac84-023a75c912d5");
//        n.setRoleUuid("36a7c5f7-b94c-11e9-85b9-22000a66be75");
//        n.setOrder(5);
//        n.setNote("this is the note");
//        n.setPrivateFlag(true);
//        n.setCreateDate(new Date());
//        n.setLastUpdate(new Date());
//        tester.addNote(SHAWKER, n, roleId);

//        Note n = tester.getNote(note1Uuid);
//        System.out.println(n);

//        List<Note> notes = tester.getNotes("042c9b45-f463-4fe1-ac84-023a75c912d5");
//        notes.forEach(System.out::println);

//        Note n = tester.getNoteByServerId(SHAWKER, "some_server_id");
//        n.setServerId("another_server_id");
//        n.setRoleUuid("36a7c5f7-b94c-11e9-85b9-22000a66be75");
//        n.setOrder(8);
//        n.setPrivateFlag(false);
//        n.setNote("an udpated note");
//        n.setLastUpdate(new Date());
//        tester.updateNote(n, roleId);

//        n.setLastUpdate(new Date());
//        n.setDeleteDate(new Date());
//        tester.deleteNote(n);
//        tester.deleteNoteForReal(note1Uuid);

        //System.out.println(n);

//        String serverId = "AAMkADgyY2Q5YjU1LWQzMDMtNGNjMS1iMzdjLTIxNmFhMjQwMjkwNgBGAAAAAABYmnC3+tpjS6zaM3ZVoxlUBwCLrp74RqTLRqDPjsYL72tFABVBn1HeAAC5U8zi0JhTR7GjXJ0TSNJ8AAPFXTN2AAA=";
//        String noteUuid = tester.getNoteUuidFromServerId(serverId);
//        System.out.println(noteUuid);

//        int noteId = 25136;
//        List<String> tagIds = tester.getTagIdsForNote(noteId);
//        tagIds.forEach(System.out::println);

//        String tagUuid = "6cf91447-b955-11e9-85b9-22000a66be75";
//        tester.addTagToNote(noteId, tagUuid);

//        tester.removeAllTagsFromNote(noteId);

//        List<Note> notes = tester.getDeletedNotes("5532c2d7-7137-4069-9dd1-b4559413a7b3", new Date(0));
//        notes.forEach(System.out::println);

//        int noteId = 102099;
//        tester.updateNoteLastSyncTime(noteId, new Date());

//        tester.deleteNoteForReal("0b71ca00-b951-11e9-85b9-22000a66be75");
//        tester.deleteNoteForReal("bf5f7b8b-b957-11e9-85b9-22000a66be75");

//        int roleId = 16998;
//        String roleUuid = "d03d0327-5bb4-431c-81f3-405a70ef08c8";
//        String taskUuid = "3898b469-b95a-11e9-85b9-22000a66be75";
//        String taskProviderUuid = "042c9b45-f463-4fe1-ac84-023a75c912d5";
//        String taskServerId = "task_server_id_ab12";
//        Task task = new Task();
//        task.setUuid(taskUuid);
//        task.setRoleUuid(roleUuid);
//        task.setServerId(taskServerId);
//        task.setProviderUuid(taskProviderUuid);
//        task.setOrder(3);
//        task.setPrivateFlag(true);
//        task.setDescription("task description");
//        task.setDueDate(new Date());
//        task.setPriority(Task.PRIORITY.A);
//        task.setReminderMinBefore(15);
//        task.setNote("the task note");
//        task.setStatus(Task.STATUS.NORMAL);
//        task.setPriorityOrder(4);
//        task.setRoleOrder(5);
//        task.setTimezoneId("America/Denver");
//        task.setCreateDate(new Date());
//        task.setLastUpdate(new Date());
//
//        tester.addTask(SHAWKER, task, roleId, new Date(), "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE,FR", true);

//        Task task = DbToDomainConverter.getTaskFromDbTask(tester.getTask(taskUuid));
//        Task task = DbToDomainConverter.getTaskFromDbTask(tester.getTaskByServerId(SHAWKER, taskServerId));
//        System.out.println(task);
//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getTasks(taskProviderUuid));
//        tasks.forEach(System.out::println);

//        task.setRoleUuid(null);
//        tester.updateTask(task, null, null, null, null, null, null);

//        task.setLastUpdate(new Date());
//        task.setDeleteDate(new Date());
//        tester.deleteTask(task);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getDeletedTasks(taskProviderUuid, new Date(0)));
//        tasks.forEach(System.out::println);

//        tester.updateTaskLastSyncTime(task.getId(), new Date());
//        tester.deleteTaskForReal(taskUuid);

//        tester.addOrUpdateEmailDomainServerMapping("foo_flub_123.comx", "servercomx.fooodge");

//        tester.nukeAllTasks(SHAWKER);
//        tester.nukeAllNotes(SHAWKER);
//        tester.nukeAllRoles(SHAWKER);

//        tester.removeNoteTagJoins(10101);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getToBeRemindedTasks());
//        tasks.forEach(System.out::println);
//        tasks = DbToDomainConverter.getTasksFromDbTasks(tester.getToBeRemindedTasks2());
//        tasks.forEach(System.out::println);

//        tester.setTaskReminded(-10101);
    }

}
