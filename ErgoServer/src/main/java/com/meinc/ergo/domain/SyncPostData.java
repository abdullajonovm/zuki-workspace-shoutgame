package com.meinc.ergo.domain;

import com.meinc.ergo.util.ServiceHelper.ERROR_TYPE;
import com.meinc.ergo.web.service.SyncService.SYNCTYPE;

public class SyncPostData
{
    public ERROR_TYPE ERROR_TYPE;
    public String providerId;
    public long lastSyncTime;
    public SYNCTYPE syncType = SYNCTYPE.ALL;
    public boolean immediate;
    public String batchId;
}
