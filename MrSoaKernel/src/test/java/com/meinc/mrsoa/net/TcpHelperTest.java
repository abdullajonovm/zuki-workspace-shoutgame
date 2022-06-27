package com.meinc.mrsoa.net;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TcpHelperTest {

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
  
  private volatile String testTxRxFailure;
  
  @Test
  public void testSimpleTxRx() throws IOException, ClassNotFoundException, InterruptedException {
    Thread rx = new Thread() {
      public void run() {
        ServerSocket ssocket = null;
        Socket socket = null;
        try {
          ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          int command = TcpHelper.readCommand(socket, 3000);
          if (command != 99) {
            testTxRxFailure = "Received command " + command;
            TcpHelper.closeConnection(socket);
          }
          String payload = (String) TcpHelper.readPayload(socket, getClass().getClassLoader());
          if (!"foobar".equals(payload)) {
            testTxRxFailure = "Payload '"+payload+"' did not match 'foobar'";
          }
          TcpHelper.sendCommand(socket, 98);
          TcpHelper.sendPayload(socket, "bizbaz");
        } catch (Exception e) {
          e.printStackTrace();
          testTxRxFailure = e.getMessage();
        } finally {
          if (socket != null)
            TcpHelper.closeConnection(socket);
        }
      }
    };
    
    rx.start();
    
    Socket socket = new Socket(InetAddress.getLocalHost(), 9118);
    TcpHelper.sendCommand(socket, 99);
    TcpHelper.sendPayload(socket, "foobar");
    
    assertNull(testTxRxFailure, testTxRxFailure);
    
    int command = TcpHelper.readCommand(socket, 3000);
    assertEquals(98, command);
    
    String payload = (String) TcpHelper.readPayload(socket, getClass().getClassLoader());
    
    TcpHelper.closeConnection(socket);
    
    assertEquals("bizbaz", payload);
    assertNull(testTxRxFailure, testTxRxFailure);
    
    rx.join();
  }
  
  @Test
  public void testClassLoaderTxRx()
  throws ClassNotFoundException, InstantiationException, IllegalAccessException,
      SecurityException, NoSuchMethodException, IllegalArgumentException,
      InvocationTargetException, UnknownHostException, IOException, InterruptedException {
    File hiddenDir = new File("src/test/resources/");
    final URLClassLoader hiddenLoader = new URLClassLoader(new URL[] {hiddenDir.toURI().toURL()});
    
    Class<?> hiddenClass = hiddenLoader.loadClass("hidden.Hidden");
    Object hiddenObject = hiddenClass.newInstance();
    hiddenClass.getMethod("setMessage", String.class).invoke(hiddenObject, "flimflam");
    
    Thread rx = new Thread() {
      public void run() {
        ServerSocket ssocket = null;
        Socket socket = null;
        try {
          ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          int command = TcpHelper.readCommand(socket, 3000);
          if (command != 99) {
            testTxRxFailure = "Received command " + command;
            TcpHelper.closeConnection(socket);
          }
          Object hiddenObject = TcpHelper.readPayload(socket, hiddenLoader);
          String message = (String) hiddenObject.getClass().getMethod("getMessage").invoke(hiddenObject);
          if (!"flimflam".equals(message)) {
            testTxRxFailure = "Payload '"+message+"' did not match 'flimflam'";
          }
          hiddenObject.getClass().getMethod("setMessage", String.class).invoke(hiddenObject, "shimsham");
          TcpHelper.sendCommand(socket, 98);
          TcpHelper.sendPayload(socket, hiddenObject);
        } catch (Exception e) {
          e.printStackTrace();
          testTxRxFailure = e.getMessage();
        } finally {
          if (socket != null)
            TcpHelper.closeConnection(socket);
        }
      }
    };
    
    rx.start();
    
    Socket socket = new Socket(InetAddress.getLocalHost(), 9118);
    TcpHelper.sendCommand(socket, 99);
    TcpHelper.sendPayload(socket, hiddenObject);
    
    assertNull(testTxRxFailure, testTxRxFailure);
    
    int command = TcpHelper.readCommand(socket, 3000);
    assertEquals(98, command);
    
    Object payload = TcpHelper.readPayload(socket, hiddenLoader);
    
    TcpHelper.closeConnection(socket);

    assertNull(testTxRxFailure, testTxRxFailure);

    String message = (String) payload.getClass().getMethod("getMessage").invoke(payload);
    assertEquals("shimsham", message);
    
    rx.join();
  }
  
  @Test
  public void testErrorTx() throws UnknownHostException, IOException,
                          ClassNotFoundException, InterruptedException {
    Thread rx = new Thread() {
      public void run() {
        ServerSocket ssocket = null;
        Socket socket = null;
        try {
          ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          int command = TcpHelper.readCommand(socket, 3000);
          if (command != 99) {
            testTxRxFailure = "Received command " + command;
            TcpHelper.closeConnection(socket);
          }
          String payload = (String) TcpHelper.readPayload(socket, getClass().getClassLoader());
          if (!"foobar".equals(payload)) {
            testTxRxFailure = "Payload '"+payload+"' did not match 'foobar'";
          }
          TcpHelper.sendCommand(socket, 98);
          TcpHelper.sendPayload(socket, "bizbaz");
        } catch (Exception e) {
          testTxRxFailure = "Error: " + e.getMessage();
        } finally {
          if (socket != null)
            TcpHelper.closeConnection(socket);
        }
      }
    };
    
    rx.start();
    
    Socket socket = new Socket(InetAddress.getLocalHost(), 9118);
    TcpHelper.sendCommand(socket, 99);
    assertNull(testTxRxFailure, testTxRxFailure);
    
    TcpHelper.closeConnection(socket);
    
    rx.join();
    
    assertNotNull("An exception should have been thrown", testTxRxFailure);
  }
}
