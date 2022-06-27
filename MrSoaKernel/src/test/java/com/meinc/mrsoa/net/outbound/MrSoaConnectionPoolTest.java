package com.meinc.mrsoa.net.outbound;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.TcpHelperNio;
import com.meinc.mrsoa.net.inbound.IMrSoaService;
import com.meinc.mrsoa.net.inbound.LocalServiceRegistry;
import com.meinc.mrsoa.net.inbound.MrSoaReceiver;
import com.meinc.mrsoa.net.inbound.MrSoaRequest;
import com.meinc.mrsoa.net.inbound.MrSoaResponderPool;
import com.meinc.mrsoa.net.inbound.MrSoaResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;

public class MrSoaConnectionPoolTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String msg =
       "*************************************************************\n" +
       "* Three hostname aliases are required to execute this test: *\n" +
       "*    1. mrsoa1 -> 127.0.0.1                                 *\n" +
       "*    2. mrsoa2 -> 127.0.0.1                                 *\n" +
       "*    3. mrsoa3 -> 127.0.0.1                                 *\n" +
       "*************************************************************\n";
    System.out.println(msg);
    
    for (int i = 1; i <= 3; i++) {
      LocalServiceRegistry.registerService("mrsoa"+i, new MockService());
    }
    
    MrSoaResponderPool responderPool = MrSoaResponderPool.getInstance();
    MrSoaReceiver receiver = new MrSoaReceiver(responderPool, new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 9119));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    for (int i = 1; i <= 3; i++) {
      LocalServiceRegistry.deregisterService("mrsoa"+i);
    }
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSimpleBorrowReturn() throws IOException {
    Socket conn1a = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa1"), 9119));
    Socket conn1b = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa1"), 9119));
    
    assertNotSame(conn1a, conn1b);
    
    MrSoaConnectionPool.returnConnection(conn1a);
    MrSoaConnectionPool.returnConnection(conn1b);
    
    Socket conn1c = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa1"), 9119));
    Socket conn1d = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa1"), 9119));
    
    assertTrue("Connections should have been recycled",
        (conn1c == conn1a || conn1c == conn1b) && 
        (conn1d != conn1c && (conn1d == conn1a || conn1d == conn1b)));
    
    MrSoaConnectionPool.returnConnection(conn1c);
    MrSoaConnectionPool.returnConnection(conn1d);
  }
  
  @Test
  public void testConnectionDurability() throws IOException, ClassNotFoundException {
    Socket[] conns = new Socket[3];
    conns[0] = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa1"), 9119));
    conns[1] = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa2"), 9119));
    conns[2] = MrSoaConnectionPool.borrowConnection(new InetSocketAddress(Inet4Address.getByName("mrsoa3"), 9119));
    
    MrSoaRequest r = new MrSoaRequest();
    for (int i = 1; i <= 3; i++) {
      Socket socket = conns[i-1];
      r.methodName = "mrsoa"+i;
      r.destination = "mrsoa"+i;
      
      TcpHelper.sendCommand(socket, TcpHelperNio.REQUEST);
      TcpHelper.sendPayload(socket, r);
      
      int command = TcpHelper.readCommand(socket, 3000);
      assertEquals(TcpHelperNio.RESPONSE, command);
      
      MrSoaResponse response = (MrSoaResponse) TcpHelper.readPayload(socket,
          getClass().getClassLoader());
      
      Object result = TcpHelper.readPayload(socket, getClass().getClassLoader());
      assertEquals(r.methodName, result.toString());
    }
    
    for (int i = 0; i < 3; i++) {
      MrSoaConnectionPool.returnConnection(conns[i]);
    }
  }
}

class MockService implements IMrSoaService {
  private String name;
  public Object invokeMethod(MrSoaRequest request, Object[] args) {
    if (name == null)
      name = request.methodName;
    return name;
  }
  public boolean isStarted() { return false; }
  public boolean isStopped() { return false; }
  public List<ServiceEndpoint> getOnStopDependencies() { return null; }
  public void start() throws Exception { }
  public void stop() throws Exception { }
  public ServiceEndpoint getEndpoint() { return null; }
}
