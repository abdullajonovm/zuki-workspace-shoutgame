package com.meinc.ergo.util;

import java.util.Date;
import java.util.LinkedList;

import com.meinc.mrsoa.distdata.core.DistributedMap;

public class SamplesOverTimeCounter {
//    private static Logger logger = Logger.getLogger(SamplesOverTimeCounter.class);
    
    private int minTimeRangeSec;
    private int maxTimeRangeSec;
    private DistributedMap<Integer,LinkedList<Date>> samples = DistributedMap.getMap("samples_over_time_counter");

    public SamplesOverTimeCounter(String distributedName, int minTimeRangeSec, int maxTimeRangeSec) {
        this.minTimeRangeSec = minTimeRangeSec;
        this.maxTimeRangeSec = maxTimeRangeSec;
    }
    
    public void addSample(int subscriberId) {
        samples.lock(subscriberId);
        try {
            LinkedList<Date> subscriberDateList = samples.remove(subscriberId);
            if (subscriberDateList == null)
                subscriberDateList = new LinkedList<Date>();
            subscriberDateList.addLast(new Date());
            trim(subscriberDateList);
            samples.put(subscriberId, subscriberDateList);
        } finally {
            samples.unlock(subscriberId);
        }
    }
    
    public void clear(int subscriberId) {
        samples.lock(subscriberId);
        try {
            LinkedList<Date> subscriberDateList = samples.remove(subscriberId);
            if (subscriberDateList != null) {
                subscriberDateList.clear();
                samples.put(subscriberId, subscriberDateList);
            }
        } finally {
            samples.unlock(subscriberId);
        }
    }
    
    private float getSamplesPerMinute(LinkedList<Date> subscriberDateList) {
        if (subscriberDateList == null || subscriberDateList.isEmpty())
            return 0f;
        long now = new Date().getTime();
        long deltaMs = now - subscriberDateList.peekFirst().getTime();
        return (deltaMs <= minTimeRangeSec * 1000) ? 0f : 60f * 1000 * subscriberDateList.size() / deltaMs;
    }
    
    public float getSamplesPerMinute(int subscriberId) {
        samples.lock(subscriberId);
        try {
            LinkedList<Date> subscriberDateList = samples.remove(subscriberId);
            if (subscriberDateList == null)
                subscriberDateList = new LinkedList<Date>();
            trim(subscriberDateList);
            float spm = getSamplesPerMinute(subscriberDateList);
            samples.put(subscriberId, subscriberDateList);
            return spm;
        } finally {
            samples.unlock(subscriberId);
        }
    }
    
    private void trim(LinkedList<Date> subscriberDateList) {
        long now = new Date().getTime();
        while (subscriberDateList != null && !subscriberDateList.isEmpty() &&
                now - subscriberDateList.peekFirst().getTime() > maxTimeRangeSec * 1000)
            subscriberDateList.removeFirst();
    }
}
