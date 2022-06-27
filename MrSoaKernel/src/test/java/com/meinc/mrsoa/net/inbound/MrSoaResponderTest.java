package com.meinc.mrsoa.net.inbound;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.service.ServiceEndpoint;

public class MrSoaResponderTest {

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

  static String mockReceiverFailure;
  static String mockServiceFailure;
  static Throwable mockRequesterFailure;

  @Test
  public void testResponder() throws InterruptedException, IOException {
    MockService[] services = new MockService[10];
    for (int i = 0; i < 10; i++) {
      services[i] = new MockService(i);
      LocalServiceRegistry.registerService(""+i, services[i]);
    }
    
    MockResponderFactory responderFactory = new MockResponderFactory();
    MockPool mockPool = new MockPool(responderFactory);
    MrSoaReceiver receiver = new MrSoaReceiver(mockPool, new InetSocketAddress(InetAddress.getLocalHost(), 9116));
    
    MockRequester[] requesters = new MockRequester[10];
    for (int i = 0; i < 1; i++) {
      requesters[i] = new MockRequester(i);
      requesters[i].start();
    }
    
    int dieTime = 6000;
    for (int i = 0; i < 1; i++) {
      try {
        long time0 = System.currentTimeMillis();
        requesters[i].join(dieTime);
        dieTime -= System.currentTimeMillis() - time0;
        if (dieTime <= 0) dieTime = 1;
        if (requesters[i].isAlive())
          fail("Requester " + i + " did not stop");
      } catch (InterruptedException e) {
        fail(e.getMessage());
      }
    }
    
    receiver.shutdown();
    receiver.join(1000);
    if (receiver.isAlive())
      fail("Receiver did not stop");
    
    for (MrSoaResponder responder : responderFactory.responders) {
      responder.shutdown();
    }
    
    for (MrSoaResponder responder : responderFactory.responders) {
      responder.join(1000);
      if (responder.isAlive())
        fail("Responder did not stop");
    }
    
    for (int i = 0; i < 10; i++) {
      LocalServiceRegistry.deregisterService(""+i);
    }

    StringWriter strWriter = new StringWriter();
    PrintWriter exWriter = new PrintWriter(strWriter);
    
    if (mockRequesterFailure != null)
    assertNull(strWriter.toString(), mockRequesterFailure);
    
    assertNull(mockReceiverFailure, mockReceiverFailure);
    assertNull(mockServiceFailure, mockServiceFailure);
    
    //assertEquals(1, MockPool.returnedCount);
  }
}

class MockRequester extends Thread {
  private int requesterNum;

  public MockRequester(int requesterNum) {
    this.requesterNum = requesterNum;
  }

  public void run() {
    try {
      Socket socket = new Socket(InetAddress.getLocalHost(), 9116);
      MrSoaRequest r = new MrSoaRequest();
      r.from = "MockRequester";
      for (int i = 0; i < 10; i++) {
        r.destination = ""+i;
        r.methodName = ""+i;
        
        TcpHelper.sendCommand(socket, TcpHelper.REQUEST);
        TcpHelper.sendPayload(socket, r);
        
        int responseCommand = TcpHelper.readCommand(socket, 3000);
        
        if (responseCommand != TcpHelper.RESPONSE) {
          MrSoaResponderTest.mockRequesterFailure = new Exception("Unexpected response command: "
              + responseCommand);
          return;
        }
        
        MrSoaResponse response = (MrSoaResponse) TcpHelper.readPayload(socket,
            getClass().getClassLoader());
        
        if (response.isException) {
          List<Object> flatException = (List<Object>) TcpHelper.readPayload(
              socket, getClass().getClassLoader());
          Throwable exception = (Throwable) TcpHelper.readPayload(socket,
              getClass().getClassLoader());
          MrSoaResponderTest.mockRequesterFailure = new Exception("Exception response: "
            + exception.getMessage());
          return;
        }
        
        Object result = TcpHelper.readPayload(socket, getClass().getClassLoader());
        int responseNum = (Integer) result;
        if (responseNum != i) {
          MrSoaResponderTest.mockRequesterFailure = new Exception("Unexpected response: "
              + responseNum);
          return;
        }
      }
      
      //Thread.sleep(1000);
      TcpHelper.closeConnection(socket);
      
    } catch (Throwable e) {
      MrSoaResponderTest.mockRequesterFailure = e;
    }
  }
}

class MockPool extends MrSoaResponderPool {
  MockPool(MrSoaResponderFactory responderFactory) {
    super(new GenericObjectPool(responderFactory), responderFactory);
  }

  static int returnedCount;

  @Override
  public void markIdle(MrSoaResponder responder) {
    returnedCount += 1;
  }
}

class MockResponderFactory extends MrSoaResponderFactory {
  public ArrayList<MrSoaResponder> responders = new ArrayList<MrSoaResponder>();
  
  @Override
  public Object makeObject() throws Exception {
    MrSoaResponder responder = (MrSoaResponder) super.makeObject();
    responders.add(responder);
    return responder;
  }
}

class MockService implements IMrSoaService {
  private int serviceNum;
  public MockService(int serviceNum) {
    this.serviceNum = serviceNum;
  }
  public Object invokeMethod(MrSoaRequest request, Object[] args) {
    int requestedServiceNum = Integer.parseInt(request.destination);
    if (!(""+serviceNum).equals(request.destination))
      MrSoaResponderTest.mockServiceFailure = "Destination does not match service name: " + request.destination;
    return requestedServiceNum;
  }
  public boolean isStarted() { return false; }
  public boolean isStopped() { return false; }
  public List<ServiceEndpoint> getOnStopDependencies() { return null; }
  public void start() throws Exception { }
  public void stop() throws Exception { }
  public ServiceEndpoint getEndpoint() { return null; }
}
