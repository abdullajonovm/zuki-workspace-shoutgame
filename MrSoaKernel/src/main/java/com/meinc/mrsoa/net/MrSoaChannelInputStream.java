package com.meinc.mrsoa.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.meinc.mrsoa.net.inbound.IResettable;

/**
 * Provides a traditional InputStream backed by a Java NIO network channel.
 * 
 * @author Matt
 */
public class MrSoaChannelInputStream extends InputStream {
  private SelectionKey key;
  private ByteBuffer buffer;
  private IResettable waitMonitor;
  private int id;
  
  /**
   * Creates a new instance using Java NIO network objects.
   * 
   * @param key
   *          The selector key for the current network read operation
   * @param buffer
   *          The byte buffer for the current network read operation. This
   *          buffer should not be manipulated in any way during a stream read
   *          operation.
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data is
   *          available to read from the network
   * @param id
   *          Ignored - used for debugging
   */
  public MrSoaChannelInputStream(SelectionKey key, ByteBuffer buffer, IResettable waitMonitor, int id) {
    this.key = key;
    this.buffer = buffer;
    this.waitMonitor = waitMonitor;
    this.id = id;
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public synchronized int read() throws IOException {
    try {
      return TcpHelperNio.readByte(key, buffer, waitMonitor, id);
    } catch (InterruptedException e) {
      throw new IOException("Network read interrupted", e);
    }
  }
}
