package com.meinc.mrsoa.service;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;


public class ServiceEndpointTest {
  @Test
  public void testEndpointEquality() {
    ServiceEndpoint ep1 = new ServiceEndpoint();
    ep1.setServiceName("hello");
    ServiceEndpoint ep2 = new ServiceEndpoint();
    ep2.setServiceName("hello");
    assertTrue(ep1.equals(ep2));
    assertTrue(ep2.equals(ep1));
    
    ep1.setNamespace("default");
    assertTrue(ep1.equals(ep2));
    assertTrue(ep2.equals(ep1));
    
    ep2.setNamespace("default");
    assertTrue(ep1.equals(ep2));
    assertTrue(ep2.equals(ep1));
    
    ep1.setServiceName("goodbye");
    assertFalse(ep1.equals(ep2));
    assertFalse(ep2.equals(ep1));
    
    ep1.setNamespace("foo");
    ep1.setServiceName("hello");
    assertFalse(ep1.equals(ep2));
    assertFalse(ep2.equals(ep1));
    
    ep1.setNamespace(null);
    ep2.setVersion("1.0");
    assertFalse(ep1.equals(ep2));
    assertFalse(ep2.equals(ep1));
  }
}
