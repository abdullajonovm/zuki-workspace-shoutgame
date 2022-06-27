package com.meinc.ergo.util;

public class TimeService
{
    public static long getNow()
    {
        //TODO: use an NTP time source
        return System.currentTimeMillis();
    }
}
