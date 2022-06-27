package com.meinc.mrsoa.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.monitor.gauge.ServerLoadGauge;
import com.meinc.mrsoa.net.LocalServerSettings;

/**
 * Monitors the local JVM and its resource consumption to generate a 
 * responsive score.
 * 
 * @see MrSoaServer#getResponsiveScore()
 * @author Matt
 */
public class LocalServerMonitor extends Thread {
  private static final Log log = LogFactory.getLog(LocalServerMonitor.class);
  
  private static LocalServerMonitor singleton = new LocalServerMonitor();
  
  public static LocalServerMonitor getInstance() {
    return singleton;
  }
  
  /**
   * The server gauge that best fits the horsepower of this server
   */
  private static ServerLoadGauge serverGauge;
  
  /**
   * Recalculate this server's responsive score every n seconds
   */
  private int monitorIntervalSeconds = 3;
  
  /**
   * Listeners will not be updated until the score changes by this amount
   */
  volatile private int scoreDeltaBeforeUpdate = 5;

  /**
   * The Runtime instance to use in this instance
   */
  private Runtime runtime = Runtime.getRuntime();
  
  /**
   * The last time a low resources warning was logged
   */
  private long lastMemoryWarningTime;
  
  /**
   * The value of the last CPU snapshot
   */
  private long lastTotalCpuTime;
  
  /**
   * The last time a CPU snapshot was taken
   */
  private long lastTimeOfCpuCalculation;
  
  /**
   * The last calculated used CPU percentage
   */
  private short lastCpuPercentage;

  /**
   * The maximum amount of heap memory available to the JVM
   */
  private long maxMemory;
  
  /**
   * The last responsive score that was calculated
   */
  private int lastServerScore;

  /**
   * The listeners attached to this monitor
   */
  private Set<ILocalServerMonitorListener> listeners = new HashSet<ILocalServerMonitorListener>();

  static {
    int mq = LocalServerSettings.getMQUIPS();
    serverGauge = ServerLoadGauge.getGaugeForServer(mq);
  }

  /**
   * Calculates a responsive score using the provided load parameters.
   * 
   * @param memoryLoad
   *          The percentage of heap memory used
   * @param cpuLoad
   *          The percentage of CPU used
   * @return The responsive score
   */
  static int getResponsiveScoreForLoad(int memoryLoad, int cpuLoad) {
    // We simplistically assume that:
    //   at low memory usage - cpu load matters more
    //   at medium memory usage - cpu load matters as much as memory load
    //   at high memory usage - cpu load matters less

    int memWeight = 50;

    if (memoryLoad < 33)
      memWeight = 20;
    else if (memoryLoad > 66)
      memWeight = 80;

    int cpuWeight = 100 - memWeight;

    int serverLoad = ((memWeight * memoryLoad) + (cpuWeight * cpuLoad)) / 100;

    return serverGauge.getResponsiveScoreForLoad(serverLoad);
  }

  private LocalServerMonitor() {
    // Prime the CPU monitoring
    lastTotalCpuTime = getTotalThreadCpuTime();
    lastTimeOfCpuCalculation = System.currentTimeMillis();

    maxMemory = runtime.maxMemory();
    if (maxMemory == Long.MAX_VALUE) {
      logMaxMemoryFixNeeded();
      maxMemory = runtime.totalMemory();
    }
    
    setPriority(Thread.MAX_PRIORITY);
    setName("Local Server Monitor");
    setDaemon(true);
    // Server monitoring disabled for now
    //start();
  }

  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  public void run() {
    while (!isInterrupted()) {
      try {
        short memLoad = getUsedMemoryPercentage();
        short cpuLoad = getUsedCpuPercentage();
        int score = getResponsiveScoreForLoad(memLoad, cpuLoad);
//        if (log.isDebugEnabled())
//          log.debug("Local server stats: memLoad="+memLoad+", cpuLoad="+cpuLoad+", responsiveScore="+score);
        
        // TODO: TWiki all the fine-tuning variables in MrSOA and what
        // real-world behavior indicates what changes may be necessary
        if (Math.abs(score-lastServerScore) > scoreDeltaBeforeUpdate) {
          lastServerScore = score;
          /* SYNC:
           * - Deadlock
           *   ? serverScoreUpdated() never returns -> This is outside the 
           *     scope of the contract of this class - implementers must 
           *     insure that listeners always return
           *   ? registerMonitor never releases monitor -> it always releases
           *   ? unregisterMonitor never releases monitor -> it always releases */
          synchronized (listeners) {
            for (ILocalServerMonitorListener listener : listeners) {
              try {
                listener.serverScoreUpdated(score);
              } catch (Throwable e) {
                log.warn("Monitor listener threw exception", e);
              }
            }
          }
        }
        
        /* SYNC:
         * - Deadlock
         *   ? monitorIntervalSeconds is set really high and blocks
         *     setScoreDeltaBeforeUpdate() and setMonitorIntervalSeconds() ->
         *     This is possible, but unlikely. Currently, only test cases
         *     adjust monitorIntervalSeconds.
         *   ? setScoreDeltaBeforeUpdate never releases monitor -> Method
         *     always releases monitor
         *   ? setMonitorIntervalSeconds never releases monitor -> Method
         *     always releases monitor
         */
        synchronized (this) {
          wait(monitorIntervalSeconds * 1000L);
        }
      } catch (InterruptedException e) {
        break;
      }
    }
  }
  
  /**
   * Calculates the percentage of heap memory currently in use by this
   * server.
   * 
   * @return The percentage of used heap memory
   */
  private short getUsedMemoryPercentage() {
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    return (short) (100 * usedMemory / maxMemory);
  }

  /**
   * Calculates the percentage of CPU currently in use by this server.
   * 
   * @return The percentage of used CPU
   */
  private short getUsedCpuPercentage() {
    long totalCpuTime = getTotalThreadCpuTime();
    long timeOfCpuCalculation = System.currentTimeMillis();

    // in microseconds
    long elapsedCpuCalculationTime = (timeOfCpuCalculation - lastTimeOfCpuCalculation) * 1000;
    
    // This avoids any div by zero exceptions
    if (elapsedCpuCalculationTime == 0) return lastCpuPercentage;
    
    long elapsedTotalCpuTime = totalCpuTime - lastTotalCpuTime;
    
    short cpuPercentage = (short) (100 * elapsedTotalCpuTime / elapsedCpuCalculationTime);
    lastTotalCpuTime = totalCpuTime;
    lastTimeOfCpuCalculation = timeOfCpuCalculation;

    lastCpuPercentage = cpuPercentage;
    return cpuPercentage;
  }

  /**
   * Logs a warning that a max memory constraint was not provided to this JVM.
   */
  private void logMaxMemoryFixNeeded() {
    String error =
      "\n" +
      "******************************************\n" +
      "*                                        *\n" +
      "*   JVM MAX MEMORY CONSTRAINT MISSING!   *\n" +
      "*                                        *\n" +
      "*  START JVM WITH -Xmx<memory> PARAMETER *\n" +
      "*                                        *\n" +
      "******************************************\n" +
      "\n";
    log.error(error);
  }

  /**
   * Calculates the total amount of system thread time consumed by this JVM.
   * 
   * @return The system thread time (in microseconds)
   */
  private long getTotalThreadCpuTime() {
    ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
    long[] threadIds = threadMxBean.getAllThreadIds();
    long totalCpuMillis = 0;
    for (long threadId : threadIds) {
      long threadCpuTime = threadMxBean.getThreadCpuTime(threadId);
      if (threadCpuTime <= 0) continue;
      // convert nanoseconds into microseconds
      totalCpuMillis += threadCpuTime / 1000;
    }
    return totalCpuMillis;
  }

  /**
   * Register a Server Monitor to receive server status updates.
   * 
   * @param listener
   *          The monitor to register
   */
  public void registerMonitorListener(ILocalServerMonitorListener listener) {
    /* SYNC: See run(){1} */
    synchronized (listeners) {
      listeners.add(listener);
    }
  }
  
  /**
   * Unregister a Server Monitor so that it no longer receives server status
   * updates.
   * 
   * @param listener
   *          The monitor to unregister
   * @return True if the monitor was successfully unregistered
   */
  public boolean unregisterMonitorListener(ILocalServerMonitorListener listener) {
    /* SYNC: See run(){1} */
    synchronized (listeners) {
      return listeners.remove(listener);
    }
  }

  /**
   * Sets the number of points that the local server score must change before
   * listeners are updated.
   * 
   * @param scoreDeltaBeforeUpdate
   *          The number of points
   */
  /* SYNC: See run(){2} */
  synchronized void setScoreDeltaBeforeUpdate(int scoreDeltaBeforeUpdate) {
    this.scoreDeltaBeforeUpdate = scoreDeltaBeforeUpdate;
    notify();
  }

  /**
   * Sets the interval in seconds between server tests.
   * 
   * @param monitorIntervalSeconds
   *          The number of seconds
   */
  /* SYNC: See run(){2} */
  synchronized void setMonitorIntervalSeconds(int monitorIntervalSeconds) {
    this.monitorIntervalSeconds = monitorIntervalSeconds;
    notify();
  }
}
