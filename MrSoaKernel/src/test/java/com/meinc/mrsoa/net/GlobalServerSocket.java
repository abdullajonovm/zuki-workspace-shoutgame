package com.meinc.mrsoa.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Simply holds the server socket used by MrSOA in this JVM.
 * 
 * @author Matt
 */
public class GlobalServerSocket {
  public static ServerSocket ssocket;
  
  static {
    try {
      ssocket = new ServerSocket();
      ssocket.setReuseAddress(true);
      ssocket.setSoTimeout(1000);
      ssocket.bind(new InetSocketAddress(9118));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
