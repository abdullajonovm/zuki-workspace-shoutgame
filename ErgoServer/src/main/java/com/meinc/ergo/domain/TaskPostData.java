package com.meinc.ergo.domain;

import com.meinc.ergo.util.ServiceHelper.ERROR_TYPE;

public class TaskPostData
{
    public String providerId;
    public Task task;
    public boolean failFlag;
    public ERROR_TYPE ERROR_TYPE;
    public String batchId;
    public boolean notify = true;
}
