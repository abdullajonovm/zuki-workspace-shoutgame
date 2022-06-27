package com.meinc.mrsoa.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * ObjectInputStream that creates objects using a provided ClassLoader
 * 
 * @author Matt
 */
public class MrSoaObjectInputStream extends ObjectInputStream {
  
  private ClassLoader classloader;
  
  /**
   * @param classloader
   *          The classloader to use when deserializing
   * @param in
   *          The input stream to deserialize
   * @throws IOException
   *           If an error occurs while reading bytes from stream
   */
  public MrSoaObjectInputStream(ClassLoader classloader, InputStream in) throws IOException {
    super(in);
    this.classloader = classloader;
  }
  
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    return Class.forName(desc.getName(), true, classloader);
  }
}