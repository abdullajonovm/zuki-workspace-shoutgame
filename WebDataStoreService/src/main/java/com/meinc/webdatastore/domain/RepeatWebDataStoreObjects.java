package com.meinc.webdatastore.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ensure that a set of {@link RepeatWebDataStoreObject} objects share the same data. This is especially important when
 * serializing to avoid serializing the data multiple times
 */
public class RepeatWebDataStoreObjects implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] data;
    private List<RepeatWebDataStoreObject> repeatObjects = new ArrayList<RepeatWebDataStoreObject>();
    
    public byte[] getData() {
        return data;
    }

    public synchronized List<RepeatWebDataStoreObject> getRepeatObjects() {
        for (RepeatWebDataStoreObject repeatObject : repeatObjects)
            repeatObject.setData(data);
        return repeatObjects;
    }

    public synchronized void addRepeatObject(RepeatWebDataStoreObject repeatObject) {
        RepeatWebDataStoreObject copy = new RepeatWebDataStoreObject(repeatObject);
        if (copy.getData() != null) {
            data = copy.getData();
            copy.setData(null);
        }
        repeatObjects.add(copy);
    }
}
