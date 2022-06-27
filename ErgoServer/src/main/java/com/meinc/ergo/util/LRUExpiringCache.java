package com.meinc.ergo.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

//http://littletechsecrets.wordpress.com/2008/11/16/simple-lru-cache-in-java/
public class LRUExpiringCache<K, V> 
{
    private int mMaxEntries;
    private long mTTL;
    private LinkedHashMap<K, ExpiringEntry<V>> mMap;
    
    public LRUExpiringCache()
    {
        this(10000, 1000*60*60); //10000 entries, with an hour ttl
    }
    
    public LRUExpiringCache(int maxEntries, long ttl)
    {
        mMaxEntries = maxEntries;
        mTTL = ttl;
        
        // removeEldestEntry() is called after a put(). To allow maxEntries in
        // cache, capacity should be maxEntries + 1 (for the entry which will be
        // removed). Load factor is taken as 1 because size is fixed. This is
        // less space efficient when very less entries are present, but there
        // will be no effect on time complexity for get(). The third parameter
        // in the base class constructor says that this map is
        // insertion-order oriented.
        mMap = new LinkedHashMap<K, ExpiringEntry<V>>(maxEntries + 1, 1, false) {
            private static final long serialVersionUID = 1L;
            protected boolean removeEldestEntry(Entry<K,ExpiringEntry<V>> eldest) {
                // After size exceeds max entries, this statement returns true and the
                // oldest value will be removed. Behaves like a queue, the first
                // inserted value will go away.
                return size() > mMaxEntries;
            };
        };
    }
    
    public void put(K key, V value)
    {
        ExpiringEntry<V> entry = new ExpiringEntry<V>(value);
        mMap.put(key, entry);
    }
    
    public V get(K key)
    {
        ExpiringEntry<V> entry = mMap.get(key);
        
        //it doesn't exist
        if (entry == null) 
            return null;
        
        //it has expired
        else if (entry.getTimestamp() + mTTL < System.currentTimeMillis()) {
            mMap.remove(key);
            return null;
        }
        
        else
            return entry.getObject();
    }
    
    public void remove(K key)
    {
        mMap.remove(key);
    }
    
    class ExpiringEntry<T>
    {
        private long _timestamp;
        private T _object;
        
        public ExpiringEntry(T object)
        {
            _timestamp = System.currentTimeMillis();
            _object = object;
        }
        
        public long getTimestamp() { return _timestamp; }
        
        public T getObject() { return _object; }
    }
    
//    public static void main(String[] args)
//    {
//        try {
//            LRUExpiringCache<String, String> cache = new LRUExpiringCache<String, String>(5, 2000);
//            
//            //put in 10 entries (5 of them _should_ be removed)
//            System.out.println("adding 10 items...");
//            for (int i=0; i<10; i++) {
//                cache.put(i+"", i+"");
//            }
//            
//            //test that only 5 entries exist
//            System.out.println("only 5 of the items should be in the list (5-9)");
//            for (int i=0; i<10; i++) {
//                String val = cache.get(i+"");
//                System.out.println("K: " + i + ", val: " + val);
//            }
//            
//            System.out.println("====================");
//            
//            //test that the entries expire after 2 seconds
//            Thread.sleep(2100);
//            System.out.println("all entries should be expired at this point...");
//            for (int i=0; i<10; i++) {
//                String val = cache.get(i+"");
//                System.out.println("K: " + i + ", val: " + val);
//            }
//            
//            System.out.println("====================");
//            
//            //put in 2 entries, remove one of them, make sure only 1 entry exists
//            System.out.println("adding two entries...");
//            cache.put("one", "one");
//            cache.put("two", "two");
//            System.out.println("one: " + cache.get("one"));
//            System.out.println("two: " + cache.get("two"));
//            System.out.println("removing 'two'...");
//            cache.remove("two");
//            System.out.println("two: " + cache.get("two"));
//            
//            System.out.println("DONE");
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }
    
}
