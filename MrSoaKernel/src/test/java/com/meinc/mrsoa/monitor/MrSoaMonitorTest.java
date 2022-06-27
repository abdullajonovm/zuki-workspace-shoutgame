package com.meinc.mrsoa.monitor;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meinc.mrsoa.monitor.gauge.AverageServerGauge;
import com.meinc.mrsoa.monitor.gauge.FastServerGauge;
import com.meinc.mrsoa.monitor.gauge.ServerLoadGauge;
import com.meinc.mrsoa.monitor.gauge.SlowServerGauge;
import com.meinc.mrsoa.net.LocalServerSettings;
import com.meinc.mrsoa.service.ServiceEndpoint;

public class MrSoaMonitorTest {

  private static final int STICKY_SCORE = 50;
  
  private static MrSoaServer server1;
  private static MrSoaServer server2;
  private static MrSoaServer server3;

  private static ServiceEndpoint ep1;
  private static ServiceEndpoint ep2;
  private static ServiceEndpoint ep3;
  private static ServiceEndpoint ep4;

  private static MrSoaServerMonitor monitor;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    server1 = new MrSoaServer(new InetSocketAddress(Inet4Address.getByAddress(new byte[]{1,1,1,1}), LocalServerSettings.getLocalServerPort()));
    server2 = new MrSoaServer(new InetSocketAddress(Inet4Address.getByAddress(new byte[]{2,2,2,2}), LocalServerSettings.getLocalServerPort()));
    server3 = new MrSoaServer(new InetSocketAddress(Inet4Address.getByAddress(new byte[]{3,3,3,3}), LocalServerSettings.getLocalServerPort()));
    
    monitor = new MrSoaServerMonitor(LocalServerMonitor.getInstance());
    monitor.setLocalStickyScore(STICKY_SCORE);
    
    server1.setResponsiveScore(STICKY_SCORE-20);
    server2.setResponsiveScore(STICKY_SCORE-10);
    server3.setResponsiveScore(STICKY_SCORE+10);
    
    ep1 = new ServiceEndpoint("default", "service1", "1.0");
    ep2 = new ServiceEndpoint("default", "service2", "1.0");
    ep3 = new ServiceEndpoint("default", "service3", "1.0");
    ep4 = new ServiceEndpoint("default", "service4", "1.0");
    
    monitor.registerRemoteService(ep1, server1);
    monitor.registerRemoteService(ep2, server2);
    monitor.registerRemoteService(ep3, server3);
    
    monitor.registerRemoteService(ep4, server1);
    monitor.registerRemoteService(ep4, server2);
    monitor.registerRemoteService(ep4, server3);
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
  public void testGetNetAddressToEndpoint() {
    assertEquals(server1.getNetAddress(), monitor.getNetAddressToEndpoint(ep1.toString()));
    assertEquals(server2.getNetAddress(), monitor.getNetAddressToEndpoint(ep2.toString()));
    assertEquals(server3.getNetAddress(), monitor.getNetAddressToEndpoint(ep3.toString()));
  }

  @Test
  public void testRegisterLocalService() throws InterruptedException {
    monitor.registerLocalService(ep4);
    InetSocketAddress netAddress = monitor.getNetAddressToEndpoint(ep4.toString());
    // By default, the local server will have a score of 100 and thus should be
    // the server returned
    assertEquals(monitor.getLocalServer().getNetAddress(), netAddress);
    
    monitor.serverScoreUpdated(STICKY_SCORE+5);
    netAddress = monitor.getNetAddressToEndpoint(ep4.toString());
    // Because the local server is greater than STICKY_SCORE, the local server
    // should be returned even though server3 is more responsive
    assertEquals(monitor.getLocalServer().getNetAddress(), netAddress);
    
    monitor.serverScoreUpdated(STICKY_SCORE-5);
    netAddress = monitor.getNetAddressToEndpoint(ep4.toString());
    // Now that the local server is below STICKY_SCORE, the best remote server
    // should be returned
    assertEquals(server3.getNetAddress(), netAddress);
}

  @Test
  public void testUnregisterLocalService() throws InterruptedException {
    monitor.serverScoreUpdated(STICKY_SCORE+5);
    monitor.unregisterLocalService(ep4);
    InetSocketAddress netAddress = monitor.getNetAddressToEndpoint(ep4.toString());
    assertNotSame(monitor.getLocalServer().getNetAddress(), netAddress);
  }
  
  @Test
  public void testServerGauges() {
    int s1 = testGauge(new SlowServerGauge(150));
    int s2 = testGauge(new AverageServerGauge(150));
    int s3 = testGauge(new FastServerGauge(150));
    
    assertTrue("Area under curve greater for slower gauge than faster gauge", s1 < s2);
    assertTrue("Area under curve greater for slower gauge than faster gauge", s2 < s3);
  }
  
  private int testGauge(ServerLoadGauge gauge) {
    int score = 0, lastScore = 1000, sum = 0;
    for (int i = 0; i <= 100; i+=10) {
      score = gauge.getResponsiveScoreForLoad(i);
      assertTrue("Score did not drop as expected", score < lastScore);
      assertFalse("Score of " +score+ " out of bounds", score < 0 || score > 100);
      sum += score;
      lastScore = score;
    }
    return sum;
  }
 
  //TODO test concurrent use of MrSoaServerMonitor
  @Test
  public void testConcurrentMonitor() {
  }
}
