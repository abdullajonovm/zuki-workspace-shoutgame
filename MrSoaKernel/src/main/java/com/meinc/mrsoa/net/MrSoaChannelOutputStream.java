package com.meinc.mrsoa.net;

import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.BUFFER_AND_SEND;
import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.JUST_FLUSH;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.meinc.mrsoa.net.TcpHelperNio.SendMode;

/**
 * Provides a traditional OutputStream backed by a Java NIO network channel.
 * 
 * @author Matt
 */
public class MrSoaChannelOutputStream extends OutputStream {
  private SelectionKey key;
  private ByteBuffer buffer;
  private Object waitMonitor;
  private int id;
  
  /**
   * Creates a new instance using Java NIO network objects.
   * 
   * @param key
   *          The selector key for the current network write operation
   * @param buffer
   *          The byte buffer for the current network write operation. This
   *          buffer should not be manipulated in any way during a stream write
   *          operation.
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data can
   *          be written to the network
   * @param id
   *          Ignored - used for debugging
   */
  protected MrSoaChannelOutputStream(SelectionKey key, ByteBuffer sendBuffer, Object waitMonitor, int id)
  throws IOException, SecurityException {
    this.key = key;
    this.buffer = sendBuffer;
    this.waitMonitor = waitMonitor;
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int b) throws IOException {
    try {
      TcpHelperNio.sendByte(key, buffer, b, waitMonitor, BUFFER_AND_SEND, id);
    } catch (InterruptedException e) {
      throw new IOException("Network write interrupted", e);
    }
  }

  @Override
  public void flush() throws IOException {
    try {
      TcpHelperNio.sendByte(key, buffer, -1, waitMonitor, JUST_FLUSH, id);
    } catch (InterruptedException e) {
      throw new IOException("Network write interrupted", e);
    }
  }
}
