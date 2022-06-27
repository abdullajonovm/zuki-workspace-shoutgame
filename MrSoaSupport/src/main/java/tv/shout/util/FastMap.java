package tv.shout.util;
import java.util.HashMap;
import java.util.Map;

public class FastMap<K,V> extends HashMap<K,V> {
    private static final long serialVersionUID = 1L;
    
    public FastMap() {
        super();
    }

    public FastMap(Object...keyValPairs) {
        super(keyValPairs.length / 2);
        if (keyValPairs.length % 2 != 0)
            throw new IllegalArgumentException("constructor parameters must be key value pairs");
        for (int i = 0; i < keyValPairs.length; i+=2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValPairs[i];
            @SuppressWarnings("unchecked")
            V val = (V) keyValPairs[i+1];
            put(key, val);
        }
    }

    public FastMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public FastMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FastMap(Map<? extends K,? extends V> m) {
        super(m);
    }

}