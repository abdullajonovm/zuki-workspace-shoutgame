package com.meinc.mrsoa.net.inbound;

import static com.meinc.mrsoa.net.TcpHelperNio.SendMode.BUFFER_AND_SEND;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.net.MrSoaNetworkingException;
import com.meinc.mrsoa.net.TcpHelperNio;

/**
 * Responds to MrSOA commands sent from clients to a service. This class handles
 * the majority of the actual handshaking required between the client and the
 * server.
 * 
 * @author Matt
 */
public class MrSoaResponder extends MrSoaCommandListener {

  private static final Log log = LogFactory.getLog(MrSoaResponder.class);
 
  /**
   * Each responder has its own acker instance that stays alive as long as its
   * responder does
   */
  private Acker acker = new Acker(listenerReadBuffer, listenerSendBuffer);
  
  /**
   * The responder pool from whence came this responder
   */
  private MrSoaResponderPool responderPool;

  private static Random random = new Random();
  
  /**
   * Create a new responder using the refrenced responder pool
   * 
   * @param responderPool
   *          The responder pool
   */
  public MrSoaResponder(MrSoaResponderPool responderPool) {
    super(Long.toString(System.currentTimeMillis() + random.nextLong()));
    this.responderPool = responderPool;
    setDaemon(false);
    start();
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#processCommand(java.net.Socket, int)
   */
  @Override
  protected boolean processCommand(SelectionKey key, int command) {
//    if (log.isTraceEnabled())
//      log.trace("Received command: " + command);
    
    // Must be a leftover SYN from the last request processed using this 
    // socket. Ignore it.
    if (command == TcpHelperNio.SYN)
      return true;
    
    MrSoaResponse response = null;
    try {
      MrSoaRequest request = null;
      request = readRequest(key, command);
      response = forwardRequest(key, request);

    } catch (MrSoaNetworkingException e) {
      // The exception is already logged and the connection closed
      return false;

    } catch (MrSoaInternalResponderException e) {
      Throwable e2 = e.getCause();
      log.error(e2.getMessage(), e2);
      response = new MrSoaResponse();
      packageResponseException(response, e2);
    }

    boolean success;
    try {
      success = sendResponse(key, response);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      try {
        key.channel().close();
      } catch (IOException e1) { }
      return false;
    }
    
    return success;
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#resetting()
   */
  @Override
  protected void resetting() {
    if (listenerKey.isValid()) {
      // Allow future requests over this key to attach to another Responder
      listenerKey.attach(null);
      // Allow future requests to fire events to be assigned another Responder.
      // This line must come after detaching this Responder so that no net events
      // fire into this Responder.
      listenerKey.interestOps(listenerKey.interestOps() | SelectionKey.OP_READ);
      // Push this Responder back into the pool for future use
    }
    
    // Prepare buffers for next time we are asked to listen to a channel
    TcpHelperNio.initBufferForFirstRead(listenerReadBuffer);
    listenerSendBuffer.clear();
    
    responderPool.markIdle(this);
  }

  /* (non-Javadoc)
   * @see com.meinc.mrsoa.net.inbound.MrSoaCommandListener#closing()
   */
  @Override
  protected void closing() {
    responderPool.markClosed(this);
    acker.shutdown();
  }
  
  /**
   * Reads and deserializes a MrSOA request object from the provided socket.
   * 
   * @param key
   *          The provided socket
   * @param command
   *          The command of the request
   * @return The resultant MrSOA request
   * @throws MrSoaInternalResponderException
   */
  private MrSoaRequest readRequest(SelectionKey key, int command) throws MrSoaInternalResponderException {
    try {
      switch (command) {
        case TcpHelperNio.REQUEST:
          Object request = null;
          try {
            request = TcpHelperNio.readPayload(key, listenerReadBuffer, MrSoaRequest.class.getClassLoader(), this, id);
          } catch (IOException e) {
            /* SYNC:
             * ? Responder throws error here
             *   - Responder throws error which causes Listener to reset */
            throwNetworkingException(key, "Error while reading request", e);
          }
          if (!(request instanceof MrSoaRequest))
            throwNetworkingException(key, "Client sent invalid request", null);
          
          return (MrSoaRequest) request;
        
        case -1:
        case TcpHelperNio.CLIENT_CLOSE:
          // Close just in case the client is broken and did not close
          TcpHelperNio.closeConnection(key, listenerSendBuffer, this);
          MrSoaNetworkingException e1 = new MrSoaNetworkingException("Client closed connection");
          log.debug(e1.getMessage());
          throw e1;
          
        case TcpHelperNio.CLIENT_FATAL:
          String reason = "no error given";
          try {
            reason = (String) TcpHelperNio.readPayload(key, listenerReadBuffer, MrSoaRequest.class.getClassLoader(), this, id);
          } catch (Exception e) { /*ignore*/ }
          // Close just in case the client is broken and did not close
          TcpHelperNio.closeConnection(key, listenerSendBuffer, this);
          MrSoaNetworkingException e2 = new MrSoaNetworkingException("Client closed connection with error: " + reason);
          log.warn(e2.getMessage());
          throw e2;
          
        case TcpHelperNio.SYN:
          TcpHelperNio.closeConnection(key, listenerSendBuffer, this);
          MrSoaNetworkingException e3 = new MrSoaNetworkingException("Client sent unexpected SYN");
          log.warn(e3.getMessage());
          throw e3;
          
        default:
          throwNetworkingException(key, "Client sent invalid command: " + command, null);
      }
    } catch (Throwable e) {
      if (e instanceof MrSoaNetworkingException)
        throw (MrSoaNetworkingException) e;
      throw new MrSoaInternalResponderException(e);
    }
    
    // processing should never reach here
    throw new IllegalStateException("Internal error: bad branch");
  }
  
  /**
   * Disconnect client (with a FATAL command), log and throw a new
   * {@link MrSoaNetworkingException}.
   * 
   * @param key
   *          The socket to disconnect
   * @param message
   *          The message to provide to the client and in the exception
   * @param e
   *          The original network exception that precipitated all this
   */
  private void throwNetworkingException(SelectionKey key, String message, IOException e) {
    TcpHelperNio.fatalizeConnection(key, listenerSendBuffer, message, this);
    MrSoaNetworkingException e2 = new MrSoaNetworkingException(message, e);
    log.error(e2.getMessage(), e2);
    throw e2;
  }
  
  /**
   * Forward the provided MrSOA request to the appropriate service application.
   * The appropriate service application is determined using the
   * {@link LocalServiceRegistry}
   * 
   * @param key
   *          The socket from whence came the provided request
   * @param request
   *          The MrSOA request
   * @return A response object to send back to the client
   * @throws MrSoaInternalResponderException
   */
  private MrSoaResponse forwardRequest(SelectionKey key, MrSoaRequest request)
  throws MrSoaInternalResponderException {
    Throwable responseException = null;
    
    IMrSoaService service = null;
    try {
      service = LocalServiceRegistry.getService(request);
    } catch (Throwable e) {
      if (e instanceof MrSoaInternalResponderException)
        throw (MrSoaInternalResponderException) e;
      responseException = e;
    }

    Object[] args = null;
    if (responseException == null) {
      if (request.argsCount == 0)
        args = new Object[0];
      else {
        try {
          args = (Object[]) TcpHelperNio.readPayload(key, listenerReadBuffer, service.getClass().getClassLoader(), this, id);
        } catch (IOException e) {
          MrSoaNetworkingException e2 = new MrSoaNetworkingException("Error while reading method invocation arguments");
          log.error(e2.getMessage(), e);
          try {
            key.channel().close();
          } catch (IOException e1) { }
          key.cancel();
          throw e2;
        } catch (ClassNotFoundException e) {
          responseException = e;
        } catch (Throwable e) {
          throw new MrSoaInternalResponderException(e);
        }
      }
    }
    
    Object resultObject = null;
    if (responseException == null) {
      /* SYNC:
       * - See MrSoaReceiver.run()
       * ? Listener.netEvent() is called just before
       *   - All calls to netEvent() will block because the Listener's object 
       *     monitor is currently locked */
      key.attach(acker);
      /* SYNC: See MrSoaCommandListener.listenTo() */
      acker.listenTo(key);
      
      try {
        resultObject = service.invokeMethod(request, args);
      } catch (Throwable e) {
        responseException = e;
      }

      /* SYNC:
       * - See MrSoaCommandListener.reset()
       * - It is critical that we reset the Acker before we detach it from 
       *   the key. This is so that no net events destined for the Responder 
       *   end up in the Acker. */
      acker.reset();
      /* SYNC: See MrSoaReceiver.run() */
      key.attach(this);
    }
    
    MrSoaResponse response = new MrSoaResponse();
    
    if (responseException != null) {
      packageResponseException(response, responseException);
    } else {
      response.result = resultObject;
    }
    
    return response;
  }

  /**
   * Packages the provided exception into the provided response object.
   * 
   * @param response
   *          The response object, ignored if null
   * @param responseException
   *          The exception
   * @return The flattened exception that was packaged into the provided
   *         response
   */
  public static ArrayList<Object> packageResponseException(MrSoaResponse response, Throwable responseException) {
    ArrayList<Object> flatException = new ArrayList<Object>();
    Throwable e = responseException;
    while (e != null) {
      flatException.add(e.getClass().getName());
      flatException.add(e.getMessage());
      flatException.add(e.getStackTrace());
      e = e.getCause();
    }
    
    if (response != null) {
      response.isException = true;
      response.flatException = flatException;
      response.exception = responseException;
    }
    
    return flatException;
  }

  /**
   * Send the provided MrSOA response back to the client.
   * 
   * @param key
   *          The socket that is connected to the client
   * @param response
   *          The response to send to the client
   * @return True if successful, False otherwise
   */
  private boolean sendResponse(SelectionKey key, MrSoaResponse response) {
    try {
      /* SYNC:
       * ? Responder throws error from here
       *   - Method returns false which causes Listener to reset */
      TcpHelperNio.sendByte(key, listenerSendBuffer, TcpHelperNio.RESPONSE, this, BUFFER_AND_SEND, id);
      TcpHelperNio.sendPayload(key, listenerSendBuffer, response, this, id);
      
      if (response.isNull) {
        // Done returning
      } else if (response.isException) {
        TcpHelperNio.sendPayload(key, listenerSendBuffer, response.flatException, this, id);
        TcpHelperNio.sendPayload(key, listenerSendBuffer, response.exception, this, id);
      } else {
        TcpHelperNio.sendPayload(key, listenerSendBuffer, response.result, this, id);
      }
    } catch (Throwable e) {
      log.error("Error sending response", e);
      try {
        key.channel().close();
      } catch (IOException e1) { }
      return false;
    }
    return true;
  }
}
