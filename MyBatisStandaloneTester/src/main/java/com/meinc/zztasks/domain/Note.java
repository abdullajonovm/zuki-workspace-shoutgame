package com.meinc.zztasks.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Note
extends BaseEntityObject
{
    private static final long serialVersionUID = 1L;
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("^.*(\\n|\\r)?");

    private String note;

    private boolean privateFlag; //optional

    @JsonIgnore
    private int roleId; //optional

    @JsonProperty("roleId")
    private String roleUuid; //optional

    private List<String> tagIds;

    @JsonProperty("noteId")
    @Override
    public String getUuid()
    {
        return uuid;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    @JsonIgnore
    public String getSubject()
    {
        //bx (16 Apr 2013): Take first line of text of the note and make it the subject
        String subject = null;
        if (note != null) {
            Matcher m = SUBJECT_PATTERN.matcher(note);
            if (m.find()) {
                subject = m.group();
            }
        }
        return subject;
    }

    public boolean isPrivateFlag()
    {
        return privateFlag;
    }

    public void setPrivateFlag(boolean privateFlag)
    {
        this.privateFlag = privateFlag;
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

    public List<String> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<String> tagIds)
    {
        this.tagIds = tagIds;
    }

    @JsonCreator
    public static Note fromString(String escapedJson)
    {
        Note w = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            w = mapper.readValue(escapedJson, Note.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return w;
    }

    @JsonIgnore
    public static Note getTestStaleNote()
    {
        Note n = new Note();
        n.setNote("stale note");
        return n;
    }

    @JsonIgnore
    public static List<String> getFieldNamesThatDiffer(Note n1, Note n2)
    {
        List<String> fieldsThatDiffer = new ArrayList<String>();

        if (n1.getNote() == null && n2.getNote() == null)
            ;
        else {
            if (n1.getNote() != null && n2.getNote() == null)
                fieldsThatDiffer.add("note");
            else if (n1.getNote() == null && n2.getNote() != null)
                fieldsThatDiffer.add("note");
            else if (!n1.getNote().equals(n2.getNote()))
                fieldsThatDiffer.add("note");
        }

        if (n1.isPrivateFlag() != n2.isPrivateFlag())
            fieldsThatDiffer.add("privateFlag");

        if (n1.getRoleUuid() == null && n2.getRoleUuid() == null)
            ;
        else {
            if (n1.getRoleUuid() != null && n2.getRoleUuid() == null)
                fieldsThatDiffer.add("roleId");
            else if (n1.getRoleUuid() == null && n2.getRoleUuid() != null)
                fieldsThatDiffer.add("roleId");
            else if (!n1.getRoleUuid().equals(n2.getRoleUuid()))
                fieldsThatDiffer.add("roleId");
        }

//order conflicts are ignored
//        if (n1.getOrder() != n2.getOrder())
//            fieldsThatDiffer.add("order");

        return fieldsThatDiffer;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("noteId: ").append(getId());
        buf.append(", uuid: ").append(getUuid());
        buf.append(", note: ").append(note);
        buf.append(", private: ").append(privateFlag);
        buf.append(", roleId: ").append(roleId);
        buf.append(", roleUuid: ").append(roleUuid);
        buf.append(super.toString());
        if (tagIds != null && tagIds.size() > 0) {
            boolean first = true;
            buf.append("\n\tTAGS: [");
            for (String s: tagIds) {
                if (!first) buf.append(",");
                first = false;
                buf.append(s);
            }
            buf.append("]");
        }

        return buf.toString();
    }

}
