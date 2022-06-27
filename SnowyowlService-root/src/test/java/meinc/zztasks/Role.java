package meinc.zztasks;

import java.text.MessageFormat;
import java.util.Date;

public class Role
{
    public int roleId;
    public String roleUuid;
    public int subscriberId;
    public String name;
    public String icon;
    public String color;
    public int order;
    public Date createDate;
    public Date updateDate;
    public Date deleteDate;

    public String googleServerId;

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "id: {0,number,#}, name: {1}, order: {2,number,#}, icon: {3}, color: {4}, createDate: {5}, updateDate: {6}, deleteDate: {7}",
            roleId, name, order, icon, color, createDate, updateDate, deleteDate);
    }
}
