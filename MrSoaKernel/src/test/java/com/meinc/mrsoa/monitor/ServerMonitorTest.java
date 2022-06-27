package com.meinc.mrsoa.monitor;


import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerMonitorTest {

  private static LocalServerMonitor serverMonitor;
  private static MockMrSoaMonitor mrsoaMonitor;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    serverMonitor = LocalServerMonitor.getInstance();
    serverMonitor.setMonitorIntervalSeconds(1);
    serverMonitor.setScoreDeltaBeforeUpdate(0);
    mrsoaMonitor = new MockMrSoaMonitor(serverMonitor);
    serverMonitor.registerMonitorListener(mrsoaMonitor);
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    serverMonitor.setMonitorIntervalSeconds(3);
    serverMonitor.setScoreDeltaBeforeUpdate(5);
    serverMonitor.unregisterMonitorListener(mrsoaMonitor);
  }

  @Before
  public void setUp() throws Exception {
    mrsoaMonitor.localScore = -1;
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCpuMonitoring() throws InterruptedException {
    while (mrsoaMonitor.localScore == -1)
      Thread.sleep(100);
    int score1 = mrsoaMonitor.localScore;
    CpuGobbler.gobble();
    int lastUpdateCount = mrsoaMonitor.updateCount;
    while (mrsoaMonitor.updateCount == lastUpdateCount)
      Thread.sleep(100);
    int score2 = mrsoaMonitor.localScore;
    CpuGobbler.die();
    Assert.assertTrue("Monitor did not notice increase in CPU usage", score2 < score1);
  }
  
  @Test
  public void testMemMonitoring() throws InterruptedException {
    while (mrsoaMonitor.localScore == -1)
      Thread.sleep(100);
    int score1 = mrsoaMonitor.localScore;
    MemGobbler.gobble();
    int lastUpdateCount = mrsoaMonitor.updateCount;
    while (mrsoaMonitor.updateCount == lastUpdateCount)
      Thread.sleep(100);
    int score2 = mrsoaMonitor.localScore;
    MemGobbler.release();
    Assert.assertTrue("Monitor did not notice increase in MEM usage", score2 < score1);
  }
}

class MockMrSoaMonitor extends MrSoaServerMonitor {
  volatile int localScore;
  volatile int updateCount;
  
  protected MockMrSoaMonitor(LocalServerMonitor serverMonitor) {
    super(serverMonitor);
  }

  @Override
  public void serverScoreUpdated(int score) {
    localScore = score;
    updateCount += 1;
  }
}
