package com.meinc.mrsoa.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioSelector extends Thread {

  public static NioSelector singleton;
  public static Throwable selectorError;
  public static Selector selector;
  public static ServerSocketChannel serverChannel;
  
  private static List<INioSelectorListener> listeners = new ArrayList<INioSelectorListener>();
  
  static {
    try {
      serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), 9119));
      
      selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
      
      singleton = new NioSelector();
      singleton.start();

    } catch (IOException e) {
      e.printStackTrace();
      selectorError = e;
    }
  }

  public static void registerListener(INioSelectorListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public static void unregisterListener(INioSelectorListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }
  
  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
          SelectionKey key = keysIt.next();
          keysIt.remove();
          
          if (key.isAcceptable()) {
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);

            synchronized (listeners) {
              for (INioSelectorListener listener : listeners)
                listener.doAccept(key);
            }
            continue;
          }
          
          if (key.isReadable()) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            synchronized (listeners) {
              for (INioSelectorListener listener : listeners)
                listener.doRead(key);
            }
            continue;
          }
  
          if (key.isWritable()) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            synchronized (listeners) {
              for (INioSelectorListener listener : listeners)
                listener.doWrite(key);
            }
            continue;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      selectorError = e;
      return;
    }
  }
}
