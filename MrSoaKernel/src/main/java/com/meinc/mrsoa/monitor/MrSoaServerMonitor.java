package com.meinc.mrsoa.monitor;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.LocalServerSettings;
import com.meinc.mrsoa.service.ServiceEndpoint;

/**
 * Monitors the MrSOA cluster by keeping track of which servers support which
 * services as well as the health of each server in the cluster.
 * 
 * @author Matt
 */
public class MrSoaServerMonitor implements ILocalServerMonitorListener {
  private static final Log log = LogFactory.getLog(MrSoaServerMonitor.class);

  private static MrSoaServerMonitor singleton;
  
  // Static initializers are thread-safe
  static {
    LocalServerMonitor monitor = LocalServerMonitor.getInstance();
    singleton = new MrSoaServerMonitor(monitor);
    monitor.registerMonitorListener(singleton);
  }
  
  public static MrSoaServerMonitor getInstance() {
    return singleton;
  }
  
  /**
   * A Terracotta-shared Map of service-descriptor to set-of-servers which host
   * the service. This map arbitrarily serves as the master set meaning before
   * synchronizing on any of the following Terracotta-shared sets, this map must
   * first be synchronized regardless of whether it is accessed or not. This
   * practice insures data consistency between the Terracotta-shared maps.
   */
  private Map<String,SortedSet<MrSoaServer>> serviceToServers = new HashMap<String,SortedSet<MrSoaServer>>();

  /**
   * A Terracotta-shared Map of server to set-of-set-of-servers that contain 
   * the server. All inner set-of-servers come from {@link #serviceToServers}
   */
  private Map<MrSoaServer,Set<SortedSet<MrSoaServer>>> serverToServerSets = new HashMap<MrSoaServer,Set<SortedSet<MrSoaServer>>>();

  /**
   * The local server that hosts the JVM executing this code
   */
  private MrSoaServer localServer;

  /**
   * The {@link LocalServerMonitor} for the local server
   */
  private LocalServerMonitor serverMonitorThread;
  
  /**
   * If the local responsive score is greater than this value, a request will
   * "stick" to the local server
   */
  private int localStickyScore = 30;

  /**
   * Create new instance with the provided Server-monitor. The provided monitor
   * must be started outside of this instance for it to have any affect.
   * 
   * @param serverMonitor
   *          The Server-monitor to use with this instance
   */
  protected MrSoaServerMonitor(LocalServerMonitor serverMonitor) {
    serverMonitorThread = serverMonitor;
    
    try {
      InetAddress localNetAddress = getLocalNetAddress();
      log.info("Found local server address of: " + localNetAddress);
      localServer = new MrSoaServer(new InetSocketAddress(localNetAddress, LocalServerSettings.getLocalServerPort()));
    } catch (IOException e) {
      log.fatal("Could not determine local network address - server is shutting down", e);
      System.exit(9);
    }
  }

  /**
   * Returns the local server.
   * <p>
   * <em>Note:</em> Changes to the returned server have no immediate affect on
   * the monitoring algorithms (don't do it).
   * 
   * @return The current responsive score
   * @see MrSoaServer#getResponsiveScore()
   */
  protected MrSoaServer getLocalServer() {
    return localServer;
  }

  /**
   * Returns an IP-address representing the best server candidate for handling a
   * request to the specified service.
   * 
   * @param endpoint
   *          The service endpoint
   * @return An IP-address to a MrSOA server
   */
  public InetSocketAddress getNetAddressToEndpoint(String destination) {
    SortedSet<MrSoaServer> serverSet;
    /* SYNC:
     * - Deadlock
     *   ? Block delays releasing monitor -> See the following blocks as they
     *     are the only things that could block/delay this block.
     *       1. handleConnectionFailedToEndpoint(){1}
     *       2. registerMonitorListener(){1}
     *       3. unregisterLocalService(){1}
     *       4. serverScoreUpdated(){1} */
    synchronized (serviceToServers) {
      serverSet = serviceToServers.get(destination);

      if (log.isDebugEnabled())
        log.debug("Found server set " + serverSet + " for service " + destination);

      if (serverSet == null)
        return null;
  
      MrSoaServer bestServer;
      /* SYNC:
       * - Deadlock
       *   ? Block delays releasing monitor -> See the following blocks as they
       *     are the only things that could block/delay this block.
       *       1. handleConnectionFailedToEndpoint(){2}
       *       2. handleConnectionFailedToEndpoint(){6}
       *       3. registerService(){2}
       *       4. unregisterLocalService(){2}
       *       5. updateServerInSet(){1} */
      synchronized (serverSet) {
        if (serverSet.isEmpty()) {
          if (log.isDebugEnabled())
            log.debug("Server set for service " + destination + " is empty");
          return null;
        }
  
        // If the local server is greater than n% likely to respond rapidly, use
        // the local server so as to eliminate network overhead for the next
        // request and optimize collective network usage. This produces the effect
        // that service calls are sticky to the local server.
        if (serverSet.contains(localServer)
            && localServer.getResponsiveScore() > localStickyScore) {
          bestServer = localServer;
        } else {
          bestServer = serverSet.last();
        }
      }
  
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the bestServer monitor because it is always
       *   nested in a serviceToServers sync block */
      synchronized (bestServer) {
        return bestServer.getNetAddress();
      }
    }
  }

  /**
   * Removes the server referred to by the provided parameters from the global
   * server registry.
   * 
   * @param destination
   * @param remoteAddress
   */
  public void handleConnectionFailedToEndpoint(String destination, InetSocketAddress remoteAddress) {
    SortedSet<MrSoaServer> serverSet;
    /* SYNC:
     * - Deadlock
     *   ? Other threads do not release this monitor -> See
     *     getNetAddressToEndpoint(){1}
     *   ? This block does not release the monitor -> This will not happen -
     *     see nested sync blocks */
    synchronized (serviceToServers) {
      serverSet = serviceToServers.get(destination);
  
      if (serverSet == null)
        return;
  
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the serverSet monitor because it is always
       *   nested in a serviceToServers sync block */
      synchronized (serverSet) {
        if (serverSet.isEmpty())
          return;
  
        Iterator<MrSoaServer> serverIt = serverSet.iterator();
        while (serverIt.hasNext()) {
          MrSoaServer server = serverIt.next();
          /* SYNC:
           * ? Other threads do not release this monitor -> If we are here, no
           *   other thread can own the server monitor because it is always
           *   nested in a serviceToServers sync block */
          synchronized (server) {
            if (server.getNetAddress() == remoteAddress) {
              serverIt.remove();
              if (serverSet.isEmpty()) {
                serviceToServers.remove(destination);
              }
              
              /* SYNC:
               * ? Other threads do not release this monitor -> If we are here,
               *   no other thread can own the serverToServerSets monitor
               *   because it is always nested in a serviceToServers sync
               *   block */
              synchronized (serverToServerSets) {
                Set<SortedSet<MrSoaServer>> serverSets = serverToServerSets.get(server);
                /* SYNC:
                 * ? Other threads do not release this monitor -> If we are
                 *   here, no other thread can own the serverSets monitor
                 *   because it is always nested in a serviceToServers sync
                 *   block */
                synchronized (serverSets) {
                  for (SortedSet<MrSoaServer> innerServerSet : serverSets) {
                     /* SYNC:
                      * ? Other threads do not release this monitor -> If we
                      *   are here, no other thread can own the innerServerSet
                      *   monitor because it is always nested in a
                      *   serviceToServers sync block */
                    synchronized (innerServerSet) {
                      innerServerSet.remove(server);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Registers a service as available for handling requests on the local server.
   * 
   * @param endpoint
   *          The service descriptor to register
   */
  public void registerLocalService(ServiceEndpoint endpoint) {
    registerService(endpoint, null);
  }

  /**
   * Registers a service as available for handling requests.
   * 
   * @param endpoint
   *          The service descriptor to register
   * @param server
   *          The server where the service is hosted. If null, the local server
   *          is used.
   */
  public void registerService(ServiceEndpoint endpoint, MrSoaServer server) {
    String endpointString = endpoint.toString();
    if (server == null)
      server = localServer;
    
    if (log.isDebugEnabled())
      log.debug("Registering service " + endpoint + " to server " + server);

    SortedSet<MrSoaServer> serverSet;
    /* SYNC:
     * - Deadlock
     *   ? Other threads do not release this monitor -> See
     *     getNetAddressToEndpoint(){1}
     *   ? This block does not release the monitor -> This will not happen -
     *     see nested sync blocks */
    synchronized (serviceToServers) {
      serverSet = serviceToServers.get(endpointString);
      if (serverSet == null) {
        serverSet = new TreeSet<MrSoaServer>();
        serviceToServers.put(endpointString, serverSet);
      }

      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the serverSet monitor because it is always
       *   nested in a serviceToServers sync block */
      synchronized (serverSet) {
        serverSet.add(server);
      }
  
      Set<SortedSet<MrSoaServer>> sets;
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the serverToServerSets monitor because it is
       *   always nested in a serviceToServers sync block */
      synchronized (serverToServerSets) {
        sets = serverToServerSets.get(server);
        if (sets == null) {
          sets = new HashSet<SortedSet<MrSoaServer>>();
          serverToServerSets.put(server, sets);
        }
      }
  
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the sets monitor because it is always nested in
       *   a serviceToServers sync block */
      synchronized (sets) {
        sets.add(serverSet);
      }
    }
  }

  /**
   * Unregisters a service as being available for handling requests on the local
   * server.
   * 
   * @param endpoint
   *          The service to unregister
   */
  public void unregisterLocalService(ServiceEndpoint endpoint) {
    String endpointString = endpoint.toString();

    SortedSet<MrSoaServer> serverSet;
    /* SYNC:
     * - Deadlock
     *   ? Other threads do not release this monitor -> See
     *     getNetAddressToEndpoint(){1}
     *   ? This block does not release the monitor -> This will not happen -
     *     see nested sync blocks */
    synchronized (serviceToServers) {
      serverSet = serviceToServers.get(endpointString);
      if (serverSet != null) {
        /* SYNC:
         * ? Other threads do not release this monitor -> If we are here, no
         *   other thread can own the serverSet monitor because it is always
         *   nested in a serviceToServers sync block */
        synchronized (serverSet) {
          serverSet.remove(localServer);
          if (serverSet.isEmpty())
            serviceToServers.remove(endpointString);
        }
      }
  
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the serverToServerSets monitor because it is
       *   always nested in a serviceToServers sync block */
      synchronized (serverToServerSets) {
        Set<SortedSet<MrSoaServer>> sets = serverToServerSets.get(localServer);
        if (sets != null) {
          /* SYNC:
           * ? Other threads do not release this monitor -> If we are here, no
           *   other thread can own the sets monitor because it is always
           *   nested in a serviceToServers sync block */
          synchronized (sets) {
            sets.remove(serverSet);
            if (sets.isEmpty())
              serverToServerSets.remove(localServer);
          }
        }
      }
    }
  }

  /**
   * Returns the IP-address of the local server
   * 
   * @return The local server's IP-address
   * @throws IOException
   *           If the IP-address cannot be determined
   */
  private InetAddress getLocalNetAddress() throws IOException {
    Enumeration<NetworkInterface> netIfaces = NetworkInterface.getNetworkInterfaces();
    while (netIfaces.hasMoreElements()) {
      NetworkInterface netIface = netIfaces.nextElement();
      if (netIface.isLoopback())
        continue;
      Enumeration<InetAddress> addresses = netIface.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress ip = addresses.nextElement();
        if (ip.getAddress().length == 4)
          return ip;
      }
    }
    return Inet4Address.getLocalHost();
  }

  /**
   * Used by {@link LocalServerMonitor} to update the local server's responsive
   * score.
   * 
   * @param score
   *          The local server's responsive score
   */
  /* SYNC:
   * ? Method delays and blocks LocalServerMonitor.run()
   *   - ...
   */
  @Override
  public void serverScoreUpdated(int score) {
    /* SYNC:
     * - Deadlock
     *   ? Other threads do not release this monitor -> See
     *     getNetAddressToEndpoint(){1}
     *   ? This block does not release the monitor -> This will not happen -
     *     see nested sync blocks */
    synchronized (serviceToServers) {
       /* SYNC:
        * ? Other threads do not release this monitor -> If we are here, no
        *   other thread can own the serverToServerSets monitor because it is
        *   always nested in a serviceToServers sync block */
      synchronized (serverToServerSets) {
        Set<SortedSet<MrSoaServer>> serverSets = serverToServerSets.get(localServer);
        if (serverSets == null)
          return;
        /* SYNC:
         * ? Other threads do not release this monitor -> If we are here, no
         *   other thread can own the serverSets monitor because it is always
         *   nested in a serviceToServers sync block */
        synchronized (serverSets) {
          for (SortedSet<MrSoaServer> serverSet : serverSets) {
            updateServerInSet(serverSet, localServer, score);
          }
        }
      }
    }
  }

  /**
   * Used to re-sort a server within a set of MrSOA servers according to the
   * server's responsive score.
   * 
   * @param set
   *          The set of servers containing the server to re-sort
   * @param server
   *          The server to re-sort
   * @param score 
   */
  private void updateServerInSet(Set<MrSoaServer> set, MrSoaServer server, int score) {
    // Force the server to re-sort among its peers in the set
    /* SYNC:
     * ? Other threads do not release this monitor -> This method is only ever
     *   called from serverScoreUpdated() and thus if we are here, no other
     *   thread can own the server monitor because it is always nested in a
     *   serviceToServers sync block */
    synchronized (set) {
      // It is critical that we remove this instance before changing it.
      // Otherwise it will never be removed!
      set.remove(server);
      
      // Change the instance
      /* SYNC:
       * ? Other threads do not release this monitor -> If we are here, no
       *   other thread can own the server monitor because it is always nested
       *   in a serviceToServers sync block */
      synchronized (server) {
        server.setResponsiveScore(score);
      }
      
      // Add it back into the set - this re-sorts the instance
      set.add(server);
    }
  }

  public boolean isLocalServerAddress(InetSocketAddress remoteAddress) {
    return localServer.getNetAddress().equals(remoteAddress);
  }

  /**
   * For unit tests only
   */
  protected void registerRemoteService(ServiceEndpoint endpoint, MrSoaServer server) {
    registerService(endpoint, server);
  }

  protected void setLocalStickyScore(int localStickyScore) {
    this.localStickyScore = localStickyScore;
  }
}
