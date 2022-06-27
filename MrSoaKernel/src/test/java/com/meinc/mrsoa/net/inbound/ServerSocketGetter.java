package com.meinc.mrsoa.net.inbound;

import java.net.ServerSocket;
import java.net.Socket;

import com.meinc.mrsoa.net.GlobalServerSocket;

public class ServerSocketGetter extends Thread {
  private String failure;
  private Socket serverSocket;
  public String getFailure() {
    return failure;
  }
  public Socket getServerSocket() {
    Socket s = serverSocket;
    serverSocket = null;
    return s;
  }
  public synchronized void run() {
    ServerSocket ssocket = null;
    try {
      ssocket = GlobalServerSocket.ssocket;
      serverSocket = ssocket.accept();
    } catch (Exception e) {
      e.printStackTrace();
      failure = e.getMessage();
    }
  }
}
