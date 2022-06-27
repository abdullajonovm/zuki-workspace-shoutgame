package com.meinc.mrsoa.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class that contains methods for sending or receiving TCP messages to
 * or from a MrSOA server.
 * 
 * @author Matt
 */
public class TcpHelper {
  private static final Log log = LogFactory.getLog(TcpHelper.class);
  
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
  
  /**
   * Send a command to a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param command
   *          The command to send
   * @throws IOException
   *           If a network exception occurs
   */
  public static void sendCommand(Socket socket, int command) throws IOException {
    OutputStream netOut = socket.getOutputStream();
    netOut.write(command);
  }
  
  /**
   * Serialize and send an object to a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param payload
   *          The object to send
   * @throws IOException
   *           If a network exception occurs
   */
  public static void sendPayload(Socket socket, Object payload) throws IOException {
    /*sendLength(socket, 0);*/
    
    OutputStream netOut = socket.getOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(netOut);
    objOut.writeObject(payload);
    objOut.flush();
  }
  
  /**
   * Send a length value to a remote MrSOA server.
   * <p>
   * <em>Note that this method is presently mostly useless as Java serialization
   * does not require a length value. It is kept here for future use when
   * non-serialized payloads are used.</em>
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param length
   *          The length value
   * @throws IOException
   *           If a network exception occurs
   */
  /*private static void sendLength(Socket socket, int length) throws IOException {
    OutputStream netOut = socket.getOutputStream();
    byte[] lengthBytes = new byte[4];
    lengthBytes[0] = (byte) (length & 0xFF);
    lengthBytes[1] = (byte) ((length >> 8) & 0xFF);
    lengthBytes[2] = (byte) ((length >> 16) & 0xFF);
    lengthBytes[3] = (byte) ((length >> 24) & 0xFF);
    netOut.write(lengthBytes);
  }*/
  
  /**
   * Read a length value from a remote MrSOA server.
   * <p>
   * <em>Note that this method is presently mostly useless as Java serialization
   * does not require a length value. It is kept here for future use when
   * non-serialized payloads are used.</em>
   * 
   * @param socket
   *          The socket connected to a remote server
   * @return The length value read from the network
   * @throws IOException
   *           If a network exception occurs
   */
  /*private static int readLength(Socket socket) throws IOException {
    InputStream netIn = socket.getInputStream();
    byte[] lengthBytes = new byte[4];
    int totalBytesRead = 0;
    
    while (totalBytesRead < 4) {
      int bytesRead = netIn.read(lengthBytes, totalBytesRead, lengthBytes.length - totalBytesRead);
      if (bytesRead == -1)
        throw new MrSoaNetworkingException("Network request terminated unexpectedly");
      totalBytesRead += bytesRead;
    }
    
    int length = lengthBytes[0]
              | (lengthBytes[1] << 8)
              | (lengthBytes[2] << 16)
              | (lengthBytes[2] << 24);
    
    return length;
  }*/
  
  /**
   * Read a command from a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param timeout
   *          The amount of time to wait for the remote command before giving up
   *          and throwing a {@link SocketTimeoutException}.
   * @return The command read from the network
   * @throws SocketTimeoutException
   *           If no command arrived before the specified timeout
   * @throws IOException
   *           If a network exception occurs
   */
  public static int readCommand(Socket socket, int timeout) throws IOException {
    try {
      socket.setSoTimeout(timeout);
    } catch (SocketException e) {
      log.error("Could not set socket timeout", e);
    }
    
    InputStream netIn = socket.getInputStream();
    int command = netIn.read();
    return command;
  }

  /**
   * Read and deserialize an object from a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param classloader
   *          The classloader to use to deserialize
   * @return The object read from the network
   * @throws IOException
   *           If a network exception occurs
   * @throws ClassNotFoundException
   *           If the classloader could not find the object's class
   */
  public static Object readPayload(Socket socket, ClassLoader classloader) throws IOException, ClassNotFoundException {
    InputStream netIn = socket.getInputStream();
    
    /*readLength(socket);*/
    
    ObjectInputStream objIn = 
      new MrSoaObjectInputStream(classloader, netIn);
    
    return objIn.readObject();
  }
  
  /**
   * Send a CLOSE command to a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   */
  public static void closeConnection(Socket socket) {
    try {
      sendCommand(socket, SERVER_CLOSE);
    } catch (IOException e) {
      //log.warn("Error while closing connection", e);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        //log.warn("Error while closing socket", e);
      }
    }
  }

  /**
   * Send a FATAL command to a remote MrSOA server.
   * 
   * @param socket
   *          The socket connected to a remote server
   * @param errorMessage
   *          The error message to send to the remote server
   */
  public static void fatalizeConnection(Socket socket, String errorMessage) {
    try {
      sendCommand(socket, SERVER_FATAL);
      sendPayload(socket, errorMessage);
    } catch (IOException e) {
      //log.warn("Error while closing connection", e);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        //log.warn("Error while closing socket", e);
      }
    }
  }
}
