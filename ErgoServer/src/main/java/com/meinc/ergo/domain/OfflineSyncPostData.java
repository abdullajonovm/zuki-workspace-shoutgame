package com.meinc.ergo.domain;

import java.util.List;

import com.meinc.ergo.util.ServiceHelper.ERROR_TYPE;

public class OfflineSyncPostData
{
    public ERROR_TYPE ERROR_TYPE;
    public String providerId;
    public List<Operation> opsSinceLastSync;
    public String batchId;
    public String transactionId;
}
