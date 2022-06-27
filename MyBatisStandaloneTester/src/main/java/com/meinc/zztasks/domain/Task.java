package com.meinc.zztasks.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task
extends BaseEntityObject
{
    private static final long serialVersionUID = 1L;

    public static enum PRIORITY {BIGROCK, A, B, C};
    public static enum STATUS {NORMAL, INPROGRESS, DELEGATED, COMPLETE};

    private String description;

    private Date dueDate; //optional

    private PRIORITY priority = PRIORITY.B; //optional

    private int roleId; //optional

    private String roleUuid; //optional

    @Deprecated
    private int reminderMinBefore; //optional

    @Deprecated
    private boolean reminded;

    private Date reminder; //optional

    private boolean privateFlag; //optional

    private String note; //optional

    private STATUS status = STATUS.NORMAL;

    private Date completedDate; //optional

    private String delegateEmail;

    private Rfc2445 rfc2445;

    private Integer nextTaskCreated; //if null, next task not created, if not null, the id of the next task that was created

    private int priorityOrder; //used for sorting when in the "priority" view of the UI

    private int roleOrder; //used for sorting when in the "role" view of the UI

    private String timezoneId;

    @Override
    public String getUuid()
    {
        return uuid;
    }

    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public Date getDueDate()
    {
        return dueDate;
    }
    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }
    public PRIORITY getPriority()
    {
        return priority;
    }
    public void setPriority(PRIORITY priority)
    {
        this.priority = priority;
    }
    public int getReminderMinBefore()
    {
        return reminderMinBefore;
    }
    public void setReminderMinBefore(int reminderMinBefore)
    {
        this.reminderMinBefore = reminderMinBefore;
    }
    public boolean isReminded() {
        return reminded;
    }

    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }

    public Date getReminder()
    {
        return reminder;
    }

    public void setReminder(Date reminder)
    {
        this.reminder = reminder;
    }

    public boolean isPrivateFlag()
    {
        return privateFlag;
    }
    public void setPrivateFlag(boolean privateFlag)
    {
        this.privateFlag = privateFlag;
    }
    public STATUS getStatus()
    {
        return status;
    }
    public void setStatus(STATUS status)
    {
        if (this.status == STATUS.COMPLETE && status == STATUS.COMPLETE) {
            ; //do nothing
        } else if (status == STATUS.COMPLETE) {
            setComplete(this.getLastUpdate());
        } else {
            //not complete
            this.status = status;
            this.completedDate = null;
        }
    }
    public void setComplete(Date completeTime)
    {
        this.status = STATUS.COMPLETE;
        this.completedDate = completeTime == null ? new Date() : completeTime;
    }

    public Date getCompletedDate()
    {
        //return completedDate;
        if (completedDate == null) {
            if (getStatus() == STATUS.COMPLETE) {
                completedDate = getLastUpdate() != null ? getLastUpdate() : new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48)); //two days ago so it doesn't show up in the today list
                //logger.debug("*** getCompletedDate: " + completedDate + ", not set (using NOW) but status == COMPLETE for: " + getDescription());
                return completedDate; //TODO: persist somehow
            } else {
                //logger.debug("*** getCompletedDate: NULL, completedDate set but status != COMPLETE (status: " + getStatus() + ") for " + getDescription());
                return null;
            }
        } else {
            if (getStatus() != STATUS.COMPLETE) {
                //logger.debug("*** getCompletedDate: NULL, completedDate != null but status != COMPLETE (status: " + getStatus() + ") for " + getDescription());
                completedDate = null; //TODO: persist somehow
                return null;
            } else {
                //logger.debug("*** getCompletedDate: " + completedDate + ", already set and status == COMPLETE for " + getDescription());
                return completedDate;
            }
        }
    }

    public boolean hasCompletedDate()
    {
        return this.completedDate != null;
    }

    public void setCompletedDate(Date completedDate)
    {
        this.completedDate = completedDate;
    }

    public int getRoleId()
    {
        return roleId;
    }

    public void setRoleId(int roleId)
    {
        this.roleId = roleId;
    }

    public String getRoleUuid()
    {
        return roleUuid;
    }

    public void setRoleUuid(String roleUuid)
    {
        this.roleUuid = roleUuid;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public String getDelegateEmail()
    {
        return delegateEmail;
    }

    public void setDelegateEmail(String delegateEmail)
    {
        this.delegateEmail = delegateEmail;
    }

    public Rfc2445 getRfc2445()
    {
        return rfc2445;
    }

    public void setRfc2445(Rfc2445 rfc2445)
    {
        this.rfc2445 = rfc2445;
    }

    public Integer getNextTaskCreated()
    {
        return nextTaskCreated;
    }

    public void setNextTaskCreated(Integer nextTaskCreated)
    {
        this.nextTaskCreated = nextTaskCreated;
    }

    public int getPriorityOrder()
    {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder)
    {
        this.priorityOrder = priorityOrder;
    }

    public int getRoleOrder()
    {
        return roleOrder;
    }

    public void setRoleOrder(int roleOrder)
    {
        this.roleOrder = roleOrder;
    }

    public String getTimezoneId()
    {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId)
    {
        this.timezoneId = timezoneId;
    }

//    /**
//     * Make a copy of the existing task, but remove all the fields that should only be stored in exchange (unless of course this is a private
//     * task in which case no fields are removed).
//     *
//     * @return
//     */
//    public Task getClonedTastWithoutExchangeData()
//    {
//        Task clone = Util.clone(this);
//        if (!isPrivateFlag()) {
//            //clone.setPriority(null);
//            clone.setNote(null);
//            //clone.setStatus(null); //removing this messes with completed date
//        }
//        return clone;
//    }
//
//    public Task getClonedTaskWihoutGoogleData()
//    {
//        Task clone = Util.clone(this);
//        if (!isPrivateFlag()) {
//            clone.setNote(null);
//        }
//        return clone;
//    }

    /**
     * Add in exchange-specific fields.
     *
     * @param exchangeTask
     */
    public void addExchangeData(Task exchangeTask)
    {
        setDescription(exchangeTask.getDescription());
        setDueDate(exchangeTask.getDueDate());
        setPriority(exchangeTask.getPriority());
        setNote(exchangeTask.getNote());
        setStatus(exchangeTask.getStatus());
    }

//    public static Task fromString(String escapedJson)
//    {
//        Task w = null;
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            w = mapper.readValue(escapedJson, Task.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return w;
//    }

    public static Task getTestStaleTask()
    {
        Task t = new Task();
        t.setDescription("stale task");
        return t;
    }

    public static List<String> getFieldNamesThatDiffer(Task t1, Task t2)
    {
        List<String> fieldsThatDiffer = new ArrayList<String>();

        if (t1.getDelegateEmail() == null && t2.getDelegateEmail() == null)
            ;
        else {
            if (t1.getDelegateEmail() != null && t2.getDelegateEmail() == null)
                fieldsThatDiffer.add("delegateEmail");
            else if (t1.getDelegateEmail() == null && t2.getDelegateEmail() != null)
                fieldsThatDiffer.add("delegateEmail");
            else if (!t1.getDelegateEmail().equals(t2.getDelegateEmail()))
                fieldsThatDiffer.add("delegateEmail");
        }

        if (t1.getDescription() == null && t2.getDescription() == null)
            ;
        else {
            if (t1.getDescription() != null && t2.getDescription() == null)
                fieldsThatDiffer.add("description");
            else if (t1.getDescription() == null && t2.getDescription() != null)
                fieldsThatDiffer.add("description");
            else if (!t1.getDescription().equals(t2.getDescription()))
                fieldsThatDiffer.add("description");
        }

        if (t1.getRoleUuid() == null && t2.getRoleUuid() == null)
            ;
        else {
            if (t1.getRoleUuid() != null && t2.getRoleUuid() == null)
                fieldsThatDiffer.add("roleId");
            else if (t1.getRoleUuid() == null && t2.getRoleUuid() != null)
                fieldsThatDiffer.add("roleId");
            else if (!t1.getRoleUuid().equals(t2.getRoleUuid()))
                fieldsThatDiffer.add("roleId");
        }

        if (t1.isPrivateFlag() != t2.isPrivateFlag())
            fieldsThatDiffer.add("privateFlag");

        if (t1.getNote() == null && t2.getNote() == null)
            ;
        else {
            if (t1.getNote() != null && t2.getNote() == null)
                fieldsThatDiffer.add("note");
            else if (t1.getNote() == null && t2.getNote() != null)
                fieldsThatDiffer.add("note");
            else if (!t1.getNote().equals(t2.getNote()))
                fieldsThatDiffer.add("note");
        }

        if (t1.getReminderMinBefore() != t2.getReminderMinBefore())
            fieldsThatDiffer.add("reminderMinBefore");

        if (t1.getDueDate() == null && t2.getDueDate() == null)
            ;
        else {
            if (t1.getDueDate() != null && t2.getDueDate() == null)
                fieldsThatDiffer.add("dueDate");
            else if (t1.getDueDate() == null && t2.getDueDate() != null)
                fieldsThatDiffer.add("dueDate");
            else if (!t1.getDueDate().equals(t2.getDueDate()))
                fieldsThatDiffer.add("dueDate");
        }

//order conflicts are ignored
//        if (t1.getOrder() != t2.getOrder())
//            fieldsThatDiffer.add("order");

        if (t1.getPriority() != t2.getPriority())
            fieldsThatDiffer.add("priority");

        if (t1.getStatus() != t2.getStatus())
            fieldsThatDiffer.add("status");

        if (t1.getRfc2445() == null && t2.getRfc2445() == null)
            ;
        else {
            if (t1.getRfc2445() != null && t2.getRfc2445() == null)
                fieldsThatDiffer.add("rfc2445");
            else if (t1.getRfc2445() == null && t2.getRfc2445() != null)
                fieldsThatDiffer.add("rfc2445");
            else if (!t1.getRfc2445().equals(t2.getRfc2445()))
                fieldsThatDiffer.add("rfc2445");
        }

        return fieldsThatDiffer;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("taskId: ").append(getId());
        if (getUuid() != null && getUuid().length() > 20) {
            buf.append(", uuid: ").append(getUuid().substring(0, 10)).append("...").append(getUuid().substring(getUuid().length()-10));
        } else {
            buf.append(", uuid: ").append(getUuid());
        }
        buf.append(", priority: ").append(getPriority());
        buf.append(", roleId: ").append(roleId);
        if (getRoleUuid() != null && getRoleUuid().length() > 20) {
            buf.append(", roleUuid: ").append(getRoleUuid().substring(0, 10)).append("...").append(getRoleUuid().substring(getRoleUuid().length()-10));
        } else {
            buf.append(", roleUuid: ").append(getRoleUuid());
        }
        //buf.append(", remindMinBefore: ").append(reminderMinBefore);
        buf.append(", private: ").append(privateFlag);
        buf.append(", status: ").append(getStatus());
        buf.append(", completedDate: ").append(getCompletedDate());
        buf.append(", delegateEmail: ").append(delegateEmail);
        buf.append(", dueDate: ").append(getDueDate());
        buf.append("\n").append(super.toString());
        buf.append("\n\tdesc: ").append(description);
        buf.append("\n\tnote: ").append(note);
        if (rfc2445 != null) {
            buf.append("\n\t").append(rfc2445);
        }

        return buf.toString();
    }

}
