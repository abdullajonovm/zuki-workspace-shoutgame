package com.meinc.launcher.serverprops;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Launches a thread that reads-in a specified Java properties file and exposes
 * its content via a {@link java.util.Properties} object.  Changes to the
 * properties file are detected and the exposed Properties object is
 * automatically updated. 
 * <h4>Java System Properties</h3>
 * <table>
 * <tr><td>meinc.server.properties.file</td><td>The Java properties file to watch (required)</td></tr>
 * <tr><td>meinc.server.properties.millis</td><td>How frequently to check for changes in the specified Java properties file (default is 3000)</td></tr>
 * </table>
 *
 * @author Matt
 */
public class ServerPropertyHolder extends Thread {
  private static Log _log = LogFactory.getLog(ServerPropertyHolder.class);
  private static Properties _props;
  private static String _propsSecretKey;
  
  private static ServerPropertyHolder holder;
  private static ExecutorService executor = Executors.newSingleThreadExecutor();
  
  /**
   * Reads in the necessary Java System Properties and launches the Server
   * Property Monitor thread.
   */
  public static synchronized void startServerPropertyMonitor() {
    if (holder != null) return;
    
    String serverPropsFileString = System.getProperty("meinc.server.properties.file");
    String serverPropsMillisString = System.getProperty("meinc.server.properties.millis");
    
    File deployerPath = null;
    if (serverPropsFileString != null && !serverPropsFileString.isEmpty()) {
      deployerPath = new File(serverPropsFileString).getAbsoluteFile();
      if (!deployerPath.exists() || !deployerPath.canRead()) {
        String error = "Cannot open '" + serverPropsFileString + "' properties file for reading";
        System.err.println(error);
        throw new IllegalStateException(error);
      }
    }
    
    int deployerMillis = 3000; // the default
    if (serverPropsMillisString != null && serverPropsMillisString.trim().length() != 0)
      deployerMillis = Integer.parseInt(serverPropsMillisString);

    holder = new ServerPropertyHolder(deployerPath, deployerMillis);
    holder.start();
  }
  
  private File serverPropsFile;
  private int serverPropsMillis;
  
  private ServerPropertyHolder(File serverPropsFile, int serverPropsMillis) {
    this.serverPropsFile = serverPropsFile;
    this.serverPropsMillis = serverPropsMillis;
    
    setDaemon(true);
    setName("Meinc Server Property Monitor");
  }
  
  @Override
  public void run() {
    _log.info("Starting Server Properties Monitor");

    long lastServerPropsFileDate = 0;
    while (!isInterrupted()) {
      if (serverPropsFile == null || serverPropsFile.lastModified() > lastServerPropsFileDate) {
        // Synced so that getProps() will block until we have finished updating
        synchronized (ServerPropertyHolder.class) {
          Map<Object,Object> oldProps = (_props == null) ? null : new HashMap<Object,Object>(_props);
          ServerPropertyHolder.class.notifyAll();
          
          if (serverPropsFile == null) {
            _props = System.getProperties();
          } else {
            _log.info("Updating Server Properties");
            try {
              lastServerPropsFileDate = serverPropsFile.lastModified();
              _props = loadPropertiesFile(serverPropsFile);
            } catch (IOException e) {
              _log.error("Error while reading Server Properties file " + serverPropsFile.getAbsolutePath(), e);
            } catch (KeyException e) {
              _log.error("Error while reading Server Properties file " + serverPropsFile.getAbsolutePath(), e);
            }
            String propsDecFilePath = System.getProperty("serverprops.dec.file");
            if (propsDecFilePath != null) {
              System.getProperties().remove("serverprops.dec.file");
              File propsDecFile = new File(propsDecFilePath);
              if (propsDecFile.exists() && propsDecFile.canRead())
                propsDecFile.delete();
            }
            if (_log.isDebugEnabled() && oldProps != null) {
              for (Entry<Object,Object> propEntry : _props.entrySet()) {
                String key = (String) propEntry.getKey();
                String oldValue = (String) oldProps.get(key);
                String newValue = (String) propEntry.getValue();
                if (!newValue.equals(oldValue))
                  _log.debug(String.format("Server property '%s' changed from %s to '%s'",
                                           key, (oldValue == null ? "null" : "'"+oldValue+"'"), newValue));
              }
            }
          }
          
          if (oldProps != null && _props != null) {
            // Check for changes
            if (!_changeListeners.isEmpty()) {
              for (final ListenerPair pair : _changeListeners) {
                final List<Change> changes = new ArrayList<Change>();
                for (Object oldKey : oldProps.keySet()) {
                  String propKey = oldKey.toString();
                  String oldVal = oldProps.get(propKey).toString();
                  String newVal = _props.getProperty(propKey);
                  if (pair.pattern.matcher(propKey).matches() &&
                      (!_props.containsKey(oldKey) || !oldVal.equals(newVal))) {
                    Change change = new Change();
                    change.key = propKey;
                    change.oldValue = oldProps.get(propKey).toString();
                    change.newValue = _props.getProperty(propKey);
                    changes.add(change);
                  }
                }
                for (Object newKey : _props.keySet()) {
                  String propKey = newKey.toString();
                  if (!oldProps.containsKey(newKey) && pair.pattern.matcher(propKey).matches()) {
                    Change change = new Change();
                    change.key = propKey;
                    change.oldValue = null;
                    change.newValue = _props.getProperty(propKey);
                    changes.add(change);
                  }
                }
                if (!changes.isEmpty()) {
                  executor.execute(new Runnable() {
                    public void run() {
                      try {
                        pair.listener.propertiesChanged(changes);
                      } catch (Exception e) {
                        _log.warn("Server Property Listener threw exception", e);
                      }
                    }
                  });
                }
              }
            }
          }
        }
      }
      
      try {
        Thread.sleep(serverPropsMillis);
      } catch (InterruptedException e) {
        _log.warn("System Properties Monitor is shutting down");
        break;
      }
    }
  }
  
  /**
   * Returns the System Properties object.
   * <p>
   * <b>Warning: Avoid using iterators on the returned Properties object as 
   * the content of this object may change at any time without warning!</b></p>
   * 
   * @return The System Properties object
   */
  public static synchronized Properties getProps() {
    if (holder == null)
      startServerPropertyMonitor();
    
    String callerDesc = null;
    
    if (_props == null) {
      callerDesc = getCallerDescriptor();
      do {
        _log.info("Waiting for Server Properties in " + callerDesc);
        try {
          ServerPropertyHolder.class.wait(3000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          _log.error("Error while retrieving Server Properties", e);
        }
      } while (_props == null);
    }
    
    if (callerDesc != null)
      _log.info("Received Server Properties in " + callerDesc);
    
    return _props;
  }
  
  public static String getProperty(String key) {
      return getProps().getProperty(key);
  }
  
  public static String getProperty(String key, String default_) {
    String val = getProps().getProperty(key);
    if (val == null)
      return default_;
    return val;
}

  private static String getCallerDescriptor() {
    StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];
      if (!element.getClassName().startsWith("com.meinc.launcher.")) {
        return element.toString();
      }
    }
    return "[unknown]";
  }

  public static interface ChangeListener extends EventListener {
    public void propertiesChanged(List<Change> properties);
  }
  
  public static class Change {
    public String key;
    public String oldValue;
    public String newValue;
  }
  
  private static class ListenerPair {
    public Pattern pattern;
    public ChangeListener listener;
  }

  private static List<ListenerPair> _changeListeners = new ArrayList<ListenerPair>();

  public static synchronized void addPropertyChangeListener(String propertyMatchRegex, ChangeListener listener) {
    ListenerPair listenerPair = new ListenerPair();
    listenerPair.pattern = Pattern.compile(propertyMatchRegex);
    listenerPair.listener = listener;
    _changeListeners.add(listenerPair);
  }
  
  public static synchronized void removePropertyChangeListener(String propertyMatchRegex) {
    List<ListenerPair> newChangeListeners = new ArrayList<ListenerPair>();
    for (ListenerPair pair : _changeListeners) {
      if (!pair.pattern.pattern().equals(propertyMatchRegex))
        newChangeListeners.add(pair);
    }
    _changeListeners = newChangeListeners;
  }
  
  public static synchronized void removePropertyChangeListener(ChangeListener listener) {
    List<ListenerPair> newChangeListeners = new ArrayList<ListenerPair>();
    for (ListenerPair pair : _changeListeners) {
      if (pair.listener != listener)
        newChangeListeners.add(pair);
    }
    _changeListeners = newChangeListeners;
  }
  
  private static byte[] concat(byte[] first, byte[] second) {
    byte[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  private static byte[] flipEndian(byte[] buf) {
    byte[] result = new byte[buf.length];
    for (int i = 0; i < buf.length; i+=4) {
      result[i+0] = buf[i+3];
      result[i+1] = buf[i+2];
      result[i+2] = buf[i+1];
      result[i+3] = buf[i+0];
    }
    return result;
  }

  private static String encodeHexString(byte[] bytes) {
    return String.format("%0"+(bytes.length*2)+"x", new BigInteger(1, bytes));
  }
  
  private static byte[] decodeHex(String string) {
    return DatatypeConverter.parseHexBinary(string);
  }
  
  public static String decryptVimBlowfish(byte[] fileContents, String password) throws UnsupportedEncodingException {
    String fileContentsPreamble = new String(fileContents, 0, 12);
    int vimCryptMethod;
    switch (fileContentsPreamble) {
      case "VimCrypt~02!":
        vimCryptMethod = 2;
        break;
      case "VimCrypt~03!":
        vimCryptMethod = 3;
        break;
      default:
        throw new UnsupportedEncodingException("not a vim blowfish file");
    }

    byte[] salt = Arrays.copyOfRange(fileContents, 12, 20);
    byte[] iv = Arrays.copyOfRange(fileContents, 20, 28);
    byte[] data = Arrays.copyOfRange(fileContents, 28, fileContents.length);

    ByteArrayOutputStream result = new ByteArrayOutputStream(data.length);

    /* Strengthen/stretch password by repeatedly hashing it */

    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      // This shouldn't ever happen
      throw new RuntimeException(e);
    }
    String hash;
    try {
      hash = encodeHexString(digest.digest(concat(password.getBytes("UTF-8"), salt)));
    } catch (UnsupportedEncodingException e) {
      // This shouldn't ever happen
      throw new RuntimeException(e);
    }
    for (int i = 0; i < 1000; i++)
      hash = encodeHexString(digest.digest(concat(hash.getBytes(), salt)));

    byte[] hashBytes;
    try {
      hashBytes = decodeHex(hash);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("password produced invalid hash: " + hash, e);
    }

    /* Prepare cipher for decryption. The Vim source code claims it uses an OFB block cipher, but in fact it uses a
     * modified CFB block cipher where the endian-ness of each 64-bit word is flipped. Because of the non-standard
     * flipping, a custom CFB block cipher is implemented below. */

    SecretKeySpec hashKey = new SecretKeySpec(hashBytes, "Blowfish");

    Cipher cipher;
    try {
      // Since we are implementing our own CFB mode, we want a basic IV-less encryptor
      cipher = Cipher.getInstance("Blowfish/ECB/NOPADDING");
    } catch (NoSuchAlgorithmException e) {
      throw new UnsupportedEncodingException("Blowfish/ECB is unsupported by this JVM");
    } catch (NoSuchPaddingException e) {
      throw new UnsupportedEncodingException("Blowfish/ECB/NOPADDING is unsupported by this JVM");
    }
    try {
      cipher.init(Cipher.ENCRYPT_MODE, hashKey);
    } catch (InvalidKeyException e) {
      throw new IllegalStateException("Password produced invalid blowfish key: " + hash, e);
    }

    /* Generate the first keystream by encrypting the initialization vector */

    int ivEncryptIterations;
    switch (vimCryptMethod) {
      case 2:
        ivEncryptIterations = 8;
        break;
      case 3:
        ivEncryptIterations = 1;
        break;
      default:
        throw new IllegalStateException();
    }
    int keystreamSize = iv.length * ivEncryptIterations;
    byte[] keystream = new byte[keystreamSize];
    for (int i = 0; i < keystreamSize; i+=8) {
      byte[] nextKeystreamChunk;
      try {
        nextKeystreamChunk = flipEndian(cipher.doFinal(flipEndian(iv)));
      } catch (IllegalBlockSizeException e) {
        throw new UnsupportedEncodingException("Could not decrypt vim blowfish file: " + e.getMessage());
      } catch (BadPaddingException e) {
        throw new UnsupportedEncodingException("Could not decrypt vim blowfish file: " + e.getMessage());
      }
      System.arraycopy(nextKeystreamChunk, 0, keystream, i, nextKeystreamChunk.length);
    }

    /* Decrypt loop */

    int dataIndex = 0;
    while (dataIndex < data.length) {
      int nextLen = Math.min(keystream.length, data.length - dataIndex);
      int oldDataIndex = dataIndex;
      for (int i = 0; i < nextLen; i++)
        result.write(data[dataIndex++] ^ keystream[i]);

      if (dataIndex - oldDataIndex == keystreamSize) {
        // Generate new keystream from the current block's ciphertext
        byte[] subData = new byte[8];
        for (int i = oldDataIndex; i < dataIndex; i+=8) {
          System.arraycopy(data, i, subData, 0, subData.length);
          byte[] nextKeystreamChunk;
          try {
            nextKeystreamChunk = flipEndian(cipher.doFinal(flipEndian(subData)));
          } catch (IllegalBlockSizeException e) {
            throw new UnsupportedEncodingException("Could not decrypt vim blowfish file: " + e.getMessage());
          } catch (BadPaddingException e) {
            throw new UnsupportedEncodingException("Could not decrypt vim blowfish file: " + e.getMessage());
          }
          System.arraycopy(nextKeystreamChunk, 0, keystream, i - oldDataIndex, nextKeystreamChunk.length);
        }
      }
    }

    return new String(result.toByteArray());
  }
  
  public synchronized static Properties loadPropertiesFile(File propsFile) throws IOException, KeyException {
    return loadPropertiesFile(propsFile, null, true);
  }
  
  public synchronized static Properties loadPropertiesFile(File propsFile, String propsSecretKey) throws IOException, KeyException {
    return loadPropertiesFile(propsFile, propsSecretKey, false);
  }
  
  private synchronized static Properties loadPropertiesFile(File propsFile, String propsSecretKey, boolean doKeyRead) throws IOException, KeyException {
    if (doKeyRead) {
      if (_propsSecretKey != null) {
        propsSecretKey = _propsSecretKey;
      } else {
        String propsDecFilePath = System.getProperty("serverprops.dec.file");
        if (propsDecFilePath != null) {
          File propsDecFile = new File(propsDecFilePath);
          if (propsDecFile.exists() && propsDecFile.canRead()) {
            FileInputStream propsDecFis = new FileInputStream(propsDecFile);
            try {
              ByteArrayOutputStream propsSecretKeyBaos = new ByteArrayOutputStream((int)propsDecFile.length());
              byte[] buf = new byte[1024];
              int bytesRead;
              while ((bytesRead = propsDecFis.read(buf)) > -1)
                propsSecretKeyBaos.write(buf, 0, bytesRead);
              _propsSecretKey = propsSecretKeyBaos.toString("UTF-8").trim();
              propsSecretKey = _propsSecretKey;
              propsSecretKeyBaos.close();
            } finally {
              propsDecFis.close();
            }
          }
        }
      }
    }

    FileInputStream propsFis = new FileInputStream(propsFile);
    //propsFis.mark(12);
    byte[] preamble = new byte[12];
    int bytesRead = propsFis.read(preamble);
    if (bytesRead == 12 && new String(preamble).startsWith("VimCrypt~")) {
      if (propsSecretKey == null) {
        propsFis.close();
        throw new KeyException("properties decrypt password not provided");
      }
      byte[] buf = new byte[4096];
      //propsFis.reset();
      propsFis.getChannel().position(0);
      ByteArrayOutputStream baos = new ByteArrayOutputStream((int)propsFile.length());
      while ((bytesRead = propsFis.read(buf)) > -1)
        baos.write(buf, 0, bytesRead);
      propsFis.close();
      String decryptedPropsString = decryptVimBlowfish(baos.toByteArray(), propsSecretKey);
      Properties props = new Properties();
      props.load(new StringReader(decryptedPropsString));
      baos.close();
      return props;
    } else {
      //propsFis.reset();
      propsFis.getChannel().position(0);
      Properties props = new Properties();
      props.load(propsFis);
      propsFis.close();
      return props;
    }
  }
}
