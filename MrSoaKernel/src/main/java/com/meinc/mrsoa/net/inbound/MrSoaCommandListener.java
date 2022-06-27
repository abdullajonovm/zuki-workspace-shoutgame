package com.meinc.mrsoa.net.inbound;

import static com.meinc.mrsoa.net.inbound.MrSoaCommandListener.ListenerState.INSTANTIATED;
import static com.meinc.mrsoa.net.inbound.MrSoaCommandListener.ListenerState.LISTENING;
import static com.meinc.mrsoa.net.inbound.MrSoaCommandListener.ListenerState.PROCESSING;
import static com.meinc.mrsoa.net.inbound.MrSoaCommandListener.ListenerState.STARTED;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.TcpHelperNio;

/**
 * Abstract base class that is run as a separate thread and listens to a
 * provided socket for a MrSOA command. Subclasses need only react to commands
 * via the {@link #processCommand(Socket, int)} method.
 * <p>
 * At any given time, instances of this class may exist in only one of four
 * states:
 * <ol><li>Instantiated</li>
 * <li>Started</li>
 * <li>Listening</li>
 * <li>Processing</li></ol>
 * <p>
 * <b>Instantiated</b><br>
 * The listener object has been created but not yet started in its own thread.
 * <p>
 * <b>Started</b><br>
 * The listener object has been started in its own thread but it has not yet
 * been provided a socket to {@link #listenTo(SelectionKey)}.
 * <p>
 * <b>Listening</b><br>
 * The listener object is listening to a provided socket for a command.  Once a
 * command has been received it will be provided to the subclass via the {@link
 * #processCommand(SelectionKey, int)} method.
 * <p>
 * <b>Processing</b><br>
 * The listener object has received a command and is processing it.  The
 * listener will automatically return to the Listening state.
 * <p>
 * Instances of this class will not stop listening to a socket until one of 
 * the following occurs:
 * <ol><li>The network connection fails</li>
 * <li>The client terminates the connection</li>
 * <li>The client sends a command that cannot be handled</li>
 * <li>The instance is explicitly {@link #reset()}</li>
 * <li>The instance is explicitly {@link #shutdown()}</li></ol>
 *
 * @author Matt
 */
abstract class MrSoaCommandListener extends Thread implements IResettable, INetEventHandler {
  private static Log log = LogFactory.getLog(MrSoaCommandListener.class);
  
  static enum ListenerState {
    INSTANTIATED,
    STARTED,
    LISTENING,
    PROCESSING
  }
  
  /**
   * The current state of the listener
   */
  private volatile ListenerState state;
  
  /**
   * Ignored - used for debugging
   */
  private static volatile int counter;
  
  /**
   * A synchronous monitor for pending listenTo commands.
   * @see #listenTo(SelectionKey)
   */
  private final Object listenToMonitor = new Object();
  
  /**
   * True if the listener is resetting states
   */
  protected boolean reset;
  
  /**
   * True if the listener has been shutdown
   */
  protected volatile boolean shutdown;
  
  /**
   * The listener's exclusive notifier
   */
  private NetEventNotifier notifier;
  
  /**
   * The current selection key assigned to the listener
   */
  protected SelectionKey listenerKey;
  
  /**
   * The buffer used for network read operations
   */
  protected ByteBuffer listenerReadBuffer;
  
  /**
   * The buffer used for network write operations
   */
  protected ByteBuffer listenerSendBuffer;
  
  /**
   * Ignored - used for debugging
   */
  protected volatile int id;

  /**
   * Create a listener instance with the provided Thread name.
   * 
   * @param threadName
   *          The name to assign to this Thread
   */
  public MrSoaCommandListener(String threadName) {
    init(threadName, null, null);
  }
  
  /**
   * Create a listener instance with the provided Thread name and read buffer.
   * 
   * @param threadName
   *          The name to assign to this Thread
   * @param readBuffer
   *          The read buffer to use for network read operations
   */
  public MrSoaCommandListener(String threadName, ByteBuffer readBuffer, ByteBuffer sendBuffer) {
    init(threadName, readBuffer, sendBuffer);
  }
  
  /**
   * Initialization method used by both constructors.
   * 
   * @param threadName
   *          The name to assign to this Thread
   * @param readBuffer
   *          The read buffer to use for network read operations
   */
  private void init(String threadName, ByteBuffer readBuffer, ByteBuffer sendBuffer) {
    state = INSTANTIATED;
    
    setDaemon(false);
    setName(threadName);
    id = counter++;
    
    notifier = new NetEventNotifier(this, id);
    
    if (readBuffer != null && sendBuffer != null) {
      this.listenerReadBuffer = readBuffer;
      this.listenerSendBuffer = sendBuffer;
    } else {
      this.listenerReadBuffer = ByteBuffer.allocateDirect(4096);
      TcpHelperNio.initBufferForFirstRead(this.listenerReadBuffer);
      this.listenerSendBuffer = ByteBuffer.allocateDirect(4096);
    }
  }
  
  /**
   * Notifies the listener that a network read or write event has occurred in
   * this listener's selector. This method blocks until the listener is able to
   * process such an event.
   * 
   * @see #netEventNow()
   */
  /* SYNC:
   * ? netEvent() called repeatedly in sequence
   *   - Doesn't matter as long as the Listener is notified when *at least* one
   *     event occurs.
   * ? netEvent and listenTo are both called before Listener thread wakes up
   *   - It is impossible for both to be called before Listener wakes up:
   *     1. If listenTo(){1} is entered, then run() is blocking any netEvent()
   *        calls
   *     2. If netEvent() is entered, then run(){1} is blocking listenTo(){1}
   * ? netEvent and reset are both called before Listener thread wakes up
   *   - TcpHelperNio.readByte would wake up and immediately detect the reset 
   *     causing the Listener to reset and ignore the net event. See next two 
   *     points.
   *   - If this is an Acker:
   *     1. If the net event came from an incoming SYN command, the Responder 
   *        will soon receive it and handle it appropriately.
   *     2. If the net event came from a pending ACK command, the Responder 
   *        will soon send a response instead of an ACK and the MrSoa client 
   *        will handle it appropriately.
   *   - If this is a Responder:
   *     1. If a net read event arrived -> In practice, this will never happen
   *        because once a Responder is assigned a channel via listenTo(), it
   *        will never be reset via the reset() method. Rather, listening
   *        Responders are only ever reset via an incoming CLOSE/FATAL command,
   *        the TCP connection dying, the current thread dying (server
   *        shutdown), or an internal exception. In any case, a resetting
   *        Responder will re-enable net read events in resetting() for another
   *        Responder to handle. If another Responder cannot make sense of the
   *        incoming data, such as in the case of a request being interrupted
   *        partway through), it will handle it as an error accordingly.
   *     2. If a net write event arrived -> In practice, this will never happen
   *        (see previous point). However, in the case of an internal error,
   *        particularly an error which produces no Java exception (such as a
   *        theoretical bug where a buffer is not flushed completely yet the
   *        response is considered complete), there is no total solution.  This
   *        issue can only be managed by careful and thorough coding and
   *        testing.
   * - Deadlock
   *   ? netEvent() never releases monitor -> Method always releases monitor
   *   ? reset() never releases monitor -> Method always releases monitor
   *   ? isReset() never releases monitor -> Method always releases monitor
   *   ? isStarted() never releases monitor -> Method always releases monitor
   *   ? run() never releases monitor -> See run().
   *   ? MrSoaResponder.forwardRequest{1} never releases monitor -> Method
   *     could only block on listenTo(). See listenTo().
   *   ? MrSoaResponder.forwardRequest{2} never releases monitor -> Method
   *     could only block on reset(). See reset().
   *   ? IMrSoaService.invokeMethod hangs -> This is outside the contract of
   *     this class.
   *   ? Acker.resetting never releases monitor -> Method always releases 
   *     monitor */
  public synchronized void netEventBlocking() {
    notify();
  }
  
  /**
   * Notifies the listener that a network read or write event has occurred in
   * this listener's selector. This method queues the event and does not block.
   * 
   * @see #netEventBlocking()
   */
  public void netEvent() {
    notifier.netEvent();
  }
  
  /**
   * Begin listening to the specified selection key for MrSOA commands. This
   * method blocks until the listener is able to process such a command.
   * 
   * @see #listenToNow(SelectionKey)
   * @param listenToKey
   *          The selection key to listen to
   */
  public void listenTo(SelectionKey listenToKey) {
    while (!isInterrupted()) {
    /* SYNC:
     * ? listenTo() is called repeatedly before Listener wakes up
     *   - The first thread to enter the block assigns a value to listenerKey,
     *     every thread thereafter will loop. This should never happen in
     *     practice as listenTo is never called twice on one Listener.
     * ? netEvent and listenTo are both called before Listener thread wakes up
     *   - See netEvent()
     * ? run() never nulls listenToKey and thus never releases monitor
     *   - Unless processCommand() hangs (which is outside the scope of the
     *     contract of this class), listenToKey will always be nulled
     * - Deadlock
     *   ? run() never releases monitor -> See run(){1}
     */
      synchronized (listenToMonitor) {
        if (listenerKey != null) {
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
            interrupt();
          }
          continue;
        }

        listenerKey = listenToKey;
        listenToMonitor.notify();
      }
      break;
    }
  }
  
  /**
   * Begin listening to the specified selection key for MrSOA commands. This
   * method queues up to one key and does not block unless the queue is full.
   * 
   * @see #listenTo(SelectionKey)
   * @param listenToKey
   *          The selection key to listen to
   */
  public void listenToNow(SelectionKey listenToKey) {
    notifier.listenTo(this, listenToKey);
  }

  /**
   * Reset the listener to the Started state. This method blocks until the
   * listener is able to process the reset command.
   */
  /* SYNC:
   * - See netEvent()
   * ? Method is called repeatedly before Listener wakes up
   *   - We only care if *at least* one reset occurred while Listener was
   *     waiting
   * ? netEvent() and reset() called before Listener wakes up
   *   - run() can only relinquish the object monitor in processCommand()
   *     (outside the scope of the contract of this class) and in
   *     TcpHelperNio.readByte(). The latter is designed to detect the reset
   *     condition of the Listener and immediately return. See
   *     TcpHelperNio.readByte() */
  public synchronized void reset() {
    this.reset = true;
    notify();
  }
  
  /**
   * Stop the listener and return it to the Instantiated state. If a listener
   * is reading from a channel when this method is invoked, the channel may
   * be terminated. Once shutdown, a listener may never be started again.
   */
  public void shutdown() {
    shutdown = true;
    interrupt();
    if (state == STARTED) {
      /* SYNC:
       * ? run() doesn't release listenToMonitor in a timely fashion
       *   - Since we know the shutdown flag was set while the Listener was
       *     in the STARTED state, either run() will wait on listenToMonitor
       *     soon or the second "while(isHealthy())" will cause the run()
       *     method to exit soon at which point listenToMonitor will be
       *     released. */
      synchronized (listenToMonitor) {
        listenToMonitor.notify();
      }
    }
  }
  
  /**
   * Determine if this listener is in the Started or any later state, and has
   * not been commanded to return to the Instantiated state.
   * 
   * @return True if started or later, False otherwise
   */
  public boolean isHealthy() {
    return isAlive() && !isInterrupted() && !shutdown;
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.IResettable#isReset()
   */
  /* SYNC: See netEvent() */
  @Override
  public synchronized boolean isReset() {
    return reset || shutdown;
  }
  
  /**
   * Returns whether the listener is in the Started state.
   * 
   * @return True if in the Started state
   */
  /* SYNC: See netEvent() */
  public synchronized boolean isStarted() {
    return state == STARTED;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  /* SYNC:
   * - Deadlock: See netEvent()
   * - Method never releases monitor
   *   ? If in STARTED state -> This is as designed. Shutdown() will still
   *   work.  */
  public synchronized void run() {
    /* SYNC:
     * ? Notifier does not start fast enough to process commands
     *   - See NetEventNotifier.run() */
    notifier.start();
    
    /* SYNC:
     * - Deadlock
     *   ? listenTo() never releases monitor -> Method always releases monitor
     * ? Block never releases monitor
     *   - Upon entering STARTED state, if a listenerKey has not already been
     *     assigned, block will always release monitor.  Subsequent states will
     *     not release monitor if they block - this is as designed. Listen-to
     *     commands should block until the Listener can actually listen to the
     *     provided key. Upon re-entering the STARTED state, block is
     *     guaranteed to release monitor. */
    synchronized (listenToMonitor) {
      while (isHealthy()) {
        try {
          state = STARTED;
          
          try {
            reset = false;
            if (shutdown) continue;
            /* SYNC:
             * ? listenerKey changes after
             *   - listenerKey cannot change because we own listenToMonitor
             * ? reset after
             *   - Cannot occur until the current monitor is released
             * ? shutdown just before
                 - See shutdown() */
            if (listenerKey == null) {
              listenToMonitor.wait();
            }
            if (listenerKey == null || shutdown)
              continue;

          } catch (InterruptedException e) {
            interrupt();
            continue;
          }
          
          state = LISTENING;

          // Continually read commands until explicitly stopped
          while (isHealthy()) {
            // Read command from network
            int command;
            try {
              command = TcpHelperNio.readByte(listenerKey, listenerReadBuffer, this, id);
            } catch (InterruptedException e) {
              interrupt();
              continue;
            } catch (IOException e) {
              /* SYNC:
               * ? Acker catches error here
               *   - Acker will wait for explicit reset in resetting() thusly 
               *     avoiding a Responder deadlock when it tries to reset the Acker
               * ? Responder catches error here
               *   - Responder will reset */
              log.error("Error while reading request command", e);
              TcpHelperNio.fatalizeConnection(listenerKey, listenerSendBuffer, "Error while reading request command", this);
              break;
            } catch (Exception e) {
              log.error("Network read error", e);
              break;
            }
            
            // Something reset TcpHelperNio while it was waiting for data
            if (command == -2) {
              if (shutdown || reset)
                break;
              log.error("Internal error: -2 returned from readByte without reset or shutdown - ignoring");
              continue;
            }
            
            state = PROCESSING;

            boolean proceed = false;
            try {
              /* SYNC:
               * ? processCommand() never returns
               *   - This is outside the scope of the contract of this class */
              proceed = processCommand(listenerKey, command);
            } catch (Throwable e) {
              // Subclasses should not allow exceptions to reach this point
              log.error(e.getMessage(), e);
            }
            
            state = LISTENING;

            SocketChannel channel = (SocketChannel) listenerKey.channel();
            if (!proceed || !channel.isOpen() || !channel.isConnected())
              break;
          } // end of listening while loop
          
          try {
            /* SYNC:
             * ? resetting() never returns
             *   - This is outside the scope of the contract of this class */
            resetting();
          } catch (Throwable e) {
            // Subclasses should not allow exceptions to reach this point
            log.error(e.getMessage(), e);
          }
        } finally {
          listenerKey = null;
        }
      } // end of main while loop
    } // end of listenToMonitor sync
    
    try {
      /* SYNC:
       * ? closing() never returns
       *   - This is outside the scope of the contract of this class */
      closing();
    } catch (Throwable e) {
      // Subclasses should not allow exceptions to reach this point
      log.error(e.getMessage(), e);
    }

    state = INSTANTIATED;

    /* SYNC:
     * ? Notifier delays termination and attempts to send commands to this
     *   closed Listener
     *   - See NetEventNotifier.listenTo()/netEvent() */
    notifier.interrupt();
  }

  /**
   * Processes a MrSOA command that was just received.
   * 
   * @param key
   *          The selection key through which the command was received
   * @param command
   *          The command that was received
   * @return True if the listener should stay in the Listening state, False if
   *         the listener should return to the Started state.
   */
  protected abstract boolean processCommand(SelectionKey key, int command);
  
  /**
   * Called just before this listener is returned to the Started state.
   * <p>
   * <em>Note:</em> This method may be called during a shutdown event just
   * before {@link #closing()} is called
   */
  protected abstract void resetting();
  
  /**
   * Called just before this listener is returned to the Instantiated state.
   */
  protected abstract void closing();
}
