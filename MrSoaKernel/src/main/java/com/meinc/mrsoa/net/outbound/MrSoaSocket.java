package com.meinc.mrsoa.net.outbound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This subclass exists only to preserve the remoteAddress regardless of whether
 * the connection to the remote address has terminated.
 * 
 * @author Matt
 */
public class MrSoaSocket extends Socket {
  private static int counter;
  private InetSocketAddress remoteAddress;
  private int socketId;
  
  public MrSoaSocket(InetSocketAddress address, int timeoutMs) throws IOException {
    super();
    connect(address, timeoutMs);
    this.remoteAddress = address;
    socketId = ++counter;
  }

  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  public String toString() {
    return super.toString() + "<"+socketId+">";
  }
}
