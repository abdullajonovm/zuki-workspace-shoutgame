package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteSyncData 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<String> deleted = new ArrayList<String>();
    private List<Note> notes = new ArrayList<Note>();
    
    public List<String> getDeleted()
    {
        return deleted;
    }
    public void setDeleted(List<String> deleted)
    {
        this.deleted = deleted;
    }
    public void addDeleted(String deletedNoteId)
    {
        this.deleted.add(deletedNoteId);
    }
    public List<Note> getNotes()
    {
        return notes;
    }
    public void setNotes(List<Note> notes)
    {
        this.notes = notes;
    }
    public void addNote(Note modifiedOrNewNote)
    {
        this.notes.add(modifiedOrNewNote);
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("NOTES:");
        for (Note note : notes) {
            buf.append("\n\t").append(note);
        }
        buf.append("\nDELETED:");
        for (String sid : deleted) {
            buf.append("\n\t").append(sid);
        }

        return buf.toString();
    }
}
