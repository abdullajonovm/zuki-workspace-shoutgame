package com.meinc.mrsoa.net.inbound;

import java.nio.channels.SelectionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Accepts and queues commands to a {@link MrSoaCommandListener} so that the
 * client issuing the command will return (not block) as quickly as possible.
 * <p>
 * Typically one Selector issues commands to all listeners and so in order to
 * keep the MrSOA server as responsive as possible, no listener should delay
 * the Selector.
 * 
 * @author Matt
 */
public class NetEventNotifier extends Thread implements INetEventHandler {
  private static final Log log = LogFactory.getLog(NetEventNotifier.class);
  
  private Object notifyMonitor;
  private boolean eventFlag;
  private MrSoaCommandListener listener;
  private SelectionKey listenerKey;
  private int id;
  
  /**
   * Creates a new instance using the parent listener's synchronous monitor.
   * The monitor will be notified when new commands are isssued.
   * 
   * @param notifyMonitor
   *          The parent listener's monitor
   * @param id
   *          Ignored - used for debugging
   */
  public NetEventNotifier(Object notifyMonitor, int id) {
    this.notifyMonitor = notifyMonitor;
    this.id = id;
    setName("Responder Network Event Notifier");
    setDaemon(false);
    setPriority(Thread.NORM_PRIORITY+1);
  }
  
  /**
   * Add a network read or write event to the queue.
   * @see MrSoaCommandListener#netEvent()
   */
  /* SYNC: See listenTo(){1} */
  public synchronized void netEvent() {
    eventFlag = true;
    /* SYNC:
     * ? netEvent is called multiple times before notifier wakes up
     *   - Doesn't matter as long as the Listener is notified when *at least*
     *     one event occurs.
     * ? netEvent and listenTo are called before notifier wakes up
     *   - Listen-to and net events are tracked separately. */
    notify();
  }
  
  /**
   * Assign a selector key to a listener
   * 
   * @see MrSoaCommandListener#listenToNow(SelectionKey)
   * @param listener
   *          The listener to assign the key
   * @param listenerKey
   *          The selector key
   */
  public void listenTo(MrSoaCommandListener listener, SelectionKey listenerKey) {
    while (!isInterrupted()) {
      /* SYNC:
       * - Deadlock
       *   ? If notifier never relinquishes monitor -> There are no blocking
       *     operations in synced block of notifier, only variable assignments.
       *   ? If notifier fails to nullify listenerKey and listenTo() endlessly
       *     loops -> Notifier nullifies in all cases. Notifier ignores
       *     exceptions and will never stop processing listenerKey on its own.
       *   ? If netEvent() never relinquishes monitor -> There are no blocking
       *     operations in netEvent() */
      synchronized (this) {
        if (this.listenerKey != null) {
          // This should never happen in practice because a listener is only
          // given a listenerKey once per activation from the pool, but just in
          // case...
          try {
            Thread.sleep(1); // Limit the damage of many loops
          } catch (InterruptedException e) {
            interrupt();
          }
          continue;
        }
          
        this.listener = listener;
        this.listenerKey = listenerKey;
        /* SYNC:
         * ? listenTo is called multiple times before notifier wakes up
         *   - The above if statement will block until the first listenTo event
         *     is handled. This should never happen in practice.
         * ? netEvent and listenTo are called before notifier wakes up
         *   - Listen-to and net events are tracked separately. */
        notify();
      }
      break;
    }
  }
  
  private boolean myEventFlag = false;
  private MrSoaCommandListener myListener = null;
  private SelectionKey myListenerKey = null;
  
  /* SYNC:
   * ? Notifier does not start fast enough to process commands for the Listener
   *   - Notifier can queue commands before the Notifier thread starts that
   *     will be handled after it starts
   * ? Notifier thread delays termination and attempts to send commands to a
   *   closed Listener
   *   - The Listener will have released notifyMonitor so the Notifier will not
   *     be blocked in sending any posthumous messages. Notifier messages to a
   *     closed listener will be ignored just as if they were sent to the
   *     Listener directly. */
  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        /* SYNC:
         * - Deadlock
         *   ? If listenTo() never relinquishes monitor -> Even in worst case
         *     (and highly unlikely) scenario where listenTo() blocks, it
         *     repeatedly releases the monitor.
         *   ? If netEvent() never relinquishes monitor -> There are no
         *     blocking operations in netEvent()
         *   ? NoSer other code syncs on this object */
        synchronized (this) {
          if (!eventFlag && listenerKey == null) {
            try {
              /* SYNC:
               * ? netEvent and listenTo are both called during
               *   - Listen-to and net events are tracked separately. */
              wait();
            } catch (InterruptedException e) {
              interrupt();
              continue;
            }
          }
          /* SYNC:
           * ? listenerKey changes after test
           *   - All access to listenerKey is synced */
          if (listenerKey != null) {
            myListener = listener;
            myListenerKey = listenerKey;
            listener = null;
            listenerKey = null;
          }
          /* SYNC:
           * ? eventFlag changes after test
           *   - All access to eventFlag is synced */
          if (eventFlag) {
            myEventFlag = true;
            eventFlag = false;
          }
        }

        /* SYNC:
         * ? myListenerKey is changed by another thread
         *   - myListenerKey is a private attribute - only one thread will ever
         *     access it. */
        if (myListenerKey != null) {
          myListener.listenTo(myListenerKey);
          myListener = null;
          myListenerKey = null;
        }
        
        /* SYNC:
         * ? myEventFlag is changed by another thread
         *   - myEventFlag is a private attribute - only one thread will ever
         *     access it. */
        if (myEventFlag) {
          /* SYNC:
           * ? notifyMonitor is changed after
           *   - notifyMonitor is assigned in constructor and never changed
           * - Deadlock
           *   ? MrSoaCommandListener.netEvent never releases monitor -> Method
           *     always releases monitor
           *   ? MrSoaCommandListener.listenTo never releases monitor -> Method
           *     always releases monitor
           *   ? MrSoaCommandListener.reset never releases monitor -> Method
           *     always releases monitor
           *   ? MrSoaCommandListener.shutdown never releases monitor -> Method
           *     always releases monitor
           *   ? MrSoaCommandListener.isReset never releases monitor -> Method
           *     always releases monitor
           *   ? MrSoaCommandListener.isStarted never releases monitor ->
           *     Method always releases monitor
           *   ? MrSoaCommandListener.run never releases monitor ->
           *     See MrSoaCommandListener.run.
           *   ? Acker.resetting never releases monitor -> Method always 
           *     releases monitor */
          synchronized (notifyMonitor) {
            notifyMonitor.notify();
          }
          myEventFlag = false;
        }
      } catch (Throwable e) {
        log.error("Error in net event loop - ignoring error", e);
      }
    }
  }
}
