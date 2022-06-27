package com.meinc.mrsoa.net;

import java.nio.channels.SelectionKey;

public interface INioSelectorListener {
  public void doAccept(SelectionKey key);
  public void doRead(SelectionKey key);
  public void doWrite(SelectionKey key);
}
