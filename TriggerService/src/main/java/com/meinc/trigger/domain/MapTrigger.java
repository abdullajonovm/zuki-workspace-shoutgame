package com.meinc.trigger.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapTrigger extends Trigger {
    private static final long serialVersionUID = 1L;

    private Map<String,Object> map = new HashMap<String,Object>();

    public MapTrigger(String key) {
        super(key, new HashMap<String,Object>());
//        setKey(key);
//        setPayload(map);
    }
    
    public Map<String,Object> getMap() {
        return map;
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<Entry<String,Object>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends String,? extends Object> m) {
        map.putAll(m);
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<Object> values() {
        return map.values();
    }
    
    @Override
    public String toString() {
        return "SyncMessage["+getKey()+"]="+map.toString();
    }
}
