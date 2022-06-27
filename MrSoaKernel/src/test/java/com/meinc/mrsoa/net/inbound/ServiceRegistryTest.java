package com.meinc.mrsoa.net.inbound;


import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.service.ServiceEndpoint;

public class ServiceRegistryTest {

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
  public void testBadForward() {
    MrSoaRequest req = new MrSoaRequest();
    req.destination = "A";
    try {
      IMrSoaService service = LocalServiceRegistry.getService(req);
      fail("Service should not exist");
    } catch (MrSoaServiceNotFoundException e) {
      // success
    } catch (Throwable e) {
      fail(e.getMessage());
    }
  }
  
  private boolean goodForward;
  
  @Test
  public void testRegisterUnregister() throws Throwable {
    IMrSoaService s1 = new IMrSoaService() {
      public Object invokeMethod(MrSoaRequest request, Object[] args) {
        goodForward = true;
        return null;
      }
      public boolean isStarted() { return false; }
      public boolean isStopped() { return false; }
      public List<ServiceEndpoint> getOnStopDependencies() { return null; }
      public void start() throws Exception { }
      public void stop() throws Exception { }
      public ServiceEndpoint getEndpoint() { return null; }
    };
    
    LocalServiceRegistry.registerService("A", s1);
    
    MrSoaRequest req = new MrSoaRequest();
    req.destination = "A";
    IMrSoaService service = LocalServiceRegistry.getService(req);
    service.invokeMethod(req, null);
    
    assertTrue("Forward did not succeed", goodForward);
    
    LocalServiceRegistry.deregisterService("A");
    
    try {
      service = LocalServiceRegistry.getService(req);
      fail("Service should not exist");
    } catch (MrSoaServiceNotFoundException e) {
      // success
    } catch (Throwable e) {
      fail(e.getMessage());
    }
  }
}
