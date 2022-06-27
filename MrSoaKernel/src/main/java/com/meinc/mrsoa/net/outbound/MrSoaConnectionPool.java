package com.meinc.mrsoa.net.outbound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.mrsoa.net.LocalServerSettings;
import com.meinc.mrsoa.net.MrSoaNetworkingException;
import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.TcpHelperNio;

public class MrSoaConnectionPool {
  private static final Log log = LogFactory.getLog(MrSoaConnectionPool.class);
  
  private static volatile Map<InetSocketAddress,ConnectionPair> netPools = new HashMap<InetSocketAddress,ConnectionPair>();
  
  private static ConnectionPair localhostPair = getPair(LocalServerSettings.getLocalServerSocketAddress());
  
  private static Timer fillLocalhostTimer = new Timer("LocalhostFiller", true);
  
  static {
    TimerTask fillTask = new TimerTask() {
      public void run() {
        if (Boolean.parseBoolean(ServerPropertyHolder.getProperty("mrsoa.preload.localhost.connection.pool", "false"))) {
          //TODO: parameterize constant
          if (localhostPair.pool.getNumActive() + localhostPair.pool.getNumIdle() < 10) {
            log.info("Populating localhost pool...");
            long now = System.currentTimeMillis();
            int failures = 0;
            //TODO: parameterize constant
            while (localhostPair.pool.getNumActive() + localhostPair.pool.getNumIdle() < 10) {
              try {
                localhostPair.pool.addObject();
              } catch (Exception e) {
                log.error("Error while populating localhost pool: "+e.getMessage(), e);
                try {
                  if (++failures >= 3) {
                    log.error("Too many errors while populating localhost pool - giving up for now");
                    break;
                  } else {
                    Thread.sleep(100);
                  }
                } catch (InterruptedException e1) {
                  Thread.currentThread().interrupt();
                  break;
                }
              }
            }
            long delta = System.currentTimeMillis() - now;
            log.info(String.format("Populated localhost pool in %dms", delta));
          }
        }
      }
    };
    fillLocalhostTimer.schedule(fillTask, 0, 60000);
  }
  
  private static ConnectionPair getPair(InetSocketAddress remoteAddress) {
    ConnectionPair pair = netPools.get(remoteAddress);
    if (pair == null) {
      /* SYNC:
       * - Deadlock
       *   ? Block does not release monitor -> Block always releases monitor */
      synchronized (MrSoaConnectionPool.class) {
        // We check whether the pair is still null because another thread may have beat us here
        pair = netPools.get(remoteAddress);
        if (pair == null) {
          pair = createNewConnectionPool(remoteAddress);
          Map<InetSocketAddress,ConnectionPair> newMap = new HashMap<InetSocketAddress,ConnectionPair>(netPools);
          newMap.put(remoteAddress, pair);
          netPools = newMap;
        }
      }
    }
    return pair;
  }
  
  public static Socket borrowConnection(InetSocketAddress remoteAddress) throws IOException {
    try {
      ConnectionPair pair = getPair(remoteAddress);
      long now = 0;
      if (log.isDebugEnabled()) {
        log.debug("Borrowing outbound connection from pool: active="+pair.pool.getNumActive()+", idle="+pair.pool.getNumIdle());
        now = System.currentTimeMillis();
      }
      MrSoaSocket socket = (MrSoaSocket) pair.pool.borrowObject();
      if (log.isDebugEnabled()) {
        long delta = System.currentTimeMillis() - now;
        log.debug("Connection returned from pool in " + delta + "ms");
      }
      return socket;
      
    } catch (Exception e) {
      if (e instanceof IOException)
        throw (IOException) e;
      else if (e instanceof RuntimeException)
        throw (RuntimeException) e;
      else
        throw new RuntimeException(e);
    }
  }

  public static void returnConnection(Socket socket) {
    if (!(socket instanceof MrSoaSocket))
      throw new IllegalArgumentException("Returned connection did not originate from borrowConnection(..)");
    
    InetSocketAddress remoteAddress = ((MrSoaSocket)socket).getRemoteAddress();
    
    ConnectionPair pair = netPools.get(remoteAddress);
    if (pair == null)
      throw new IllegalStateException("Pool dissappeared!");
    
    try {
      pair.pool.returnObject(socket);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private static ConnectionPair createNewConnectionPool(InetSocketAddress remoteAddress) {
    ConnectionPair pair = new ConnectionPair();
    
    pair.factory = new ConnectionFactory(remoteAddress);
    pair.pool = new GenericObjectPool(pair.factory);
    
    // Allow a given connection n minutes of idleness before considering its destruction
    pair.pool.setMinEvictableIdleTimeMillis(10 * 60 * 1000); // 10 minutes
    // We don't want any hard limit on the number of idle connections other than
    // when each connection times out.
    pair.pool.setMaxIdle(-1);
    pair.pool.setMinIdle(1);
    // Check for connections to be destroyed every n minutes
    pair.pool.setTimeBetweenEvictionRunsMillis(10 * 60 * 1000); // 10 minutes
    // Only evict a max 1/abs(n) of idle connections per eviction run
    pair.pool.setNumTestsPerEvictionRun(-2);
    // No limit on the maximum number of connections allowed in the pool at one
    // time. This works because other throttling mechanisms are in place
    // elsewhere in MrSoa that will generally prevent runaway connection
    // creation. The major exception to this rule would be a runaway service
    // application that is launching chatty threads with wild abandon.
    pair.pool.setMaxActive(10);
    // When the pool is tapped out, fail
    pair.pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
    
    pair.pool.setTestOnBorrow(true);
    pair.pool.setTestOnReturn(true);
    pair.pool.setTestWhileIdle(true);
    
    return pair;
  }
  
  private static class ConnectionFactory implements PoolableObjectFactory {
    private InetSocketAddress netAddress;

    public ConnectionFactory(InetSocketAddress remoteAddress) {
      this.netAddress = remoteAddress;
    }
    
    public Object makeObject() throws Exception {
      if (log.isDebugEnabled())
        log.debug("Pool new Connection for host " + netAddress);
      
      MrSoaSocket socket = null;
      int failures = 0;
      while (true) {
        try {
          socket = new MrSoaSocket(netAddress, 10000);
          break;
        } catch (IOException e) {
          if (++failures <= 3)
            log.warn(String.format("Connect to %s failed (\"%s\") - retry #%d", netAddress, e.getMessage(), failures));
          else
            throw e;
        } catch (Exception e) {
          throw new MrSoaNetworkingException("Could not connect to host " + netAddress, e);
        }
      }
      
      return socket;
    }

    public void destroyObject(Object obj) throws Exception {
      if (log.isDebugEnabled())
         log.debug("Pool kill Connection for host " + netAddress);
      
      MrSoaSocket socket = (MrSoaSocket) obj;
      try {
        socket.close();
      } catch (IOException e) { }
    }

    public boolean validateObject(Object obj) {
      MrSoaSocket socket = (MrSoaSocket) obj;
      
      boolean valid = !socket.isClosed() &&
                       socket.isConnected() &&
                      !socket.isInputShutdown() &&
                      !socket.isOutputShutdown();
      if (!valid) return false;
      
      int command = -2;
      try {
        command = TcpHelper.readCommand(socket, 1);
      } catch (IOException e) { }
      
      switch (command) {
        case -2:
          break;
          
        case -1:
        case TcpHelperNio.SERVER_CLOSE:
          return false;
          
        case TcpHelperNio.SERVER_FATAL:
          String reason = null;
          try {
              reason = (String) TcpHelper.readPayload(socket, getClass().getClassLoader());
            } catch (Exception e) { }          
          log.error("Remote server closed connection with error: " + (reason == null ? "no reason provided" : reason));
          return false;

        default:
          log.error("Unexpected network communication - closing connection");
          TcpHelper.fatalizeConnection(socket, "Unexpected network communication");
          return false;
      }
      
      return true;
    }

    public void activateObject(Object obj) throws Exception { }
    public void passivateObject(Object obj) throws Exception { }
  }
  
  private static class ConnectionPair {
    public ConnectionFactory factory;
    public GenericObjectPool pool;
  }
}
