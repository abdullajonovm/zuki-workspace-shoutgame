package com.meinc.mrsoa.net;


import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.net.inbound.IResettable;
import com.meinc.mrsoa.net.inbound.NetEventNotifier;

public class TcpHelperNioTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testIoStreamToNio() throws IOException {
    InputStream fis = getClass().getResourceAsStream("/com/meinc/mrsoa/net/Winter.jpg");
    BufferedInputStream bfis = new BufferedInputStream(fis);
    
    SelectorListener listener = new SelectorListener();
    NioSelector.registerListener(listener);
    
    try {
      Socket socket = new Socket(InetAddress.getLocalHost(), 9119);
      OutputStream netOut = socket.getOutputStream();
      
      byte[] picBytes = new byte[4096];
      int bytesRead = 0;
      while (true) {
        bytesRead = bfis.read(picBytes);
        if (bytesRead == -1) break;
        netOut.write(picBytes, 0, bytesRead);
        if (SelectorListener.listenerFailure != null) {
          fail(SelectorListener.listenerFailure.getMessage());
          break;
        }
      }
      
      bfis.close();
      socket.close();
      
      try {
        listener.join(3000);
      } catch (InterruptedException e) { }
      assertFalse(listener.isAlive());
      
      StringWriter strWriter = new StringWriter();
      PrintWriter exWriter = new PrintWriter(strWriter);
      
      if (SelectorListener.listenerFailure != null)
        SelectorListener.listenerFailure.printStackTrace(exWriter);
      assertNull(strWriter.toString(), SelectorListener.listenerFailure);
      
    } finally {
      NioSelector.unregisterListener(listener);
    }
  }
}

class SelectorListener extends Thread implements INioSelectorListener, IResettable {
  public static Throwable listenerFailure;
  public NetEventNotifier notifier;
  public ByteBuffer buffer;
  public SelectionKey key;
  private BufferedInputStream bfis;

  public SelectorListener() {
    notifier = new NetEventNotifier(this, 0);
    buffer = ByteBuffer.allocateDirect(4096);
    TcpHelperNio.initBufferForFirstRead(buffer);
    
    InputStream fis = getClass().getResourceAsStream("/com/meinc/mrsoa/net/Winter.jpg");
    bfis = new BufferedInputStream(fis);
    
    start();
  }
  
  @Override
  public void doAccept(SelectionKey key) {
    
  }

  @Override
  public void doRead(SelectionKey key) {
    if (key.attachment() == null) {
      key.attach(this);
      synchronized (this) {
        this.key = key;
      }
    }
    notifier.netEvent();
  }

  @Override
  public void doWrite(SelectionKey key) {
  }

  @Override
  public void run() {
    notifier.start();
    try {
      while (!isInterrupted()) {
        synchronized (this) {
          if (key == null)
            wait();
        }
        
        int netByte = TcpHelperNio.readByte(key, buffer, this, 0);
        int picByte = bfis.read();
        if (netByte != picByte)
          throw new IOException("Net byte " + netByte + " does not match pic byte " + picByte);
        else if (netByte == -1)
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
      listenerFailure = e;
      return;
    } catch (InterruptedException e) {
      e.printStackTrace();
      listenerFailure = e;
      return;
    } finally {
      notifier.interrupt();
    }
  }

  @Override
  public boolean isReset() {
    return false;
  }
}