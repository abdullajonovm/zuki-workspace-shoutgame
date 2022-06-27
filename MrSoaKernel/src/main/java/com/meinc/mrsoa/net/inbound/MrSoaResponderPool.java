package com.meinc.mrsoa.net.inbound;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.monitor.ILocalServerMonitorListener;
import com.meinc.mrsoa.monitor.LocalServerMonitor;
import com.meinc.mrsoa.net.MrSoaNetworkingException;

/**
 * Manages a pool of {@link MrSoaResponder} objects.  The goals of this class are:
 * <ul><li>Minimize thread create/destroy</li>
 * <li>Minimize memory allocate/deallocate</li>
 * <li>Minimize request-to-response time</li></ol>
 *
 * @author Matt
 */
public class MrSoaResponderPool implements ILocalServerMonitorListener {
  private static final Log log = LogFactory.getLog(MrSoaResponder.class);
  private static final Log throtLog = LogFactory.getLog(MrSoaResponder.class);
  
  private static MrSoaResponderPool singleton;
  
  // Static initializers are thread-safe
  static {
    MrSoaResponderFactory factory = new MrSoaResponderFactory();
    singleton = new MrSoaResponderPool(new GenericObjectPool(factory), factory);
  }
  
  /**
   * Returns the singleton responder pool.
   * 
   * @return The responder pool
   */
  public static MrSoaResponderPool getInstance() {
    return singleton;
  }
  
  /**
   * The actual pool implementation
   */
  private GenericObjectPool responderPool;
  
  /**
   * Responders are added to this list so they are never garbage collected
   * while active
   */
  private ArrayList<MrSoaResponder> activeResponders = new ArrayList<MrSoaResponder>();
  
  /**
   * Instantiate a new pool using the provided factory.
   * 
   * @param responderPool
   *          The pool implementation to use as the underlying responder pool
   * @param responderFactory
   *          The factory
   */
  MrSoaResponderPool(GenericObjectPool responderPool, MrSoaResponderFactory responderFactory) {
    this.responderPool = responderPool;
    responderFactory.setResponderPool(this);
    
    Properties serverProperties = ServerPropertyHolder.getProps();
    
    // Enforce a minimum of n idle responders at any given time
//    int minIdle = Integer.parseInt(serverProperties.getProperty("mrsoa.responder.pool.minIdle", "1"));
    responderPool.setMinIdle(1);
    // Allow a given responder n minutes of idleness before considering its destruction
    responderPool.setSoftMinEvictableIdleTimeMillis(5 * 60 * 1000); // 5 minutes
    // Since we are using Soft Timeouts (see previous line) we want to disable Hard Timeouts
    // A Soft Timeout respects MinIdle, but a Hard Timeout does not
    responderPool.setMinEvictableIdleTimeMillis(0);
    // We don't want any hard limit on the number of idle responders other than
    // when each responder times out
//    int maxIdle = Integer.parseInt(serverProperties.getProperty("mrsoa.responder.pool.maxIdle", "-1"));
    responderPool.setMaxIdle(-1);
    // Check for responders to be destroyed every n minutes
    responderPool.setTimeBetweenEvictionRunsMillis(10 * 60 * 1000); // 10 minutes
    // Only evict a max 1/abs(n) of idle responders per eviction run
    responderPool.setNumTestsPerEvictionRun(-2);
    // Maximum number of responders allowed in the pool at one time
    // This number is set low because it will be adjusted as necessary
//    int maxActive = Integer.parseInt(serverProperties.getProperty("mrsoa.responder.pool.maxActive", "1000"));
    responderPool.setMaxActive(10);
    // When the pool is tapped out, fail
    responderPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
    
    responderPool.setTestOnBorrow(true);
    responderPool.setTestOnReturn(true);
    responderPool.setTestWhileIdle(true);
    
    LocalServerMonitor.getInstance().registerMonitorListener(this);
  }

  /**
   * Activate a responder in the pool to begin listening to the provided socket.
   * 
   * @param socket
   *          The socket to listen to
   */
  public void listenTo(SelectionKey key) {
    MrSoaResponder responder;
    try {
      responder = (MrSoaResponder) responderPool.borrowObject();
      
    } catch (NoSuchElementException e) {
      // This should never happen because MrSoaReceiver checks for a potential
      // receiver before allowing a new connection. But just in case...
      log.warn("Closing connection because no responders are available");
      try {
        key.channel().close();
      } catch (IOException e1) {
        // Ignore
      }
      key.cancel();
      return;
      
    } catch (Exception e) {
      throw new MrSoaNetworkingException("Could not retrieve Responder from pool", e);
    }
    
    /* SYNC:
     * - Deadlock
     *   ? Block delays releasing monitor -> Block never delays
     *   ? markIdle(){1} delays releasing monitor -> Method never delays
     *   ? markClosed(){1} delays releasing monitor -> Method never delays */
    synchronized (activeResponders) {
      activeResponders.add(responder);
    }
    
    /* SYNC: See MrSoaReceiver.run */
    // Associate this responder with this key
    key.attach(responder);
    
    responder.listenToNow(key);
  }
  
  /**
   * Mark the provided responder as idle within the pool. A responder is idle
   * when it is no longer listening to a socket.
   * 
   * @param responder
   *          The responder to mark as idle
   */
  public void markIdle(MrSoaResponder responder) {
    log.debug("Marking responder idle");
    boolean responderWasActive;
    /* SYNC: See listenTo(){1} */
    synchronized (activeResponders) {
      responderWasActive = activeResponders.remove(responder);
    }
    if (!responderWasActive) {
      if (log.isDebugEnabled())
        log.debug("Marking responder idle - responder was not active - not returning to pool (" + this + ")");
    } else {
      if (log.isDebugEnabled())
        log.debug("Marking responder idle - returning to pool (" + this + ")");
      try {
        responderPool.returnObject(responder);
      } catch (Exception e) {
        log.error("Could not return responder to pool", e);
      }
    }
  }
  
  /**
   * Mark the provided responder as closed and remove it from the pool.
   * 
   * @param responder
   *          The responder to close
   */
  public void markClosed(MrSoaResponder responder) {
    boolean responderWasActive;
    /* SYNC: See listenTo(){1} */
    synchronized (activeResponders) {
      responderWasActive = activeResponders.remove(responder);
    }
    if (!responderWasActive) {
      if (log.isDebugEnabled())
        log.debug("Marking responder closed - responder was not active - not returning to pool (" + this + ")");
    } else {
      if (log.isDebugEnabled())
        log.debug("Marking responder closed - returning to pool (" + this + ")");
      try {
        responderPool.invalidateObject(responder);
      } catch (Exception e) {
        log.error("Could not invalidate responder to pool", e);
      }
    }
  }
  
  /**
   * Evict idle responders from the pool right now rather than waiting for the
   * eviction timer.
   */
  public void evictNow() {
    try {
      responderPool.evict();
    } catch (Exception e) {
      log.error("Error while explicitly evicting Responders from pool", e);
    }
  }
  
  /**
   * Closes this responder pool. Operations will no longer work on this
   * instance.
   */
  public void close() {
    if (log.isDebugEnabled())
      log.debug("Closing responder pool (" + this + ")");
    LocalServerMonitor.getInstance().unregisterMonitorListener(this);
    
    try {
      responderPool.close();
    } catch (Exception e) {
      log.error("Error while closing Responder Pool", e);
    }
  }
  
  /**
   * Returns the number of active responders currently within the pool. A
   * responder is active when it is currently listening to a socket.
   * 
   * @return The number of active responders
   */
  public int getActiveResponderCount() {
    return responderPool.getNumActive();
  }
  
  /**
   * Returns the number of idle responders currently within the pool. A
   * responder is idle when it is no longer listening to a socket.
   * 
   * @return The number of idle responders
   */
  public int getIdleResponderCount() {
    return responderPool.getNumIdle();
  }
  
  public int getPotentialResponders() {
    int activeResponders = responderPool.getNumActive();
    int maxResponders = responderPool.getMaxActive();
    int potentialResponders = maxResponders - activeResponders;
    return potentialResponders;
  }

  private long firstBadScoreMillis;
  private long lastAdjustmentMillis;
  private int averageScoreSinceFirstBadScore = -1;
  private final int maxDecreaseOfResponderCountPerInterval = 3;
  private final int increaseResponderCountWhenAvailableRespondersIsLessThan = 3;
  private final int maxIncreaseOfResponderCountPerInterval = 3;
  private int adjustAfterFirstBadScoreMillis = 60000;
  private int shrinkPoolIntervalMillis = 10000;
  private int growPoolIntervalMillis = 5000;
  
  /**
   * Controls the number of responders allowed in the responder pool based on
   * the provided score value.
   * <p>
   * By default, if the score has averaged under 20 for the last 70 seconds, the
   * number of responders in the pool is capped at its current count.
   * <p>
   * By default, if the score has averaged under 20 for the last 70 seconds, and
   * recent scores are under 10, the number of responders in the pool is reduced
   * by 3 responders every 10 seconds as necessary.
   * <p>
   * By default, if the average score is above 20, the pool is allowed to grow
   * by 3 responders every 5 seconds as necessary.
   * 
   * @see com.meinc.mrsoa.monitor.ILocalServerMonitorListener#serverScoreUpdated(int)
   */
  /* SYNC:
   * ? Method delays and blocks LocalServerMonitor.run()
   *   - Method always returns quickly */
  @Override
  public void serverScoreUpdated(int score) {
    String oldState = null;
    int oldAvgScore = 0;
    if (log.isDebugEnabled()) {
      oldState = toString();
      oldAvgScore = averageScoreSinceFirstBadScore;
    }
    
    // Update our average "bad" score
    if (averageScoreSinceFirstBadScore == -1 && score < 20)
      averageScoreSinceFirstBadScore = score;
    else if (averageScoreSinceFirstBadScore != -1)
      averageScoreSinceFirstBadScore = (averageScoreSinceFirstBadScore + score) / 2;
    
    int activeResponders = responderPool.getNumActive();
    int maxResponders = responderPool.getMaxActive();
    int potentialResponders = maxResponders - activeResponders;
    long currentTime = System.currentTimeMillis();
    
    // If our average "bad" score is too bad (under 20)
    if (averageScoreSinceFirstBadScore != -1 && averageScoreSinceFirstBadScore < 20) {
      
      if (firstBadScoreMillis == 0) {
        firstBadScoreMillis = currentTime;

      // If we have had 60 seconds of bad scores and we haven't responded to it
      // in the last 10 seconds
      } else if (currentTime - firstBadScoreMillis >= adjustAfterFirstBadScoreMillis
          && currentTime - lastAdjustmentMillis >= shrinkPoolIntervalMillis) {
        
        lastAdjustmentMillis = currentTime;
        
        // If we have a really bad score, reduce responder count
        if (averageScoreSinceFirstBadScore < 10) {
          int newResponders = Math.max(0, activeResponders - maxDecreaseOfResponderCountPerInterval);
          
          responderPool.setMaxActive(newResponders);
          throtLog.warn("Shrinking responder count from " + maxResponders + " to " + newResponders + " due to very low system resources");
          try {
            responderPool.evict();
          } catch (Exception e) {
            throtLog.warn("Error while evicting responders from pool", e);
          }

        // If we have a moderately bad score, cap responder count
        } else {
          responderPool.setMaxActive(activeResponders);
          throtLog.warn("Capping responder count to " + activeResponders + " due to low memory.");
          try {
            responderPool.evict();
          } catch (Exception e) {
            throtLog.warn("Error while evicting responders from pool", e);
          }
        }
      }
    // If our average "bad" score is acceptable
    } else {
      firstBadScoreMillis = 0;
      averageScoreSinceFirstBadScore = -1;
      
      // If we are running low on responders and we haven't increased the number
      // of responders in the last 10 seconds, expand responder count
      if (potentialResponders < increaseResponderCountWhenAvailableRespondersIsLessThan
          && currentTime - lastAdjustmentMillis >= growPoolIntervalMillis) {
        lastAdjustmentMillis = currentTime;
        int newResponders = maxResponders + maxIncreaseOfResponderCountPerInterval;
        responderPool.setMaxActive(newResponders);
        throtLog.info("Expanding responder count from " + maxResponders + " to " + newResponders + " due to demand");
      }
    }
    if (log.isDebugEnabled())
      log.debug("Updated server score from "+oldAvgScore+" ("+oldState+") to "+averageScoreSinceFirstBadScore+" ("+this+")");
  }

  /**
   * Sets the number of millis to wait after first receiving a bad responsive
   * score before taking any action to correct the bad score.
   * 
   * @param adjustAfterFirstBadScoreMillis
   *          The number of millis to wait
   */
  protected void setAdjustAfterFirstBadScoreMillis(int adjustAfterFirstBadScoreMillis) {
    this.adjustAfterFirstBadScoreMillis = adjustAfterFirstBadScoreMillis;
  }

  /**
   * Sets the number of millis to wait after shrinking the pool before shrinking
   * it again.
   * 
   * @param shrinkPoolIntervalMillis
   *          The number of millis to wait
   */
  protected void setShrinkPoolIntervalMillis(int shrinkPoolIntervalMillis) {
    this.shrinkPoolIntervalMillis = shrinkPoolIntervalMillis;
  }

  /**
   * Sets the number of millis to wait after growing the pool before growing it
   * again.
   * 
   * @param growPoolIntervalMillis
   *          The number of millis to wait
   */
  protected void setGrowPoolIntervalMillis(int growPoolIntervalMillis) {
    this.growPoolIntervalMillis = growPoolIntervalMillis;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + getActiveResponderCount() + "/"
        + getIdleResponderCount() + "/" + getPotentialResponders() + "<="
        + responderPool.getMaxActive() + "]";
  }
}
