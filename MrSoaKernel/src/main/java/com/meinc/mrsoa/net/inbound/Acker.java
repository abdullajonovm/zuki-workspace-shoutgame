package com.meinc.mrsoa.net.inbound;

import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.SEND_AND_FLUSH;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.TcpHelperNio;
import com.meinc.mrsoa.net.TcpHelperNio.SendMode;

/**
 * Responds to a SYN command sent from a MrSOA client by returning an ACK
 * command.
 * <p>
 * Acker objects are intended to run in their own threads while service
 * application code is executing.
 * 
 * @author Matt
 */
class Acker extends MrSoaCommandListener {
  private static Log log = LogFactory.getLog(Acker.class);
  
  /**
   * Creates a new instance using the provided read buffer. In most instances an
   * Acker will share a read buffer with its parent Responder.
   * 
   * @param readBuffer
   *          The read buffer to use
   */
  public Acker(ByteBuffer readBuffer, ByteBuffer sendBuffer) {
    super("MrSOA Request Acker", readBuffer, sendBuffer);
    setPriority(Thread.MAX_PRIORITY);
    setDaemon(false);
    start();
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#processCommand(java.net.Socket, int)
   */
  @Override
  protected boolean processCommand(SelectionKey key, int command) {
    if (command == TcpHelperNio.SYN) {
      // received SYN - respond with ACK
      try {
        /* SYNC:
         * ? Acker throws error from here
         *   - Method returns false which causes Listener to reset. Containing Responder will probably deadlock trying to reset us */
        TcpHelperNio.sendByte(key, listenerSendBuffer, TcpHelperNio.ACK, this, SEND_AND_FLUSH, id);
      } catch (Throwable e) {
        log.error("Error while sending ACK", e);
        TcpHelperNio.fatalizeConnection(key, listenerSendBuffer, "Error while sending ACK", this);
        return false;
      }
    } else {
      // received bad command - close socket
      log.error("Acker received illegal command: " + command);
      TcpHelperNio.fatalizeConnection(key, listenerSendBuffer, "Illegal command: " + command, this);
      return false;
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#resetting()
   */
  @Override
  protected synchronized void resetting() {
    // If we were not explicitly reset by our containing Responder then we must
    // wait for it, otherwise, our Responder will deadlock when it tries to
    // reset us
    while (!reset) {
      try {
        /* SYNC:
         * ? MrSoaResponder/NetEventNotifier repeatedly sends notice of a
         *   network event and effectively deadlocks Responder from explicitly
         *   resetting us
         *   - MrSoaResponder automatically disables channel read events after
         *     receiving the first one so this is not a problem.
         * ? MrSoaResponder/NetEventNotifier sends net event that gets ignored
         *   here
         *   - If we are here, then an error occurred that is causing us to
         *     reset before we were explicitly instructed to do so. In other
         *     words, we are in a bad state and cannot deal with net events
         *     right now, so ignoring them is acceptable. In practice, the
         *     Responder will eventually reset us and eventually attempt to
         *     perform a network operation at which point it can handle the
         *     situation. */
        wait();
      } catch (InterruptedException e) {
        interrupt();
        break;
      }
    }
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#closing()
   */
  @Override
  protected void closing() {
    // Do nothing
  }
}
