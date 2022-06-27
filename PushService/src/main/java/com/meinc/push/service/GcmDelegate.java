package com.meinc.push.service;


public interface GcmDelegate
{
    public void messageSentGcm(String deviceId, String messageId);
    public void updateDeviceTokenGcm(String oldDeviceId, String newDeviceId);
    public void messageSendFailedGcm(String deviceId);
    public void serverExceptionGcm(String deviceId, String error);
}
