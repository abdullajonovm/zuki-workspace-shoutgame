package com.meinc.mrsoa.net;

import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.BUFFER_AND_SEND;
import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.JUST_FLUSH;
import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.SEND_AND_FLUSH;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.inbound.IResettable;

/**
 * Utility class that contains methods for sending or receiving TCP messages to
 * or from a MrSOA server using Java NIO operations.
 * 
 * @author Matt
 */
public class TcpHelperNio {
  private static final Log log = LogFactory.getLog(TcpHelperNio.class);
  
  //TODO: extract these constants from TcpHelper and TcpHelperNio into one place
  // Request commands
  public static final int SYN = 0;
  public static final int REQUEST = 1;
  public static final int CLIENT_CLOSE = 90;
  public static final int CLIENT_FATAL = 91;
  
  // Response commands
  public static final int ACK = 0;
  public static final int RESPONSE = 1;
  public static final int SERVER_CLOSE = 90;
  public static final int SERVER_FATAL = 91;
  
  public static enum SendMode {
    /** Buffer these bytes until the buffer is full then flush the buffer */
    BUFFER_AND_SEND,
    /** Buffer these bytes and then immediately flush the buffer */
    SEND_AND_FLUSH,
    /** Ignore these bytes and just flush the buffer */
    JUST_FLUSH
  }
  
  /**
   * Send a byte to a remote process. This method blocks until the provided byte
   * can be transmitted on the network, or until some other event interrupts
   * this method.
   * 
   * @param key
   *          The selector key for the current network write operation
   * @param buffer
   *          The byte buffer for the current network write operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param b
   *          The byte to send
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data can be
   *          written to the network
   * @param id
   *          Ignored - used for debugging
   * @throws IOException
   *           Upon network error
   * @throws InterruptedException
   *           If the current thread was interrupted
   */
  public static void sendByte(SelectionKey key, ByteBuffer buffer, int b, Object waitMonitor, SendMode mode, int id)
  throws IOException, InterruptedException {
    
    if (mode == BUFFER_AND_SEND && buffer.hasRemaining()) {
      buffer.put((byte) b);
      return;
    }
    
    SocketChannel channel = (SocketChannel) key.channel();
    buffer.flip();
    boolean extraByteSent = false;
    while (buffer.hasRemaining()) {
      int bytesWritten = channel.write(buffer);
      
      if (bytesWritten == -1)
        throw new IOException("Connection was closed");
      
      if (bytesWritten == 0) {
        /* SYNC:
         * - Deadlock
         *   ? waitMonitor blocks forever -> This problem is outside the
         *     contract of this method. That said, at present, the only two
         *     clients of this method are MrSoaResponder and Acker which both
         *     check out. */
        synchronized (waitMonitor) {
          // Enable the selector to write events for this
          // channel
          /* SYNC: See MrSoaReceiver.run() */
          key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
          // Make sure the selector does not ignore our interest change because
          // it is hibernating in select().
          key.selector().wakeup();
          
          /* SYNC:
           * ? Waits forever
           *   - If the network write is blocked forever then we should wait
           *     forever. In practice, if the client is not reading bytes then
           *     there is a bug somewhere in the client code. The problem on
           *     our end is a side affect - blame the client.
           *   - Our associated server classes should not block forever. See
           *     MrSoaReceiver, MrSoaCommandListener, MrSoaResponder, Acker. */
          waitMonitor.wait();
          continue;
        }
      }
      
      if (buffer.hasRemaining()) {
        continue;
      } else if (extraByteSent) {
        break;
      } else if (mode != JUST_FLUSH) {
        if (mode == SEND_AND_FLUSH) {
          buffer.position(0);
          buffer.limit(1);
          buffer.put((byte) b);
          extraByteSent = true;
          buffer.flip();
          continue;
        } else { // mode == BUFFER_AND_SEND
          buffer.clear();
          buffer.put((byte) b);
          return;
        }
      }
    }
    
    buffer.clear();
  }
  
  /**
   * Serialize and send an object to a remote MrSOA process. This method blocks
   * until the specified payload can be sent to the network, or until some other
   * event interrupts this method.
   * 
   * @param key
   *          The selector key for the current network write operation
   * @param buffer
   *          The byte buffer for the current network write operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param payload
   *          The object to send
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data can be
   *          written to the network
   * @param id
   *          Ignored - used for debugging
   * @throws IOException
   *           Upon network error
   */
  public static void sendPayload(SelectionKey key, ByteBuffer buffer, Object payload, Object waitMonitor, int id)
  throws IOException {
    MrSoaChannelOutputStream netOut = new MrSoaChannelOutputStream(key, buffer, waitMonitor, id);
    ObjectOutputStream objOut = new ObjectOutputStream(netOut);
    objOut.writeObject(payload);
    objOut.close();
  }
  
  /**
   * Initializes a buffer so that it will report no bytes available upon the
   * first attempt to read from it.
   * 
   * @param buffer
   *          The buffer to initialize
   */
  public static void initBufferForFirstRead(ByteBuffer buffer) {
    //buffer.position(buffer.limit());
    buffer.clear();
    buffer.limit(0);
  }
  
  /**
   * Read a byte from a remote MrSOA process. This method will block until
   * either a byte is successfully read or some other event interrupts this
   * method.
   * 
   * @param key
   *          The selector key for the current network read operation
   * @param buffer
   *          The byte buffer for the current network read operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data is
   *          available to read from the network
   * @param id
   *          Ignored - used for debugging
   * @return The byte read from the network, -1 if the connection has closed, or
   *         -2 if the waitMonitor was reset while network read was blocked
   * @throws IOException
   *           Upon network error
   * @throws InterruptedException
   *           If the current thread was interrupted
   */
  public static int readByte(SelectionKey key, ByteBuffer buffer, IResettable waitMonitor, int id)
  throws IOException, InterruptedException {
    if (!buffer.hasRemaining()) {
      SocketChannel channel = (SocketChannel) key.channel();
      while (true) {
        buffer.clear();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
          initBufferForFirstRead(buffer);
          return -1;
          //TODO: move this test to the first position for performance
        } else if (bytesRead > 0) {
          buffer.flip();
          break;
          // Bytes read is zero
        } else {
          initBufferForFirstRead(buffer);
          // Wait for the selector to notify us of ready bytes
          /* SYNC:
           * - Deadlock
           *   ? waitMonitor blocks forever -> This problem is outside the
           *     contract of this method. That said, at present, the only two
           *     clients of this method are MrSoaResponder and Acker which both
           *     check out. */
          synchronized (waitMonitor) {
            // Enable the selector to receive network events for this
            // channel
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            // Make sure the selector does not ignore our interest change
            // because it is hibernating in select().
            key.selector().wakeup();
            
            /* SYNC:
             * ? Waits forever
             *   - If the network read is blocked forever then we should wait
             *     forever. In practice, if the client is not writing bytes then
             *     there is a bug somewhere in the client code or there is a
             *     bug in our server business logic - the blame goes to one of
             *     them.
             *   - Our associated server classes should not block forever. See
             *     MrSoaReceiver, MrSoaCommandListener, MrSoaResponder, Acker. */
            waitMonitor.wait();
            
            /* SYNC:
             * ? Listener.reset() is called after this test 
             *   - Impossible because Listener.reset() will block until the
             *     waitMonitor is released in the wait statement above. Even
             *     after leaving the sync block, waitMonitor is still locked by
             *     our, at present, three clients MrSoaCommandListener,
             *     MrSoaResponder, and Acker.
             */
            if (waitMonitor.isReset()) {
              return -2;
            }
          }
          continue;
        }
      }
    }
    
    // Safely convert unsigned byte to signed int
    return buffer.get() & 0xFF;
  }
  
  /**
   * Read and deserialize an object from a remote MrSOA process. This method
   * will block until either a byte is successfully read or some other event
   * interrupts this method.
   * 
   * @param key
   *          The selector key for the current network read operation
   * @param buffer
   *          The byte buffer for the current network read operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param classloader
   *          The classloader to use to deserialize
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data is
   *          available to read from the network
   * @param id
   *          Ignored - used for debugging
   * @return The object read from the network
   * @throws IOException
   *           Upon network error
   * @throws ClassNotFoundException
   *           If the classloader could not find the object's class
   */
  public static Object readPayload(SelectionKey key, ByteBuffer buffer, ClassLoader classloader, IResettable waitMonitor, int id)
  throws IOException, ClassNotFoundException {
    MrSoaChannelInputStream netIn = new MrSoaChannelInputStream(key, buffer, waitMonitor, id);
    
    ObjectInputStream objIn = 
      new MrSoaObjectInputStream(classloader, netIn);
    
    return objIn.readObject();
  }
  
  /**
   * Send a CLOSE command to a remote MrSOA process.
   * 
   * @param key
   *          The selector key for the current network read operation
   * @param buffer
   *          The byte buffer for the current network write operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data is
   *          available to read from the network
   */
  public static void closeConnection(SelectionKey key, ByteBuffer buffer, Object waitMonitor) {
    try {
      sendByte(key, buffer, SERVER_CLOSE, waitMonitor, SEND_AND_FLUSH, -1);
    } catch (Exception e) {
      //log.warn("Error while closing connection", e);
    } finally {
      try {
        key.channel().close();
      } catch (IOException e) { }
      key.cancel();
    }
  }

  /**
   * Send a FATAL command to a remote MrSOA process.
   * 
   * @param key
   *          The selector key for the current network read operation
   * @param buffer
   *          The byte buffer for the current network write operation. This
   *          buffer should not be manipulated in any way during this operation.
   * @param errorMessage
   *          The error message to send to the remote server
   * @param waitMonitor
   *          The Java synchronization monitor to block on when no data is
   *          available to read from the network
   */
  public static void fatalizeConnection(SelectionKey key, ByteBuffer buffer, String errorMessage, Object waitMonitor) {
    try {
      sendByte(key, buffer, SERVER_FATAL, waitMonitor, BUFFER_AND_SEND, -1);
      sendPayload(key, buffer, errorMessage, waitMonitor, -1);
    } catch (Exception e) {
      //log.warn("Error while closing connection", e);
    } finally {
      try {
        key.channel().close();
      } catch (IOException e) { }
      key.cancel();
    }
  }
}
