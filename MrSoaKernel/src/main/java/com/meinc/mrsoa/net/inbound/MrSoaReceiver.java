package com.meinc.mrsoa.net.inbound;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.LocalServerSettings;

/**
 * Accepts new connections and network read/write events for the local MrSOA
 * server.
 * <p>
 * Once a new connection is established, a receiver will hand it over to a
 * responder. Upon network events, a receiver will notify the appropriate
 * responder.
 * 
 * @author Matt
 */
public class MrSoaReceiver extends Thread {
  private static final Log log = LogFactory.getLog(MrSoaReceiver.class);
  
  /**
   * The receiver's channel bound to a local port
   */
  private static List<ServerSocketChannel> serverChannels = new ArrayList<ServerSocketChannel>();
  
  /**
   * The receiver's selector that will signal network events
   */
  private static Selector selector;

  static {
    try {
      selector = Selector.open();
    } catch (IOException e) {
      log.error("Error opening network access", e);
    }
  }
  
  /**
   * The receiver's pool of responders
   */
  private MrSoaResponderPool responderPool;
  
  /**
   * Create a new receiver using the specified responder pool and address.
   * <p>
   * <em>Note:</em> This object's thread is automatically started upon calling
   * this constructor.
   * 
   * @param responderPool
   *          The responder pool
   * @param serverAddress
   *          The address this receiver should bind to. If null, default address
   *          is used.
   * @throws IOException
   *           If the specified address could not be bound
   */
  public MrSoaReceiver(MrSoaResponderPool responderPool, InetSocketAddress serverAddress)
  throws IOException {
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);
    
    if (serverAddress == null)
      serverChannel.socket().bind(new InetSocketAddress(LocalServerSettings.getLocalServerPort()));
    else
      serverChannel.socket().bind(serverAddress);
    
    serverChannel.register(selector, SelectionKey.OP_ACCEPT, null);
    
    serverChannels.add(serverChannel);
    
    this.responderPool = responderPool;
    
    setDaemon(false);
    setName("MrSOA Receiver");
    setPriority(Thread.MAX_PRIORITY);
    
    start();
  }
  
  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        
        Iterator<SelectionKey> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
          SelectionKey key = keysIt.next();
          keysIt.remove();
          
          if (key.isAcceptable()) {
            if (responderPool.getPotentialResponders() <= 0) {
              log.warn("Closing connection because no responders are available");
              ((ServerSocketChannel)key.channel()).close();
            } else {
              log.debug("Accepting connection");
              SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
              //TODO: when swamped, close connection/send BUSY command
              channel.configureBlocking(false);
              channel.register(selector, SelectionKey.OP_READ);
            }
            continue;
          }
          
          if (key.isReadable()) {
            /* SYNC:
             * ? TcpHelperNio.readByte calls first and deadlocks
             *   - Since we're in this block, TcpHelperNio will be notified in
             *     a moment
             * ? TcpHelperNio.readByte calls after, and extra net event occurs
             *   - If a Responder or Acker receives a false net event, both
             *     will ignore it. */
            // Disable any more read events until the one that was just issued
            // is handled
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            /* SYNC:
             * ? MrSoaResponder.resetting nulls attachment first
             *   - A fresh Responder will be told to listen to this key.
             *   - If this event represents a new MrSOA request then everything
             *     is fine.
             *   - If this event is part of an old request, the fresh Responder
             *     will catch this as an error and react accordingly.
             * ? MrSoaResponder.forwardRequest switches attachment to Acker
             *   - Either we will get a Responder or an Acker, either way null
             *     is never falsely returned (because this code is single-
             *     threaded).
             *   - A Responder will not switch to an Acker unless it has
             *     received a complete and correct request so a switch will
             *     never occur mid-request.
             * ? MrSoaResponder.forwardRequest switches attachment to Responder
             *   - Either we will get a Responder or an Acker, either way null
             *     is never falsely returned.
             *   - A Responder can handle a SYN command intended for an
             *     Acker. */
            INetEventHandler responder = (INetEventHandler) key.attachment();
            if (responder == null) {
              responderPool.listenTo(key);
            } else {
              responder.netEvent();
            }
            continue;
          }
          
          if (key.isWritable()) {
            /* SYNC:
             * ? TcpHelperNio.sendByte calls first and deadlocks
             *   - Since we're in this block, TcpHelperNio will be notified in
             *     a moment.
             * ? TcpHelperNio.sendByte calls after and extra net event occurs
             *   - If a Responder or Acker receives a false net event, both
             *     will ignore it. */
            // Disable any more write events until the one that was just issued
            // is handled
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            /* SYNC:
             * ? MrSoaResponder.resetting nulls attachment first
             *   - The following error will log, and write events will be
             *     disabled so it will not happen again
             * ? MrSoaResponder.forwardRequest switches attachment to Acker
             *   - Either we will get a Responder or an Acker, either way null
             *     is never falsely returned (because this code is single-
             *     threaded).
             *   - A Responder will never attempt to switch to an Acker during
             *     the middle of sending a response.
             * ? MrSoaResponder.forwardRequest switches attachment to Responder
             *   - If an Acker is waiting to send an ACK and the attachment is
             *     switched to a Responder, the Acker will be reset first and
             *     give up trying to send the ACK. MrSOA clients will accept a
             *     RESPONSE command instead of an ACK command. If a Responder
             *     receives a net event before it is ready to send the
             *     response, it will be ignored. */
            INetEventHandler responder = (INetEventHandler) key.attachment();
            if (responder == null) {
              log.error("Internal error - network write event raised on non-existant Responder - ignoring");
              //key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            } else {
              responder.netEvent();
            }
            continue;
          }
        }
      } catch (Throwable e) {
        log.error("Error in main selector/receiver loop - ignoring", e);
      }
    }
    
    //TODO: should this be commented out?
//    try {
//      selector.close();
//      for (ServerSocketChannel channel : serverChannels)
//        channel.close();
//    } catch (IOException e) {
//      log.error("Error while closing network", e);
//    }
  }

  /**
   * Shutdown the receiver. Once shutdown a receiver may not be started again.
   */
  public void shutdown() {
    interrupt();
    selector.wakeup();
  }
}
