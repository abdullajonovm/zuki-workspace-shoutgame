package com.shawker.queue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DistributedMap<K,V> {
    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap<String,DistributedMap> mapsMap = new ConcurrentHashMap<>();
    private static ReentrantLock mapsLock = new ReentrantLock();

    private Map<Integer, Integer> keyLockMap = new HashMap<>();

    public boolean tryLock(K key)
    {
        if (key == null) throw new NullPointerException();

        mapsLock.lock();
        try {
            int hashCode = key.hashCode();
            Integer currentLockCount = keyLockMap.get(hashCode);
            if (currentLockCount == null) {
                keyLockMap.put(hashCode, 1);
                return true;
            } else {
                return false;
            }

        } finally {
            mapsLock.unlock();
        }
    }

    public boolean tryPut(K key, V value, long timeoutMs)
    {
        if (key == null) throw new NullPointerException();

        mapsLock.lock();
        try {
            if (!tryLock(key)) return false;

            put(key, value);
            return true;

        } finally {
            mapsLock.unlock();
        }
    }

    public void lock(K key)
    {
        if (key == null) throw new NullPointerException();

        mapsLock.lock();
        try {
            tryLock(key);

        } finally {
            mapsLock.unlock();
        }
    }

    public void unlock(K key)
    {
        if (key == null) throw new NullPointerException();

        mapsLock.lock();
        try {
            int hashCode = key.hashCode();
            Integer currentLockCount = keyLockMap.get(hashCode);
            if (currentLockCount == null) {
                return;
            } else {
                currentLockCount--;
                if (currentLockCount == 0) {
                    keyLockMap.remove(hashCode);
                } else {
                    keyLockMap.put(hashCode, currentLockCount);
                }
            }

        } finally {
            mapsLock.unlock();
        }
    }

    public static <KT,VT> DistributedMap<KT,VT> getMap(String mapName) {
        mapsLock.lock();
        try {
            @SuppressWarnings("unchecked")
            DistributedMap<KT,VT> map = mapsMap.get(mapName);
            if (map == null) {
                map = new DistributedMap<>();
                mapsMap.put(mapName, map);
            }
            return map;
        } finally {
            mapsLock.unlock();
        }
    }

    private ConcurrentHashMap<K,V> inner;

    private DistributedMap() {
        inner = new ConcurrentHashMap<>();
    }

    public V get(K key) {
        return inner.get(key);
    }

    public V put(K key, V val) {
        return inner.put(key, val);
    }

    public V remove(K key) {
        return inner.remove(key);
    }

    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public Collection<V> values() {
        return inner.values();
    }

    public boolean containsKey(K key)
    {
        return inner.containsKey(key);
    }

    public int size()
    {
        return inner.size();
    }

}
