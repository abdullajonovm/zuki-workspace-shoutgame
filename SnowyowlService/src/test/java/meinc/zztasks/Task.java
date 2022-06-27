package meinc.zztasks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

public class Task
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum PRIORITY {BIGROCK, A, B, C}
    public enum STATUS {NORMAL, INPROGRESS, DELEGATED, COMPLETE}

    public int taskId;
    public String taskUuid;
    public int subscriberId;
    public boolean prvate;
    public String description;
    public String note;
    public Date dueDate;
    public PRIORITY priority;
    public Integer roleId;
    public String roleUuid;
    public Integer reminderMinutesBefore;
    public boolean reminded;
    public Date reminder;
    public STATUS status;
    public Date completedDate;
    public String delegateEmail;
    public Date recurringStartDate;
    public boolean recurringRegenerativeFlag;
    public String recurringRRule;
    public boolean nextTaskCreated;
    public int order;
    public int priorityOrder;
    public int roleOrder;
    public String timezone;
    public Date createDate;
    public Date updateDate;
    public Date deleteDate;

    public String googleServerId;
    public String unconvertedErgoStatus;
    public String unconvertedErgoPriority;

    @Override
    public String toString()
    {
        String desc;
        if (description == null) {
            desc = "<null>";
        } else if (description.length() < 20) {
            desc = description;
        } else {
            desc = description.substring(0,20) + " ...";
        }

        String nte;
        if (note == null) {
            nte = "<null>";
        } else if (note.length() < 20) {
            nte = note;
        } else {
            nte = note.substring(0,20) + " ...";
        }

        return MessageFormat.format(
            "private: {0}, desc: {1}, note: {2}, duedate: {3}, priority: {4}, roleId: {5}, reminderMins: {6}, reminded: {7}, reminder: {8}, status: {9}, completed: {10}, " +
            "delegateEmail: {11}, recurringStart: {12}, regenerative: {13}, rrule: {14}, nextCreated: {15}, order: {16}, porder: {17}, rorder: {18}, timezone: {19}, " +
            "created: {20}, updated: {21}, deleted: {22}",
            prvate,
            desc,
            nte,
            dueDate, priority, roleId, reminderMinutesBefore, reminded, reminder, status, completedDate,
            delegateEmail, recurringStartDate, recurringRegenerativeFlag, recurringRRule, nextTaskCreated, order, priorityOrder, roleOrder, timezone,
            createDate, updateDate, deleteDate
        );
    }
}
