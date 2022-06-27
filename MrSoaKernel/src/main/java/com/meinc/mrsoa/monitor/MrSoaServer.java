package com.meinc.mrsoa.monitor;

import java.net.InetSocketAddress;

/**
 * Represents a physical server in a MrSOA cluster.
 * 
 * @author Matt
 */
public class MrSoaServer implements Comparable<MrSoaServer> {
  /**
   * The responsive score for this instance. Default of 100.
   */
  private int responsiveScore = 100;
  
  /**
   * The network address for this instance. Default of null.
   */
  private InetSocketAddress netAddress;
  
  /**
   * The value used to compare this instance to other instances. This facilitates
   * a performance optimization in that a key need not be generated every time
   * a server is compared.
   */
  private String key;
  
  /**
   * True if this instance is *the* local server instance. Make sure this
   * transient modifier is honored by Terracotta.
   */
  private transient boolean isLocalServer;
  
  /**
   * @param netAddress
   *          The IP-address of this server
   */
  public MrSoaServer(InetSocketAddress netAddress) {
    this.netAddress = netAddress;
    updateKey();
  }
  
  private void updateKey() {
    key = responsiveScore+":"+netAddress;
  }

  /**
   * Returns this server's IP-address
   * 
   * @return This server's IP-address
   */
  public InetSocketAddress getNetAddress() {
    return netAddress;
  }

  /**
   * Returns the responsive score of this server. A responsive score is a
   * heuristic number from <code>0</code> to <code>100</code> representing a
   * server's probability of responding instantaneously to a trivial request.
   * Several factors contribute to a responsive score such as raw computing
   * power of the server as well as current memory and CPU load.  The higher
   * the score, the faster this server <em>should</em> be able to process a
   * trivial request.  In practice, the responsive score will never be entirely
   * accurate because each request is unique and varies from every other
   * request.
   * 
   * @return This server's responsive score
   */
  /* SYNC:
   * - Deadlock
   *   ? Method delays releasing monitor -> Method never delays
   *   ? setResponsiveScore delays releasing monitor -> Method never delays */
  public synchronized int getResponsiveScore() {
    return responsiveScore;
  }
  
  /**
   * Sets this server's responsive score.
   * 
   * @param responsiveScore
   *          This server's responsive score
   * @see #getResponsiveScore()
   */
  protected synchronized void setResponsiveScore(int responsiveScore) {
    this.responsiveScore = responsiveScore;
    updateKey();
  }


  public boolean isLocalServer() {
    return isLocalServer;
  }
  
  protected void setLocalServer(boolean isLocalServer) {
    this.isLocalServer = isLocalServer;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MrSoaServer))
      return false;
    
    MrSoaServer server = (MrSoaServer) obj;
    
    return compareTo(server) == 0;
  }

  /**
   * Compares this server to another server using responsive scores.
   * @see Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(MrSoaServer server) {
    return key.compareTo(server.key);
  }

  @Override
  public String toString() {
    return ""+netAddress+"{"+responsiveScore+"}";
  }
}
