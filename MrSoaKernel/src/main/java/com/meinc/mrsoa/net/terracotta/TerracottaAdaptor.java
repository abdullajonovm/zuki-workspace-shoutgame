package com.meinc.mrsoa.net.terracotta;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TerracottaAdaptor {
  private static final Log log = LogFactory.getLog(TerracottaAdaptor.class);
  
  private static final String classLoaderName = "MrSoaServiceBundle";
  private static ClassLoader loader = TerracottaAdaptor.class.getClassLoader();
  private static URLClassLoader childLoader = new URLClassLoader(new URL[] {}, loader);

  public static void init(boolean createChildLoader) {
    ClassLoader targetLoader = createChildLoader ? childLoader : loader;
    
    synchronized (loader) {
      try {
        Class<?> namedLoader = targetLoader.loadClass("com.tc.object.loaders.NamedClassLoader");
        Method method = namedLoader.getMethod("__tc_setClassLoaderName", new Class[]{String.class});
        method.invoke(targetLoader, new Object[]{classLoaderName});
        log.info("Terracotta classloader name set to " + classLoaderName);
      } catch (Throwable t) {
        log.warn("Could not set Terracotta classloader name");
      }
      
      try {
        Class<?> processorClass = targetLoader.loadClass("com.tc.object.bytecode.hook.impl.ClassProcessorHelper");
        Class<?> namedLoaderClass = targetLoader.loadClass("com.tc.object.loaders.NamedClassLoader");
        Method registerMethod = processorClass.getMethod("registerGlobalLoader", namedLoaderClass);
        registerMethod.invoke(null, targetLoader);
      } catch (Exception e) {
        log.warn("Could not register MrSoaService classloader with Terracotta");
      }
    }
  }
}
