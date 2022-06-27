package com.meinc.mrsoa.service.remoting.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

//TODO: Investigate whether Collections.unmodifiable* can replace this implementation
public class ImmutableSet<E> implements Set<E> {
  private Set<E> delegate;
  
  ImmutableSet(Set<E> delegate) {
    this.delegate = delegate;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean add(E e) {
    //return delegate.add(e);
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean addAll(Collection<? extends E> c) {
    //return delegate.addAll(c);
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public void clear() {
    //delegate.clear();
    throw new UnsupportedOperationException();
  }

  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  public int hashCode() {
    return delegate.hashCode();
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public Iterator<E> iterator() {
    return new ImmutableIterator<E>(delegate.iterator());
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean remove(Object o) {
    //return delegate.remove(o);
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean removeAll(Collection<?> c) {
    //return delegate.removeAll(c);
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean retainAll(Collection<?> c) {
    //return delegate.retainAll(c);
    throw new UnsupportedOperationException();
  }

  public int size() {
    return delegate.size();
  }

  public Object[] toArray() {
    return delegate.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

}
