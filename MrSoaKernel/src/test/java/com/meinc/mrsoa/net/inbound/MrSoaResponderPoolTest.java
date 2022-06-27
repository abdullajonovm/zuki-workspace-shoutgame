package com.meinc.mrsoa.net.inbound;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.net.INioSelectorListener;
import com.meinc.mrsoa.net.NioSelector;
import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.TcpHelperNio;

public class MrSoaResponderPoolTest {

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
  public void testPool() throws UnknownHostException, IOException, InterruptedException {
    MockFactory factory = new MockFactory();
    final MrSoaResponderPool responderPool = new MrSoaResponderPool(new GenericObjectPool(factory), factory);

    ByteBuffer buffer = ByteBuffer.allocateDirect(16);
    TcpHelperNio.initBufferForFirstRead(buffer);
    
    INioSelectorListener listener = new INioSelectorListener() {
      public void doAccept(SelectionKey key) {
      }
      public void doRead(SelectionKey key) {
        if (key.attachment() == null) {
          responderPool.listenTo(key);
        } else {
          MrSoaResponder responder = (MrSoaResponder) key.attachment();
          responder.netEventBlocking();
        }
      }
      public void doWrite(SelectionKey key) {
      }
    };
    
    NioSelector.registerListener(listener);

    assertEquals(0, responderPool.getActiveResponderCount());
    
    Socket[] sockets = new Socket[10];
    for (int i = 0; i < 10; i++) {
      sockets[i] = new Socket(InetAddress.getLocalHost(), 9119);
      TcpHelper.sendCommand(sockets[i], TcpHelper.SYN);
    }
    
    assertEquals(10, responderPool.getActiveResponderCount());
    
    for (int i = 0; i < 5; i++) {
      TcpHelper.closeConnection(sockets[i]);
    }
    
    // Wait for the Responders to go idle. This is obviously error prone so if 
    // the following tests fail try increasing the sleep time.
    Thread.sleep(1000);
    assertEquals(5, responderPool.getActiveResponderCount());
    assertEquals(5, responderPool.getIdleResponderCount());
    
    for (MrSoaResponder responder : factory.responders) {
      responder.shutdown();
      responder.join();
    }
    
    assertEquals(0, responderPool.getActiveResponderCount());
    assertEquals(5, responderPool.getIdleResponderCount());
    
    responderPool.evictNow();
    assertTrue(responderPool.getIdleResponderCount() < 5);
    
    NioSelector.unregisterListener(listener);
    
    responderPool.close();
  }
  
  /*@Test
  public void testPoolAdjustments() throws Exception {
    MrSoaResponderFactory factory = new MrSoaResponderFactory();
    GenericObjectPool objectPool = new GenericObjectPool(factory);
    MrSoaResponderPool responderPool = new MrSoaResponderPool(objectPool, factory);
    
    responderPool.setAdjustAfterFirstBadScoreMillis(1000);
    responderPool.setShrinkPoolIntervalMillis(1000);
    responderPool.setGrowPoolIntervalMillis(1000);
    
    int maxActive0 = objectPool.getMaxActive();
    Object responder1 = objectPool.borrowObject();
    Object responder2 = objectPool.borrowObject();
    Object responder3 = objectPool.borrowObject();
    Object responder4 = objectPool.borrowObject();
    
    responderPool.serverScoreUpdated(10);
    Thread.sleep(1100);
    responderPool.serverScoreUpdated(10);
    assertEquals("Responder count should have been capped at 4", 4, objectPool.getMaxActive());

    responderPool.serverScoreUpdated(0);
    Thread.sleep(1100);
    responderPool.serverScoreUpdated(0);
    assertEquals("Responder count should have been decreased to 1", 1, objectPool.getMaxActive());
    
    responderPool.serverScoreUpdated(100);
    Thread.sleep(1100);
    responderPool.serverScoreUpdated(100);
    assertEquals("Responder count should have increased to 4", 4, objectPool.getMaxActive());
    
    objectPool.returnObject(responder1);
    objectPool.returnObject(responder2);
    objectPool.returnObject(responder3);
    objectPool.returnObject(responder4);
    
    responderPool.close();
  }*/
}

class MockFactory extends MrSoaResponderFactory {
  public ArrayList<MrSoaResponder> responders = new ArrayList<MrSoaResponder>();
  public Object makeObject() throws Exception {
    MrSoaResponder responder = (MrSoaResponder) super.makeObject();
    responders.add(responder);
    return responder;
  }
}