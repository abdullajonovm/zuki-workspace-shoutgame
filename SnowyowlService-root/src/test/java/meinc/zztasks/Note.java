package meinc.zztasks;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class Note
{
    public int noteId;
    public String noteUuid;
    public int subscriberId;
    public String note;
    public boolean prvate;
    public Integer roleId;
    public String roleUuid;
    public int order;
    public List<Integer> tagIds;
    public Date createDate;
    public Date updateDate;
    public Date deleteDate;

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "id: {0,number,#}, note: {1}, order: {2,number,#}, private: {3}, roleId: {4}, createDate: {5}, updateDate: {6}, deleteDate: {7}, tagIds: {8}",
            noteId, note, order, prvate, roleId, createDate, updateDate, deleteDate, tagIds);
    }

}
