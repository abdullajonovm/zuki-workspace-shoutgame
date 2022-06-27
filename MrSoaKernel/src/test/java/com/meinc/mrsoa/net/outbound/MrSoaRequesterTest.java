package com.meinc.mrsoa.net.outbound;


import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.monitor.MrSoaServerMonitor;
import com.meinc.mrsoa.monitor.MrSoaServer;
import com.meinc.mrsoa.monitor.LocalServerMonitor;
import com.meinc.mrsoa.net.GlobalServerSocket;
import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.inbound.MrSoaRequest;
import com.meinc.mrsoa.net.inbound.MrSoaResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;

public class MrSoaRequesterTest {

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
  public void testGetBestServer() throws UnknownHostException, InterruptedException {
    ServiceEndpoint serviceEndpoint = new ServiceEndpoint("default", "remoteService", "1.0");
    MockMrSoaMonitor mrsoaMonitor = new MockMrSoaMonitor();
    
    MockMrSoaServer remoteServer = new MockMrSoaServer(new InetSocketAddress(Inet4Address.getByName("1.1.1.1"), 9118));
    remoteServer.setResponsiveScore(30);
    
    mrsoaMonitor.registerRemoteService(serviceEndpoint, remoteServer);
    mrsoaMonitor.registerLocalService(serviceEndpoint);
    mrsoaMonitor.serverScoreUpdated(25);
    
    MockMrSoaRequester requester = new MockMrSoaRequester(mrsoaMonitor);
    InetSocketAddress bestServer = requester.getBestServerForEndpoint(serviceEndpoint);
    
    assertEquals(remoteServer.getNetAddress(), bestServer);
    
    mrsoaMonitor.serverScoreUpdated(35);
    bestServer = requester.getBestServerForEndpoint(serviceEndpoint);
    
    assertEquals(remoteServer.getNetAddress(), bestServer);
    
    // Wait for the best server record to become stale
    MrSoaRequester.setServerScoreTimeoutMillis(1000);
    Thread.sleep(1100);
    bestServer = requester.getBestServerForEndpoint(serviceEndpoint);
    
    assertEquals(mrsoaMonitor.getLocalServer().getNetAddress(), bestServer);
  }
  
  private Throwable requesterFailure;
  
  @Test
  public void testSimpleRequester() throws UnknownHostException, IOException, InterruptedException {
    Thread mockReceiver = new Thread() {
      public void run() {
        Socket socket = null;
        try {
          ServerSocket ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          
          int command = TcpHelper.readCommand(socket, 0);
          if (command != TcpHelper.REQUEST)
            throw new Exception("Unexpected command: " + command);
          
          MrSoaRequest request = (MrSoaRequest) TcpHelper.readPayload(socket, getClass().getClassLoader());
          if (!request.destination.equals("[default][s1][1.0]"))
            throw new Exception("Unexpected request destination: " + request.destination);
          if (request.callStack != null)
            throw new Exception("Callstack is supposed to be null");
          
          Object[] payload = (Object[]) TcpHelper.readPayload(socket, getClass().getClassLoader());
          String message = (String) payload[0];
          if (!"foobar".equals(message))
            throw new Exception("Unexpected payload: " + message);
          
          TcpHelper.sendCommand(socket, TcpHelper.RESPONSE);
          TcpHelper.sendPayload(socket, new MrSoaResponse());
          TcpHelper.sendPayload(socket, "bizbaz");
          
        } catch (Throwable e) {
          e.printStackTrace();
          requesterFailure = e;
        } finally {
          if (socket != null) {
            TcpHelper.closeConnection(socket);
          }
        }
      }
    };
    mockReceiver.start();

    try {
      ServiceEndpoint endpoint = new ServiceEndpoint("default", "s1", "1.0");
      
      MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
      MrSoaServer server = new MrSoaServer(new InetSocketAddress(Inet4Address.getByName("localhost"), 9118));
      monitor.registerService(endpoint, server);
      
      MrSoaRequester requester = MrSoaRequester.getInstance();
      String result = (String) requester.invokeMethod(endpoint, "m1", false, false, "foobar");
      assertEquals("bizbaz", result);
      
      StringWriter strWriter = new StringWriter();
      PrintWriter exWriter = new PrintWriter(strWriter);
      
      if (requesterFailure != null)
        requesterFailure.printStackTrace(exWriter);
      assertNull(strWriter.toString(), requesterFailure);
      
    } finally {
      mockReceiver.join();
    }
  }

  @Test
  public void testSynAckRequester() throws UnknownHostException, IOException, InterruptedException {
    Thread mockReceiver = new Thread() {
      public void run() {
        Socket socket = null;
        try {
          ServerSocket ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          while (true) {
            int command = TcpHelper.readCommand(socket, 0);
  
            MrSoaRequest request = (MrSoaRequest) TcpHelper.readPayload(socket, getClass().getClassLoader());
            Object[] payload = (Object[]) TcpHelper.readPayload(socket, getClass().getClassLoader());
            int waitMillis = (Integer) payload[0];
            boolean sendAck = (Boolean) payload[1];
            boolean lastCommand = (Boolean) payload[2];
            
            command = TcpHelper.readCommand(socket, waitMillis);
            if (command != TcpHelper.SYN)
              throw new Exception("Unexpected command: " + command);
            
            if (sendAck)
              TcpHelper.sendCommand(socket, TcpHelper.ACK);
            
            TcpHelper.sendCommand(socket, TcpHelper.RESPONSE);
            TcpHelper.sendPayload(socket, new MrSoaResponse());
            TcpHelper.sendPayload(socket, "bizbaz");
            
            if (lastCommand) break;
          }
        } catch (Throwable e) {
          e.printStackTrace();
          requesterFailure = e;
        } finally {
          if (socket != null) {
            TcpHelper.closeConnection(socket);
          }
        }
      }
    };
    mockReceiver.start();

    try {
      ServiceEndpoint endpoint = new ServiceEndpoint("default", "s1", "1.0");
  
      MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
      MrSoaServer server = new MrSoaServer(new InetSocketAddress(Inet4Address.getByName("localhost"), 9118));
      monitor.registerService(endpoint, server);
  
      MrSoaRequester requester = MrSoaRequester.getInstance();
      MrSoaRequester.setMessageSynIntervalMillis(1000);
      String result = (String) requester.invokeMethod(endpoint, "m1", false, false, 1500, true, false);
      assertEquals("bizbaz", result);
  
      result = (String) requester.invokeMethod(endpoint, "m1", false, false, 1500, false, true);
      assertEquals("bizbaz", result);
      MrSoaRequester.setMessageSynIntervalMillis(10000);
      
      mockReceiver.join(1000);
      Assert.assertFalse("Receiver should have died", mockReceiver.isAlive());
      
      StringWriter strWriter = new StringWriter();
      PrintWriter exWriter = new PrintWriter(strWriter);
  
      if (requesterFailure != null)
        requesterFailure.printStackTrace(exWriter);
      assertNull(strWriter.toString(), requesterFailure);
      
    } finally {
      mockReceiver.join();
    }
  }
  
  @Test
  public void testExceptionRequester() throws UnknownHostException, IOException, InterruptedException {
    Thread mockReceiver = new Thread() {
      public void run() {
        Socket socket = null;
        try {
          ServerSocket ssocket = GlobalServerSocket.ssocket;
          socket = ssocket.accept();
          int command = TcpHelper.readCommand(socket, 0);

          MrSoaRequest request = (MrSoaRequest) TcpHelper.readPayload(socket, getClass().getClassLoader());
          Object[] payload = (Object[]) TcpHelper.readPayload(socket, getClass().getClassLoader());

          MrSoaResponse response = new MrSoaResponse();
          response.isException = true;

          TcpHelper.sendCommand(socket, TcpHelper.RESPONSE);
          TcpHelper.sendPayload(socket, response);
          TcpHelper.sendPayload(socket, new ArrayList());
          TcpHelper.sendPayload(socket, new RuntimeException("bizbaz"));

        } catch (Throwable e) {
          e.printStackTrace();
          requesterFailure = e;
        } finally {
          if (socket != null) {
            TcpHelper.closeConnection(socket);
          }
        }
      }
    };
    mockReceiver.start();

    try {
      ServiceEndpoint endpoint = new ServiceEndpoint("default", "s1", "1.0");
  
      MrSoaServerMonitor monitor = MrSoaServerMonitor.getInstance();
      MrSoaServer server = new MrSoaServer(new InetSocketAddress(Inet4Address.getByName("localhost"), 9118));
      monitor.registerService(endpoint, server);
  
      MrSoaRequester requester = MrSoaRequester.getInstance();
      MrSoaRequester.setMessageSynIntervalMillis(1000);
      try {
        String result = (String) requester.invokeMethod(endpoint, "m1", false, false, 1500, true, false);
        fail("Method invocation should have thrown exception");
      } catch (RuntimeException e) {
        assertEquals("bizbaz", e.getMessage());
      }
      
      StringWriter strWriter = new StringWriter();
      PrintWriter exWriter = new PrintWriter(strWriter);
  
      if (requesterFailure != null)
        requesterFailure.printStackTrace(exWriter);
      assertNull(strWriter.toString(), requesterFailure);
      
    } finally {
      mockReceiver.join();
    }
  }
}

class MockMrSoaRequester extends MrSoaRequester {
  MockMrSoaRequester(MrSoaServerMonitor monitor) {
    super(monitor);
  }

  protected InetSocketAddress getBestServerForEndpoint(ServiceEndpoint endpoint) {
    return super.getBestServerForEndpoint(endpoint.toString()).address;
  }
}

class MockMrSoaServer extends MrSoaServer {
  public MockMrSoaServer(InetSocketAddress netAddress) {
    super(netAddress);
  }
  protected synchronized void setResponsiveScore(int responsiveScore) {
    super.setResponsiveScore(responsiveScore);
  }
}

class MockMrSoaMonitor extends MrSoaServerMonitor {
  public MockMrSoaMonitor() {
    super(LocalServerMonitor.getInstance());
  }

  protected void registerRemoteService(ServiceEndpoint endpoint, MrSoaServer server) {
    super.registerRemoteService(endpoint, server);
  }

  protected MrSoaServer getLocalServer() {
    return super.getLocalServer();
  }

  public void serverScoreUpdated(int score) {
    super.serverScoreUpdated(score);
  }
}
