package com.meinc.mrsoa.net.inbound;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MrSoaReceiverTest {

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
  
  private String testReceiverFailure;
  private String testRequesterFailure;
  private volatile int respondCount;
  private volatile int netEventCount;
  
  @Test
  public void testReceivers() throws InterruptedException, IOException {
    final MrSoaCommandListener listener = new MrSoaCommandListener("Mock Listener") {
      protected void closing() { }
      protected boolean processCommand(SelectionKey key, int command) {return false;}
      protected void resetting() { }
      public void netEvent() {
        netEventCount += 1;
      }
    };
    
    MrSoaResponderFactory responderFactory = new MrSoaResponderFactory();
    
    MrSoaResponderPool responderPool =
        new MrSoaResponderPool(new GenericObjectPool(responderFactory), responderFactory) {
      public void listenTo(SelectionKey key) {
        try {
          respondCount += 1;
          key.attach(listener);
          key.interestOps(SelectionKey.OP_READ);
        } catch (Throwable e) {
          testReceiverFailure = e.getMessage();
        }
      }
    };
    
    MrSoaReceiver receiver = new MrSoaReceiver(responderPool, new InetSocketAddress(InetAddress.getLocalHost(), 9117));
    
    RequesterThread[] requesters = new RequesterThread[3];
    for (int i = 0; i < 3; i++) {
      requesters[i] = new RequesterThread();
      requesters[i].start();
    }
    
    for (int i = 0; i < 3; i++) {
      try {
        requesters[i].join();
      } catch (InterruptedException e) {
        fail(e.getMessage());
      }
    }
    
    receiver.shutdown();
    receiver.join(3000);
    if (receiver.isAlive())
      fail("Could not stop receiver");
    
    assertNull("Receiver failed", testReceiverFailure);
    assertNull("Requester failed", testRequesterFailure);
    assertEquals(30, respondCount);
    assertEquals(30, netEventCount);
  }
  
  private class RequesterThread extends Thread {
    public void run() {
      try {
        for (int i = 0; i < 10; i++) {
          Socket socket = new Socket(InetAddress.getLocalHost(), 9117);
          OutputStream netOut = socket.getOutputStream();
          netOut.write(1);
          netOut.flush();
          socket.close();
        }
      } catch (Throwable e) {
        testRequesterFailure = e.getMessage();
      }
    }
  }
}
