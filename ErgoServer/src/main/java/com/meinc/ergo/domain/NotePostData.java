package com.meinc.ergo.domain;

import com.meinc.ergo.util.ServiceHelper.ERROR_TYPE;

public class NotePostData
{
    public String providerId;
    public Note note;
    public boolean failFlag;
    public ERROR_TYPE ERROR_TYPE;
    public String batchId;
}
