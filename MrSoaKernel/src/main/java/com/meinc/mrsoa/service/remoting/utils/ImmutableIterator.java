package com.meinc.mrsoa.service.remoting.utils;

import java.util.Iterator;

public class ImmutableIterator<E> implements Iterator<E> {
  private Iterator<E> delegate;

  public ImmutableIterator(Iterator<E> delegate) {
    this.delegate = delegate;
  }

  public boolean hasNext() {
    return delegate.hasNext();
  }

  public E next() {
    return delegate.next();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public void remove() {
    //delegate.remove();
    throw new UnsupportedOperationException();
  }
}
