package tv.shout.util;

import java.util.LinkedHashMap;

public class MaxSizeHashMap<K, V>
extends LinkedHashMap<K, V>
{
    private static final long serialVersionUID = 1L;
    private int _maxSize = 1024;

    public MaxSizeHashMap<K, V> withMaxSize(int maxSize)
    {
        _maxSize = maxSize;
        return this;
    }

    public int getMaxSize()
    {
        return _maxSize;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest)
    {
        return size() > _maxSize;
    }

}
