package com.meinc.zztasks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.meinc.zztasks.db.DbTask;
import com.meinc.zztasks.db.DbToDomainConverter;
import com.meinc.zztasks.db.IErgoDao;
import com.meinc.zztasks.domain.ImportId;
import com.meinc.zztasks.domain.ImportStatus;
import com.meinc.zztasks.domain.Note;
import com.meinc.zztasks.domain.Role;
import com.meinc.zztasks.domain.Tag;
import com.meinc.zztasks.domain.Task;

import tv.shout.util.Tuple;

public class ErgoTester
extends BaseTester
implements IErgoDao
{

    @Override
    public void nukeAllTasks(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.nukeAllRoles(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
        // TODO Auto-generated method stub
    }

    @Override
    public List<ImportId> getImportedIds(String transactionUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getImportedIds(transactionUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public void beginImport(String transactionUuid, int totalItems)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.beginImport(transactionUuid, totalItems);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addImportId(String transactionUuid, String origRoleUuid, String newRoleUuid, int newRoleId,
            String noteUuid, String taskUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addImportId(transactionUuid, origRoleUuid, newRoleUuid, newRoleId, noteUuid, taskUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateImportProgress(String transactionUuid, int completeItems)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.updateImportProgress(transactionUuid, completeItems);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void setImportEndStatus(String transactionUuid, String status, String message, String failedOpIds, String successfulOpIds)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.setImportEndStatus(transactionUuid, status, message, failedOpIds, successfulOpIds);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void setImportReceivedButNotStarted(String transactionUuid, int subscriberId, String fromProviderUuid, String toProviderUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.setImportReceivedButNotStarted(transactionUuid, subscriberId, fromProviderUuid, toProviderUuid);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public ImportStatus getImportStatus(String transactionUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getImportStatus(transactionUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public void addRole(int subscriberId, Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addRole(subscriberId, role);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Role getRole(int subscriberId, String roleUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getRole(subscriberId, roleUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Role> getRoles(int subscriberId, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getRoles(subscriberId, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateRole(Role role)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTasksForRole(roleId);

        } finally {
            session.close();
        }
    }

    @Override
    public void addNote(int subscriberId, Note note)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addNote(subscriberId, note);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Note getNote(int subscriberId, String noteUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getNote(subscriberId, noteUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Note> getNotes(int subscriberId, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getNotes(subscriberId, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateNote(Note note)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.updateNote(note);

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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTagIdsForNote(noteId);

        } finally {
            session.close();
        }
    }

    @Override
    public void removeAllTagsFromNote(int noteId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.removeAllTagsFromNote(noteId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTask(int subscriberId, Task task, Date rfc2445StartDate, String rfc2445iCalString, Boolean rfc2445IsRegenerative)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addTask(subscriberId, task, rfc2445StartDate, rfc2445iCalString, rfc2445IsRegenerative);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public DbTask getTask(int subscriberId, String taskUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTask(subscriberId, taskUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<DbTask> getTasks(int subscriberId, Date lastSyncTime)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTasks(subscriberId, lastSyncTime);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateTask(Task task, Integer roleId, Date rfc2445StartDate, String iCalString, Boolean regenerative)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.updateTask(task, roleId, rfc2445StartDate, iCalString, regenerative);

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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.deleteTask(task);

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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.setTaskReminded(taskId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTag(Tag tag)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addTag(tag);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Tag getTag(String tagUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTag(tagUuid);

        } finally {
            session.close();
        }
    }

    @Override
    public List<Tag> getTags(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getTags(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void updateTag(Tag tag)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.updateTag(tag);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteTag(int tagId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.deleteTag(tagId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTagToSubscriber(int tagId, int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addTagToSubscriber(tagId, subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void removeTagSubscriberJoin(int tagId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.removeTagSubscriberJoin(tagId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addTagToNote(int noteId, String tagUuid)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
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
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.removeNoteTagJoins(tagId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Integer> getAllNonExpiringPremiumSubscriberIds(String sql)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getAllNonExpiringPremiumSubscriberIds(sql);

        } finally {
            session.close();
        }
    }

    @Override
    public void deleteSubscriberPremiumEntitlements(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.deleteSubscriberPremiumEntitlements(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteSubscriberReceipts(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.deleteSubscriberReceipts(subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void addSubscriberPremiumReceipt(String uuid1, String premiumItemUuid, int subscriberId, String receipt, Date newExpireDate)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addSubscriberPremiumReceipt(uuid1, premiumItemUuid, subscriberId, receipt, newExpireDate);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public int getSubscriberMostRecentReceiptId(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getSubscriberMostRecentReceiptId(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void addSubscriberPremiumEntitlements(int subscriberId, int receiptId, int premiumItemId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.addSubscriberPremiumEntitlements(subscriberId, receiptId, premiumItemId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateSubscriberPremiumState(Date newExpireDate, int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.updateSubscriberPremiumState(newExpireDate, subscriberId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Tuple<String>> getPrefs(int subscriberId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getPrefs(subscriberId);

        } finally {
            session.close();
        }
    }

    @Override
    public void setPref(int subscriberId, String prefName, String prefValue)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.setPref(subscriberId, prefName, prefValue);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void incrementAffiliateAccess(String affiliateId)
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            mapper.incrementAffiliateAccess(affiliateId);

            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Date getDbNow()
    {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            IErgoDao mapper = session.getMapper(IErgoDao.class);
            return mapper.getDbNow();

        } finally {
            session.close();
        }
    }

    private void testImport(int subscriberId, String googleProviderUuid, String ergoProviderUuid)
    {
        String transactionUuid = "c8792fce-c41e-11e9-85b9-22000a66be75";

//        setImportReceivedButNotStarted(transactionUuid, subscriberId, googleProviderUuid, ergoProviderUuid);
//        beginImport(transactionUuid, 1);

//        String origRoleUuid = "8d091e36-c41f-11e9-85b9-22000a66be75";
//        String newRoleUuid = "9316b068-c41f-11e9-85b9-22000a66be75";
//        int newRoleId = 10101;
//        String noteUuid = "9a2a6b25-c41f-11e9-85b9-22000a66be75";
//        String taskUuid = "9f0d14b3-c41f-11e9-85b9-22000a66be75";
//        addImportId(transactionUuid, origRoleUuid, newRoleUuid, newRoleId, noteUuid, taskUuid);

//        List<ImportId> importIds = getImportedIds(transactionUuid);
//        importIds.forEach(System.out::println);

//        System.out.println(getImportStatus(transactionUuid));
//        updateImportProgress(transactionUuid, 1);
//        System.out.println(getImportStatus(transactionUuid));

        setImportEndStatus(transactionUuid, "COMPLETE", "all done", null, "10101");
        System.out.println(getImportStatus(transactionUuid));
    }

    private void testRole(int subscriberId, String roleUuid, String noteUuid, String taskUuid)
    {
//        Role role = new Role();
//        role.setUuid(roleUuid);
//        role.setName("mybatis test role");
//        role.setColor("50328f");
//        role.setIcon("family");
//        role.setOrder(3);
//        role.setCreateDate(new Date());
//        role.setLastUpdate(new Date());
//
//        System.out.println(role);
//        addRole(subscriberId, role);
//        System.out.println(role);

        Role role = getRole(subscriberId, roleUuid);
//        System.out.println(role);

//        List<Role> roles = getRoles(subscriberId, new Date(0));
//        roles.forEach(System.out::println);

//        Note note = new Note();
//        note.setUuid(noteUuid);
//        note.setNote("this is the mybatis test note");
//        note.setPrivateFlag(true);
//        note.setRoleId(role.getId());
//        note.setRoleUuid(role.getUuid());
//        note.setOrder(2);
//        note.setCreateDate(new Date());
//        note.setLastUpdate(new Date());
//        addNote(subscriberId, note);
//        System.out.println(note);

//        List<Note> notes = getNotesForRole(role.getId());
//        notes.forEach(System.out::println);

//        Task task = new Task();
//        task.setUuid(taskUuid);
//        task.setDescription("the task description");
//        task.setDueDate(new Date(System.currentTimeMillis() + 99_999_999L));
//        task.setPriority(Task.PRIORITY.A);
//        task.setRoleId(role.getId());
//        task.setRoleUuid(role.getUuid());
//        task.setReminderMinBefore(15);
//        task.setReminder(new Date());
//        task.setPrivateFlag(true);
//        task.setNote("this is the embedded task note");
//        task.setStatus(Task.STATUS.NORMAL);
//        task.setOrder(4);
//        task.setPriorityOrder(3);
//        task.setRoleOrder(2);
//        task.setTimezoneId("America/Denver");
//        task.setCreateDate(new Date());
//        task.setLastUpdate(new Date());
//        Date rfc2445StartDate = new Date();
//        String rfc2445iCalString = "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO;UNTIL=20200325T000000Z";
//        boolean rfc2445IsRegenerative = false;
//        addTask(subscriberId, task, rfc2445StartDate, rfc2445iCalString, rfc2445IsRegenerative);
//        System.out.println(task);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(getTasksForRole(role.getId()));
//        tasks.forEach(System.out::println);

//        role.setName("updated name");
//        role.setColor("729bd9");
//        role.setIcon("work");
//        role.setOrder(1);
//        role.setLastUpdate(new Date());
//        updateRole(role);
    }

    private void testNote(int subscriberId, String noteUuid)
    {
        Note note = getNote(subscriberId, noteUuid);
//        System.out.println(note);

//        List<Note> notes = getNotes(subscriberId, new Date(0));
//        notes.forEach(System.out::println);

//        note.setNote("updated note");
//        note.setPrivateFlag(true);
//        note.setRoleId(1487);
//        note.setRoleUuid("38fb745d-97c3-497b-99e2-b1d485a5c019");
//        note.setOrder(1);
//        note.setLastUpdate(new Date());
//        updateNote(note);

        String tagUuid = "c0c93a6f-c428-11e9-85b9-22000a66be75";
//        Tag tag = new Tag();
//        tag.setUuid(tagUuid);
//        tag.setName("test note tag");
//        tag.setDescription("test note tag description");
//        tag.setCreateDate(new Date());
//        tag.setLastUpdate(new Date());
//        addTag(tag);
//        System.out.println(tag);

        Tag tag = getTag(tagUuid);
//        System.out.println(tag);

//        addTagToNote(note.getId(), tag.getUuid());
//        List<String> tagIds = getTagIdsForNote(note.getId());
//        tagIds.forEach(System.out::println);

        String tag2Uuid = "a5b714b0-c429-11e9-85b9-22000a66be75";
//        Tag tag2 = new Tag();
//        tag2.setUuid(tag2Uuid);
//        tag2.setName("test2 note tag");
//        tag2.setDescription("test note tag2 description");
//        tag2.setCreateDate(new Date());
//        tag2.setLastUpdate(new Date());
//        addTag(tag2);
//        addTagToNote(note.getId(), tag2Uuid);

//        removeNoteTagJoins(tag.getId());
//        removeAllTagsFromNote(note.getId());
    }

    private void testTags(int subscriberId)
    {
        String tagUuid = "c0c93a6f-c428-11e9-85b9-22000a66be75";
        String tag2Uuid = "a5b714b0-c429-11e9-85b9-22000a66be75";

        Tag tag1 = getTag(tagUuid);
        Tag tag2 = getTag(tag2Uuid);

//        addTagToSubscriber(tag1.getId(), subscriberId);
//        addTagToSubscriber(tag2.getId(), subscriberId);

//        List<Tag> tags = getTags(subscriberId);
//        tags.forEach(System.out::println);

//        removeTagSubscriberJoin(tag1.getId());
//        List<Tag> tags = getTags(subscriberId);
//        tags.forEach(System.out::println);

//        tag1.setName("updated tag1 name");
//        tag1.setDescription("updated tag1 description");
//        tag1.setLastUpdate(new Date());
//        updateTag(tag1);

//        removeTagSubscriberJoin(tag2.getId());
//        List<Tag> tags = getTags(subscriberId);
//        tags.forEach(System.out::println);

        deleteTag(tag1.getId());
        deleteTag(tag2.getId());
    }

    private void testTasks(int subscriberId, String roleUuid, String taskUuid)
    {
        Role role = getRole(subscriberId, roleUuid);
        Task task = DbToDomainConverter.getTaskFromDbTask(getTask(subscriberId, taskUuid));
//        System.out.println(task);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(getTasks(subscriberId, new Date(0)));
//        tasks.forEach(System.out::println);

//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(getToBeRemindedTasks());
//        tasks.forEach(System.out::println);
//        List<Task> tasks = DbToDomainConverter.getTasksFromDbTasks(getToBeRemindedTasks2());
//        tasks.forEach(System.out::println);

//        setTaskReminded(task.getId());

//        System.out.println(task);
//
//        task.setDescription("updated description");
//        task.setDueDate(new Date(System.currentTimeMillis() + 99_999_999L));
//        task.setPriority(Task.PRIORITY.BIGROCK);
//        task.setRoleUuid(roleUuid);
//        task.setReminderMinBefore(20);
//        task.setReminder(new Date());
//        task.setPrivateFlag(true);
//        task.setNote("updated embedded note");
//        task.setStatus(Task.STATUS.COMPLETE);
//        task.setCompletedDate(new Date());
//        task.setLastUpdate(new Date());
//        task.setReminded(true);
//        task.setTimezoneId(task.getTimezoneId());
//        updateTask(task, role.getId(), null, null, null);
//
//
//        task = DbToDomainConverter.getTaskFromDbTask(getTask(subscriberId, taskUuid));
//        System.out.println(task);
    }

    private void testDelete(int subscriberId, String roleUuid, String noteUuid, String taskUuid)
    {
        System.out.println("PRE");
        Role role = getRole(subscriberId, roleUuid);
        Note note = getNote(subscriberId, noteUuid);
        Task task = DbToDomainConverter.getTaskFromDbTask(getTask(subscriberId, taskUuid));
        Arrays.asList(role, note, task).forEach(System.out::println);

        final Date now = new Date();
        Arrays.asList(role, note, task).forEach(o -> {
            o.setDeleteDate(now);
            o.setLastUpdate(now);
        });
        deleteTask(task);
        deleteNote(note);
        deleteRole(role);

        System.out.println("POST");
        role = getRole(subscriberId, roleUuid);
        note = getNote(subscriberId, noteUuid);
        task = DbToDomainConverter.getTaskFromDbTask(getTask(subscriberId, taskUuid));
        Arrays.asList(role, note, task).forEach(System.out::println);
    }

    private void testNonExpiringSubscribers(int subscriberId, String uuid1, String uuid2)
    {
//        //interval is actually 2 MONTHS , not 2 YEARS, but this way we actually get results to play with
//        String sql = "select subscriber_id from ergo.subscriber s, ergo.forever_premium_subscribers fps where s.email = fps.email AND (state != 'PREMIUM' OR state_expiration_date < DATE_ADD(NOW(), INTERVAL 2 YEAR)) UNION select subscriber_id from ergo.subscriber where upper(email) like '%@%FRANKLINCOVEY%' AND (state != 'PREMIUM' OR state_expiration_date < DATE_ADD(NOW(), INTERVAL 2 YEAR))";
//
//        List<Integer> allNonExpiringPremiumSubscriberIds = getAllNonExpiringPremiumSubscriberIds(sql);
//        System.out.println(MessageFormat.format("{0}", allNonExpiringPremiumSubscriberIds));

        //the remainder of this code is copied from the AutoUpgradeDaemon, an hard-coded to work on the given subscriberId
        String premiumItemUuid = "77836c94-f8e5-4ebb-90b5-cb3b72572206";
        int premiumItemId = 11;
        String receipt = "{\"transactionId\":\""+uuid2+"\", \"description\":\"signup trial\"}";
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Date newExpireDate = c.getTime();

        //deleteSubscriberPremiumEntitlements(subscriberId);
        //deleteSubscriberReceipts(subscriberId);
        //addSubscriberPremiumReceipt(uuid1, premiumItemUuid, subscriberId, receipt, newExpireDate);
        //int receiptId = getSubscriberMostRecentReceiptId(subscriberId);
        //addSubscriberPremiumEntitlements(subscriberId, receiptId, premiumItemId);
        //updateSubscriberPremiumState(newExpireDate, subscriberId);
    }

    private void testOther(int subscriberId)
    {
//        setPref(subscriberId, "pref1", "prev1Value");
//
//        List<Tuple<String>> prefs = getPrefs(subscriberId);
//        prefs.forEach(t -> {
//            System.out.println(t.getKey() + " : " + t.getVal());
//        });

//        System.out.println(getDbNow());

//        incrementAffiliateAccess("SHAWKER");
    }

    public static void main(String[] args)
    {
        int SHAWKER = 358;
        String googleProviderUuid = "f5a28db8-c1ec-4d97-80eb-3285bed68ae4";
        String ergoProviderUuid = "893eede5-8a48-4bff-81d2-40557dcc26da";
        ErgoTester tester = new ErgoTester();

        //tester.testImport(SHAWKER, googleProviderUuid, ergoProviderUuid);

        String roleUuid = "9aa55b84-c423-11e9-85b9-22000a66be75";
        String noteUuid = "0407f95b-c425-11e9-85b9-22000a66be75";
        String taskUuid = "c27d8b9e-c425-11e9-85b9-22000a66be75";

        //tester.testRole(SHAWKER, roleUuid, noteUuid, taskUuid);
        //tester.testNote(SHAWKER, noteUuid);
        //tester.testTags(SHAWKER);
        //tester.testTasks(SHAWKER, roleUuid, taskUuid);
        //tester.testDelete(SHAWKER, roleUuid, noteUuid, taskUuid);

        String uuid1 = "a9e99777-c436-11e9-85b9-22000a66be75";
        String uuid2 = "af5b33d4-c436-11e9-85b9-22000a66be75";
        //tester.testNonExpiringSubscribers(SHAWKER, uuid1, uuid2);

        //tester.testOther(SHAWKER);
    }

}
