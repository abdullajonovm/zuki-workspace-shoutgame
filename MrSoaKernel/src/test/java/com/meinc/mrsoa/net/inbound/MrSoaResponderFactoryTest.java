package com.meinc.mrsoa.net.inbound;


import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MrSoaResponderFactoryTest {

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
  public void testFactory() {
    MrSoaResponderFactory factory = new MrSoaResponderFactory();
    MrSoaResponderPool pool = new MrSoaResponderPool(new GenericObjectPool(factory), factory);
    
    try {
      MrSoaResponder responder = (MrSoaResponder) factory.makeObject();
      assertNotNull(responder);
      assertTrue("Responder is dead", responder.isAlive());
      
      factory.destroyObject(responder);
      responder.join(3000);
      assertFalse("Responder is alive", responder.isAlive());
      
      assertFalse("Responder is active", factory.validateObject(responder));
      
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
