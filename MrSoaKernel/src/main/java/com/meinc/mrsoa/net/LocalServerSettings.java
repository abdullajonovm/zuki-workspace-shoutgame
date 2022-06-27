package com.meinc.mrsoa.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalServerSettings {
  private static final Log log = LogFactory.getLog(LocalServerSettings.class);
  
  /**
   * The system property key for this server's MQUIPS score
   */
  private static final String mquipsPropertyKey = "mrsoa.server.mquips";
  
  private static final String portPropertyKey = "mrsoa.server.port";
  
  /**
   * The MQUIPS value for this server as produced by the Hint benchmark
   */
  private static int mquips = 70;
  
  private static int port = 9119;

  static {
    String mquipsString = System.getProperty(mquipsPropertyKey);
    if (mquipsString == null) {
      log.warn("System property " + mquipsPropertyKey + " was not specified - using default of " + mquips + ".  If this is too low the server will be underutilized, or if too high it will be overutilized.");
    } else
      try {
        mquips = Integer.parseInt(mquipsString);
      } catch (NumberFormatException e) { }
    
    String portString = System.getProperty(portPropertyKey);
    if (portString != null) {
      try {
        port = Integer.parseInt(portString);
      } catch (NumberFormatException e) { }
    }
    
    log.info("Binding to TCP port " + port);
  }
  
  /**
   * Returns the MQUIPS score assigned to this server. This is determined by
   * reading the system property described by {@link #mquipsPropertyKey}.
   * 
   * @return The MQUIPS score for this server
   */
  public static int getMQUIPS() {
    return mquips;
  }

  public static InetAddress getLocalServerAddress() {
    try {
      return InetAddress.getByName("127.0.0.1");
    } catch (UnknownHostException e) {
      // Shouldn't ever happen
      throw new RuntimeException(e);
    }
  }
  
  public static int getLocalServerPort() {
    return port;
  }
  
  public static InetSocketAddress getLocalServerSocketAddress() {
    return new InetSocketAddress(getLocalServerAddress(), getLocalServerPort());
  }
}
