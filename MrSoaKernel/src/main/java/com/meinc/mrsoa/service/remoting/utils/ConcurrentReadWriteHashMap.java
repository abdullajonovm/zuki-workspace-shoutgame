package com.meinc.mrsoa.service.remoting.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extends {@link HashMap} via composition to provide read-write level
 * concurrency safety.
 * 
 * @author Matt
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentReadWriteHashMap<K,V> implements Map<K,V> {
  private HashMap<K,V> delegate;

  public ConcurrentReadWriteHashMap(int initialCapacity, float loadFactor) {
    delegate = new HashMap<K,V>(initialCapacity, loadFactor);
  }

  public ConcurrentReadWriteHashMap(int initialCapacity) {
    delegate = new HashMap<K,V>(initialCapacity);
  }

  public ConcurrentReadWriteHashMap() {
    delegate = new HashMap<K,V>();
  }

  public ConcurrentReadWriteHashMap(Map<? extends K, ? extends V> m) {
    delegate = new HashMap<K,V>(m);
  }

  public synchronized void clear() {
    delegate.clear();
  }

  public synchronized Object clone() {
    return delegate.clone();
  }

  public synchronized boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  public synchronized boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  public synchronized Set<Entry<K, V>> entrySet() {
    return new ImmutableSet<Entry<K,V>>(delegate.entrySet());
  }

  public synchronized boolean equals(Object o) {
    return delegate.equals(o);
  }

  public synchronized V get(Object key) {
    return delegate.get(key);
  }

  public synchronized int hashCode() {
    return delegate.hashCode();
  }

  public synchronized boolean isEmpty() {
    return delegate.isEmpty();
  }

  public synchronized Set<K> keySet() {
    return new ImmutableSet<K>(delegate.keySet());
  }

  public synchronized V put(K key, V value) {
    return delegate.put(key, value);
  }

  public synchronized void putAll(Map<? extends K, ? extends V> m) {
    delegate.putAll(m);
  }

  public synchronized V remove(Object key) {
    return delegate.remove(key);
  }

  public synchronized int size() {
    return delegate.size();
  }

  public synchronized String toString() {
    return delegate.toString();
  }

  public synchronized Collection<V> values() {
    return new ImmutableCollection<V>(delegate.values());
  }
}
