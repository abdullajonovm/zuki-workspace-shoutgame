package com.meinc.mrsoa.net.inbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.net.INioSelectorListener;
import com.meinc.mrsoa.net.NioSelector;
import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.TcpHelperNio;

public class AckerTest {

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

  //@Test
  public void testSimpleAcker()
  throws UnknownHostException, IOException, InterruptedException {
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(16);
    ByteBuffer sendBuffer = ByteBuffer.allocateDirect(16);
    TcpHelperNio.initBufferForFirstRead(readBuffer);
    
    final Acker acker = new Acker(readBuffer, sendBuffer) {
      protected void resetting() {
        super.resetting();
        listenerKey.attach(null);
      }
    };

    INioSelectorListener listener = new INioSelectorListener() {
      public void doAccept(SelectionKey key) {
      }
      public void doRead(SelectionKey key) {
        if (key.attachment() == null) {
          acker.listenTo(key);
          key.attach(acker);
        } else {
          acker.netEventBlocking();
        }
      }
      public void doWrite(SelectionKey key) {
        acker.netEventBlocking();
      }
    };
    
    NioSelector.registerListener(listener);
    
    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 9119));

      for (int i = 0; i < 3; i++) {
        TcpHelper.sendCommand(socket, TcpHelper.SYN);
        int command = TcpHelper.readCommand(socket, 3000);
        assertEquals(TcpHelperNio.ACK, command);
      }
    } finally {
      acker.shutdown();
      acker.join(3000);
      
      TcpHelper.closeConnection(socket);
      NioSelector.unregisterListener(listener);
      
      assertFalse("Acker should have died", acker.isAlive());
    }
    
    assertNull(NioSelector.selectorError);
  }

  @Test
  public void testAckerError()
  throws UnknownHostException, IOException, InterruptedException {
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(16);
    ByteBuffer sendBuffer = ByteBuffer.allocateDirect(16);
    TcpHelperNio.initBufferForFirstRead(readBuffer);
    
    final Acker acker = new Acker(readBuffer, sendBuffer) {
      protected void resetting() {
        super.resetting();
        listenerKey.attach(null);
      }
    };

    INioSelectorListener listener = new INioSelectorListener() {
      public void doAccept(SelectionKey key) {
      }
      public void doRead(SelectionKey key) {
        if (key.attachment() == null) {
          acker.listenTo(key);
          key.attach(acker);
        } else {
          acker.netEventBlocking();
        }
      }
      public void doWrite(SelectionKey key) {
        acker.netEventBlocking();
      }
    };
    
    NioSelector.registerListener(listener);
    
    Socket socket = new Socket();
    
    try {
      socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 9119));
      TcpHelper.sendCommand(socket, TcpHelper.REQUEST);
      try {
        int command = TcpHelper.readCommand(socket, 3000);
        assertEquals(TcpHelper.SERVER_FATAL, command);
        String error = (String) TcpHelper.readPayload(socket, getClass().getClassLoader());
        assertEquals("Illegal command: 1", error);
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
      
      assertTrue("Socket should have closed", socket.isConnected());
    } finally {
      acker.shutdown();
      acker.join(3000);
      
      NioSelector.unregisterListener(listener);
      
      assertFalse("Acker should have died", acker.isAlive());
    }
    
    assertNull(NioSelector.selectorError);
  }

  //@Test
  public void testSequentialAcker()
  throws UnknownHostException, IOException, InterruptedException {
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(16);
    ByteBuffer sendBuffer = ByteBuffer.allocateDirect(16);
    TcpHelperNio.initBufferForFirstRead(readBuffer);
    
    final Acker acker = new Acker(readBuffer, sendBuffer) {
      protected void resetting() {
        super.resetting();
        listenerKey.attach(null);
      }
    };

    INioSelectorListener listener = new INioSelectorListener() {
      public void doAccept(SelectionKey key) {
      }
      public void doRead(SelectionKey key) {
        if (key.attachment() == null || acker.isStarted()) {
          acker.listenTo(key);
          key.attach(acker);
        } else {
          acker.netEventBlocking();
        }
      }
      public void doWrite(SelectionKey key) {
        acker.netEventBlocking();
      }
    };
    
    NioSelector.registerListener(listener);
    
    Socket socket = new Socket();
    
    try {
      socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 9119));
      
      for (int i = 0; i < 3; i++) {
        TcpHelper.sendCommand(socket, TcpHelper.SYN);
        int command = TcpHelper.readCommand(socket, 3000);
        assertEquals(TcpHelper.ACK, command);
        
        acker.reset();
      }
    } finally {
      TcpHelper.closeConnection(socket);

      acker.shutdown();
      acker.join(3000);
      
      NioSelector.unregisterListener(listener);
      
      assertFalse("Acker should have died", acker.isAlive());
    }
    
    assertNull(NioSelector.selectorError);
  }
}

