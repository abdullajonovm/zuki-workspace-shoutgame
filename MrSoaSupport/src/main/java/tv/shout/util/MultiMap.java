package tv.shout.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class MultiMap<K,V> {
    private Map<K,List<V>> map;

    public MultiMap() {
        map = new TreeMap<K,List<V>>();
    }
    
    public MultiMap(Comparator<K> comparator) {
        map = new TreeMap<K,List<V>>(comparator);
    }
    
    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        for (List<V> values : map.values())
            if (values.contains(value))
                return true;
        return false;
    }

    public Set<java.util.Map.Entry<K,List<V>>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public List<V> get(Object key) {
        return map.get(key);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public List<V> put(K key, V value) {
        List<V> values;
        if (map.containsKey(key)) {
            values = map.get(key);
        } else {
            values = new ArrayList<V>();
            map.put(key, values);
        }
        values.add(value);
        return values;
    }

    public void putAll(Map<? extends K,? extends V> m) {
        putAllUnchecked(m);
    }
    
    @SuppressWarnings("unchecked")
    private <PK,PV> void putAllUnchecked(Map<PK,PV> m) {
        Iterator<Entry<PK,PV>> entrySetIt = m.entrySet().iterator();
        while (entrySetIt.hasNext()) {
            Entry<PK,PV> entry = entrySetIt.next();
            put((K)entry.getKey(), (V)entry.getValue());
        }
    }

    public List<V> remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<List<V>> values() {
        return map.values();
    }
}