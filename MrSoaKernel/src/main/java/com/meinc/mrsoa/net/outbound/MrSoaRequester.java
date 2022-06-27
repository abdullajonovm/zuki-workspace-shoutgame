package com.meinc.mrsoa.net.outbound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.mrsoa.monitor.MrSoaServerMonitor;
import com.meinc.mrsoa.net.LocalServerSettings;
import com.meinc.mrsoa.net.MrSoaNetworkingException;
import com.meinc.mrsoa.net.MrSoaObjectInputStream;
import com.meinc.mrsoa.net.TcpHelper;
import com.meinc.mrsoa.net.inbound.IMrSoaService;
import com.meinc.mrsoa.net.inbound.LocalServiceRegistry;
import com.meinc.mrsoa.net.inbound.MrSoaInternalResponderException;
import com.meinc.mrsoa.net.inbound.MrSoaRequest;
import com.meinc.mrsoa.net.inbound.MrSoaResponder;
import com.meinc.mrsoa.net.inbound.MrSoaResponse;
import com.meinc.mrsoa.net.inbound.MrSoaServiceNotFoundException;
import com.meinc.mrsoa.service.ServiceCallStack;
import com.meinc.mrsoa.service.ServiceCallStackRow;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.exception.AdaptorWrappedException;
import com.meinc.mrsoa.service.exception.AllServersBusy;
import com.meinc.mrsoa.service.exception.CannotDeserializeExceptionWrapperException;
import com.meinc.mrsoa.service.exception.ServiceDiedAfterReceivingCallException;

public class MrSoaRequester {
  private static final Log log = LogFactory.getLog(MrSoaRequest.class);
  
  private static volatile Map<String,RemoteAddressPair> lastServerForService = new HashMap<String,RemoteAddressPair>();
  
  private static final int messageTimeoutMillis = 120000;
  private static int messageSynIntervalMillis = 10000;
  private static int serverScoreTimeoutMillis = 5000;
  
  private static boolean localhostFallback;
  private static MrSoaRequester singleton;

  static {
    String fallbackAddressString = System.getProperty("mrsoa.client.localhost.fallback");
    if (fallbackAddressString != null && "true".equals(fallbackAddressString.trim().toLowerCase()))
      localhostFallback = true;
    
    // Static initializers are thread-safe
    singleton = new MrSoaRequester(MrSoaServerMonitor.getInstance());        
  }
  
  public static MrSoaRequester getInstance() {
    return singleton;
  }

  protected static void setServerScoreTimeoutMillis(int serverScoreTimeoutMillis) {
    MrSoaRequester.serverScoreTimeoutMillis = serverScoreTimeoutMillis;
  }

  private MrSoaServerMonitor monitor;
  
  MrSoaRequester(MrSoaServerMonitor monitor) {
    this.monitor = monitor;
  }
  
  protected RemoteAddressPair getBestServerForEndpoint(String destination) {
    // Find the cached best server for this request
    RemoteAddressPair remoteAddressPair = lastServerForService.get(destination);
    if (remoteAddressPair == null) {
      /* SYNC:
       * - Deadlock
       *   ? Block delays to release monitor -> Block never delays
       *   ? connectionFailedForEndpoint() delays to release monitor -> Method 
       *     always releases monitor */
      synchronized (MrSoaRequester.class) {
        // We check whether the pair is still null because another thread may have beat us here
        remoteAddressPair = lastServerForService.get(destination);
        if (remoteAddressPair == null) {
          Map<String,RemoteAddressPair> newLastServerForService = new HashMap<String,RemoteAddressPair>(lastServerForService);
          remoteAddressPair = new RemoteAddressPair();
          newLastServerForService.put(destination, remoteAddressPair);
          lastServerForService = newLastServerForService;
        }
      }
    }
    
    // If the cached best server is stale, refresh it
    long currentTime = System.currentTimeMillis();
    if (currentTime - remoteAddressPair.timestamp > serverScoreTimeoutMillis) {
      InetSocketAddress remoteAddress = monitor.getNetAddressToEndpoint(destination);
      boolean isFallback = false;
      if (remoteAddress == null) {
        if (localhostFallback) {
          remoteAddress = LocalServerSettings.getLocalServerSocketAddress();
          isFallback = true;
        }
      }
      /* SYNC:
       * - Synced so that pair updates are always atomic and we don't end up 
       *   with "combo" pairs */
      synchronized (remoteAddressPair) {
        remoteAddressPair.isFallback = isFallback;
        remoteAddressPair.timestamp = currentTime;
        remoteAddressPair.address = remoteAddress;
      }
    }
    
    return remoteAddressPair;
  }

  protected void connectionFailedForEndpoint(String destination, InetSocketAddress remoteAddress) {
    //monitor.handleConnectionFailedToEndpoint(destination, remoteAddress);
    /* SYNC: See getBestServerForEndpoint(){1} */
    synchronized (MrSoaRequester.class) {
      RemoteAddressPair remoteAddressPair = lastServerForService.get(destination);
      if (remoteAddressPair != null) {
        if (remoteAddressPair.address == remoteAddress)
          lastServerForService.remove(destination);
      }
    }    
  }

  public Object invokeMethod(ServiceEndpoint endpoint, String methodName, Boolean internalMethod, boolean fastCopy, Object... args) {
    String destination = endpoint.toString();
    Socket socket = null;
    
    List<ServiceCallStackRow> callStack = ServiceCallStack.getCallStack();
    boolean innerCallstack = false;
    if (callStack == null) {
      // This request may have been born inside a service on-load method
      callStack = ServiceCallStackBackDoor.generateInnerServiceCallStack();
      if (callStack != null && !callStack.isEmpty())
        innerCallstack = true;
    }
    try {
      MrSoaRequest request = new MrSoaRequest();
      
      request.destination = endpoint.toString();
      if (callStack != null && !callStack.isEmpty())
        request.from = callStack.get(callStack.size()-1).toString();
      request.callStack = callStack;
      request.isInternalMethodCall = internalMethod;
      request.methodName = methodName;
      request.argsCount = args.length;
  
      int tryCount = 0;
      // start of get-socket while loop
      while (true) {
        RemoteAddressPair remoteAddressPair = getBestServerForEndpoint(destination);
        
        InetSocketAddress remoteAddress = remoteAddressPair.address;
        if (remoteAddress == null) 
          throw new MrSoaServiceNotFoundException(destination);
        
        boolean isLocalhostFallback = remoteAddressPair.isFallback;
        if (!isLocalhostFallback && monitor.isLocalServerAddress(remoteAddress)) {
          try {
            // Bypass using sockets altogether and attempt to invoke the service
            // directly.
            return invokeLocalMethod(request, fastCopy, args);
          } catch (MrSoaServiceNotFoundException e) {
            // Ignore and try invoke via network below.
            // This should be a rare occurrence - especially in production
            // environments as MrSOA servers probably will not run side-by-side on
            // the same machine.
          }
        }
        
        try {
          socket = MrSoaConnectionPool.borrowConnection(remoteAddress);
        } catch (IOException e) {
          connectionFailedForEndpoint(destination, remoteAddress);
          
          if (isLocalhostFallback) {
            log.error("Could not connect to " + remoteAddress, e);
            throw new MrSoaServiceNotFoundException(destination);
          }
          
          // If we are at the beginning of the first service method invocation (in
          // Tomcat or similar), try once more and then give up.
          else if (callStack == null || callStack.isEmpty()) {
            if (++tryCount <= 1) {
              log.warn("Could not connect to " + remoteAddress + ". Trying one more time.");
              try {
                Thread.sleep(1500);
              } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e1);
              }
              continue;
            } else {
              log.warn("Could not connect to " + remoteAddress + ". Giving up.");
              throw new AllServersBusy();
            }
            
          // If we are in the middle of a sequence of service method invocations,
          // never give up and keep trying.
          } else {
            log.warn("Could not connect to " + remoteAddress + ". Retrying.");
            try {
              Thread.sleep(1500);
            } catch (InterruptedException e1) {
              Thread.currentThread().interrupt();
              throw new RuntimeException(e1);
            }
            continue;
          }
        }
        
        break;
      } // end of get-socket while loop
      
      try {
        // Send the request
        try {
  //        if (log.isTraceEnabled())
  //          log.trace("Sending command: " + TcpHelper.REQUEST);
          TcpHelper.sendCommand(socket, TcpHelper.REQUEST);
          TcpHelper.sendPayload(socket, request);
          if (args.length > 0)
            TcpHelper.sendPayload(socket, args);
        } catch (IOException e) {
          throwNetworkingException(socket, "Could not send request to remote server", e);
        }
        
        long messageSentMillis = System.currentTimeMillis();
        long lastAckMillis = 0;
        long lastSynMillis = 0;
        MrSoaResponse response = null;
        
        // Wait for a response
        while (response == null) {
          //
          // The remote server has been silent for too long, kill the connection
          if ((lastAckMillis == 0 && System.currentTimeMillis() - messageSentMillis > messageTimeoutMillis) ||
              (lastAckMillis != 0 && System.currentTimeMillis() - lastAckMillis > messageTimeoutMillis)) {
            TcpHelper.fatalizeConnection(socket, "Request timed out");
            throw new ServiceDiedAfterReceivingCallException(
                "Remote service "
                + endpoint
                + " appears to have stopped processing the request");
          }
          
          int command = -1;
          try {
            command = TcpHelper.readCommand(socket, messageSynIntervalMillis);
          } catch (SocketTimeoutException e) {
            try {
              // If we've received an ACK since the last SYN
              if (lastSynMillis <= lastAckMillis)
                TcpHelper.sendCommand(socket, TcpHelper.SYN);
              continue;
            } catch (IOException e1) {
              throwNetworkingException(socket, "Error while sending SYN to remote service " + endpoint, e1);
            }
          } catch (IOException e) {
            throwNetworkingException(socket, "Error while reading response from service " + endpoint, e);
          }
          
          switch (command) {
            case TcpHelper.ACK:
              lastAckMillis = System.currentTimeMillis();
              continue;
    
            case TcpHelper.RESPONSE:
              try {
                response = (MrSoaResponse) TcpHelper.readPayload(socket, getClass().getClassLoader());
              } catch (IOException e2) {
                throwNetworkingException(socket, "Error while reading response from service " + endpoint, e2);
              } catch (ClassNotFoundException e2) {
                MrSoaNetworkingException e = new MrSoaNetworkingException("Could not deserialize class into local classloader from service " + endpoint, e2);
                // we kill the connection because something is seriously wrong if
                // we cannot deserialize an instance of MrSoaResponse
                TcpHelper.fatalizeConnection(socket, e.getMessage());
                log.error(e.getMessage(), e);
                throw e;
              }
              break;
              
            case TcpHelper.SERVER_CLOSE:
              try {
                socket.close();
              } catch (IOException e1) { }
              
              throw new ServiceDiedAfterReceivingCallException("Remote service " + endpoint + " closed connection");
              
            case TcpHelper.SERVER_FATAL:
              String reason = null;
              try {
                reason = (String) TcpHelper.readPayload(socket, MrSoaRequest.class.getClassLoader());
              } catch (Exception e) { }
              try {
                socket.close();
              } catch (IOException e) { }
              
              throw new ServiceDiedAfterReceivingCallException(
                  "Remote service "
                  + endpoint
                  + " closed connection with error: " + (reason == null ? "no reason provided" : reason));
              
            default:
              TcpHelper.fatalizeConnection(socket, "Illegal response");
              throw new MrSoaNetworkingException("Remote server returned illegal response");
          }
        } // end of wait for response loop
  
        if (response.isNull)
          return null;
  
        if (response.isException) {
          ArrayList<Object> flatException = null;
          try {
            flatException = (ArrayList<Object>) TcpHelper.readPayload(socket, getClass().getClassLoader());
          } catch (IOException e) {
            throwNetworkingException(socket, "Error while reading returned exception from service" + endpoint, e);
          } catch (ClassNotFoundException e) {
            MrSoaNetworkingException e2 = new MrSoaNetworkingException("Could not deserialize class into local classloader from service " + endpoint, e);
            // we kill the connection because something is seriously wrong if
            // we cannot deserialize an array of primitives
            TcpHelper.fatalizeConnection(socket, e2.getMessage());
            log.error(e2.getMessage(), e2);
            throw e2;
          }
          
          Throwable exception = null;
          try {
            exception = (Throwable) TcpHelper.readPayload(socket, ServiceCallStack.getCurrentServiceClassLoader());
          } catch (IOException e) {
            throwNetworkingException(socket, "Error while reading returned exception from service" + endpoint, e);
          } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled())
              log.debug("Could not deserialize returned exception", e);
            throw inflateException(flatException);
          }
  
          EvilThrower.throwException(stripWrappedExceptions(exception));
          throw new IllegalStateException("Internal error: This code should never execute");
        } // end of if response is exception block
  
        // If we're here, the response is not an exception
        Object returnObj = null;
        try {
          returnObj = TcpHelper.readPayload(socket, ServiceCallStack.getCurrentServiceClassLoader());
        } catch (IOException e) {
          throwNetworkingException(socket, "Error while reading returned object from service " + endpoint, e);
        } catch (ClassNotFoundException e) {
          MrSoaNetworkingException e2 = new MrSoaNetworkingException("Could not deserialize class into local classloader from service " + endpoint, e);
          log.error(e2.getMessage(), e2);
          throw e2;
        }
  
        return returnObj;
        
      } finally {
        if (socket != null)
          MrSoaConnectionPool.returnConnection(socket);
      }
      
    } finally {
      if (innerCallstack) {
        callStack.clear();
        innerCallstack = false;
      }
    }
  }

  private RuntimeException inflateException(ArrayList<Object> flatException) {
    // The remote exception does not exist in this classloader, so create
    // a substitute "combo" exception to represent it
    RuntimeException topException = null;
    try {
      if (flatException.size() % 3 != 0)
        throw new MrSoaNetworkingException("Error while deserializing returned exception from service");

      RuntimeException lastException = null;
      for (int i = 0; i < flatException.size(); i += 3) {
        String exceptionClassname = (String) flatException.get(i);
        String exceptionMessage = (String) flatException.get(i+1);
        StackTraceElement[] stackTrace = (StackTraceElement[]) flatException.get(i+2);
        // Create new exception to represent the remote exception
        exceptionMessage = "[Original Exception Class: " + exceptionClassname + "] " + exceptionMessage;
        CannotDeserializeExceptionWrapperException cannotDeserializeWrapperException = new CannotDeserializeExceptionWrapperException(exceptionMessage);
        cannotDeserializeWrapperException.setExceptionClassname(exceptionClassname);
        cannotDeserializeWrapperException.setStackTrace(stackTrace);
        if (lastException == null) {
          lastException = cannotDeserializeWrapperException;
          topException = cannotDeserializeWrapperException;
        } else {
          lastException.initCause(cannotDeserializeWrapperException);
          lastException = cannotDeserializeWrapperException;
        }
      }
    } catch (Exception e1) {
      throw new MrSoaNetworkingException("Error while deserializing returned exception from service", e1);
    }
    
    return topException;
  }
  
  private Object invokeLocalMethod(MrSoaRequest request, boolean fastCopy, Object[] args)
  throws MrSoaServiceNotFoundException {
    IMrSoaService service;
    try {
      service = LocalServiceRegistry.getService(request);
    } catch (MrSoaInternalResponderException e) {
      throw new RuntimeException("Error while looking up local service", e);
    }
    
    Object[] copiedArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];

      if (fastCopy) {
        copiedArgs[i] = arg;
      } else {
        try {
          // TODO: Some arguments may require very large byte buffers - should
          // we use a multi-threaded direct-output-to-input-stream instead?
          // (it's a space-vs-time tradeoff) Note: If used effectively, the NoSer
          // wrappers could alleviate this problem and give us the best of both
          // worlds (time and space).
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
          objOut.writeObject(arg);
          objOut.close();
          ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
          ObjectInputStream objIn = new MrSoaObjectInputStream(service.getClass().getClassLoader(), byteIn);
          copiedArgs[i] = objIn.readObject();
          objIn.close();
        } catch (IOException e) {
          throw new RuntimeException("Error while copying method arguments", e);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Error while copying method arguments", e);
        }
      }
    }

    ClassLoader currentClassLoader = ServiceCallStack.getCurrentServiceClassLoader();
    
    Object result;
    try {
      result = service.invokeMethod(request, copiedArgs);
    } catch (Throwable e1) {
      Throwable copiedEx = null;
      try {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(e1);
        objOut.close();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objIn = new MrSoaObjectInputStream(currentClassLoader, byteIn);
        copiedEx = (Throwable) objIn.readObject();
        objIn.close();
        
      } catch (IOException e2) {
        throw new RuntimeException("Error while copying method thrown exception", e2);
        
      } catch (ClassNotFoundException e2) {
        ArrayList<Object> flatException = MrSoaResponder.packageResponseException(null, e1);
        throw inflateException(flatException);
      }
      
      EvilThrower.throwException(stripWrappedExceptions(copiedEx));
      throw new IllegalStateException("Internal error: This code should never execute");
    }
    
    if (fastCopy) {
      return result;
    } else {
      try {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(result);
        objOut.close();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objIn = new MrSoaObjectInputStream(currentClassLoader, byteIn);
        Object copiedResult = objIn.readObject();
        objIn.close();
        return copiedResult;
      } catch (IOException e) {
        throw new RuntimeException("Error while copying method return value", e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Error while copying method return value", e);
      }
    }
  }

  private Throwable stripWrappedExceptions(Throwable exception) {
    Throwable f = exception;          
    // Strip away wrapper exceptions to get at the real Exception
    while (f instanceof AdaptorWrappedException) {
      Throwable g = f.getCause();
      if (g == null || !(g instanceof AdaptorWrappedException))
        break;
      f = g;
    }
    return f;
  }
  
  /**
   * Disconnect client (with a FATAL command), log and throw a new
   * {@link MrSoaNetworkingException}.
   * 
   * @param socket
   *          The socket to disconnect
   * @param message
   *          The message to provide to the client and in the exception
   * @param e
   *          The original network exception that precipitated all this
   */
  private void throwNetworkingException(Socket socket, String message, IOException e) {
    TcpHelper.fatalizeConnection(socket, message);
    MrSoaNetworkingException e2 = new MrSoaNetworkingException(message, e);
    log.error(e2.getMessage(), e2);
    throw e2;
  }

  /**
   * Sets the time after a request is sent before a SYN command is sent.
   * 
   * @param messageSynIntervalMillis
   *          The time to wait before sending a SYN command
   */
  protected static void setMessageSynIntervalMillis(int messageSynIntervalMillis) {
    MrSoaRequester.messageSynIntervalMillis = messageSynIntervalMillis;
  }

  /**
   * Returns whether MrSOA is configured to "fallback" as a last resort to
   * connecting to a MrSOA server on localhost
   * 
   * @return True if localhost fallback is enabled
   */
  public static boolean isLocalhostFallback() {
    return localhostFallback;
  }
  
  private static class ServiceCallStackBackDoor extends ServiceCallStack {
    protected static List<ServiceCallStackRow> generateInnerServiceCallStack() {
      return ServiceCallStack.generateInnerServiceCallStack();
    }
  }
  
  protected static class RemoteAddressPair {
    public boolean isFallback;
    public volatile long timestamp;
    public volatile InetSocketAddress address;
  }
}

class EvilThrower {
  private static final Log log = LogFactory.getLog(EvilThrower.class);
  
  private static ThreadLocal<Throwable> exception = new ThreadLocal<Throwable>();
  
  private EvilThrower() throws Throwable {
    throw exception.get();
  }
  
  public static void throwException(Throwable exception) {
    EvilThrower.exception.set(exception);
    try {
      EvilThrower.class.newInstance();
    } catch (InstantiationException e) {
      log.error(e);
    } catch (IllegalAccessException e) {
      log.error(e);
    } finally {
      EvilThrower.exception.set(null);
    }
  }
}
