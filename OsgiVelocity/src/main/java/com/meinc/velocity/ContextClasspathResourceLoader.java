package com.meinc.velocity;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class ContextClasspathResourceLoader extends ClasspathResourceLoader {
  private static final Log log = LogFactory.getLog(ContextClasspathResourceLoader.class);
  
  @Override
  public synchronized InputStream getResourceStream(String name)
      throws ResourceNotFoundException {
    InputStream result = null;
    
    if (name == null || name.length() == 0) {
      throw new ResourceNotFoundException("No template name provided");
    }

    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      result = classLoader.getResourceAsStream(name);
    } catch (Exception fnfe) {
      /*
       *  log and convert to a general Velocity ResourceNotFoundException
       */

      throw new ResourceNotFoundException(fnfe.getMessage());
    }

    return result;
  }
}
