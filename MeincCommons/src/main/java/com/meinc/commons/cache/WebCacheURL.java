package com.meinc.commons.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class that accepts an un-cached file URL and returns a ready-to-
 * use cached file URL.
 * 
 * @author Matt
 */
public class WebCacheURL implements IWebCache {
  private static final Log log = LogFactory.getLog(WebCacheURL.class);
  private static IWebCache webCache = new WebCacheURL();
  
  public static IWebCache getInstance() {
    return webCache;
  }
  
  /* (non-Javadoc)
   * @see com.meinc.commons.cache.IWebCache#buildClasspathCachedURL(java.lang.Long, java.lang.String, java.lang.String)
   */
  public String buildClasspathCachedURL(Long cacheId, String baseURL, String filePath)
  throws FileNotFoundException {
    return buildCachedURL(cacheId, baseURL, "/cache/cp", filePath);
  }
  
  /* (non-Javadoc)
   * @see com.meinc.commons.cache.IWebCache#buildServletCachedURL(java.lang.Long, java.lang.String, java.lang.String)
   */
  public String buildServletCachedURL(Long cacheId, String baseURL, String filePath)
  throws FileNotFoundException {
    return buildCachedURL(cacheId, baseURL, "/cache/sv", filePath);
  }
  
  /* (non-Javadoc)
   * @see com.meinc.commons.cache.IWebCache#buildApacheCachedURL(java.lang.Long, java.lang.String, java.lang.String)
   */
  public String buildApacheCachedURL(Long cacheId, String baseURL, String filePath)
  throws FileNotFoundException {
    return buildCachedURL(cacheId, baseURL, "/cache/ap", filePath);
  }
  
  /**
   * Builds a cached-url from the provided parameters
   * @param cacheId
   *        The cache-id to include in the returned URL
   * @param baseURL
   *        The un-cached base url to include in the returned URL
   * @param urlPrefix
   *        The prefix to prepend to the returned URL
   * @param filePath
   *        An optional absolute path to a file on the local machine.  If 
   *        this is non-null, a cache-id will be computed from the specified 
   *        file and the cacheId parameter will be ignored.
   * @return
   * @throws FileNotFoundException 
   */
  private String buildCachedURL(Long cacheId, String baseURL, String urlPrefix, String filePath) throws FileNotFoundException {
    if (filePath != null) {
      File file = new File(filePath);
      if (!file.exists())
        throw new FileNotFoundException("Specified file does not exist: " + filePath);
      cacheId = file.lastModified();
    }
    
    String prefix = urlPrefix + "/" + cacheId;
    String result;
    
    if (baseURL.startsWith("http://")) {
      // We have a full URL
      URL url;
      try {
        url = new URL(baseURL);
      } catch (MalformedURLException e) {
        RuntimeException e2 = new RuntimeException("Could not create cached URL from: " + baseURL, e);
        log.error(e2.getMessage(), e2);
        throw e2;
      }
      String urlPath = url.getPath();
      result = url.getProtocol() + "://" + url.getHost()
          + (url.getPort() == -1 ? "" : ":" + url.getPort())
          + prefix
          + (urlPath.startsWith("/") ? "" : "/") + urlPath
          + (url.getQuery() == null ? "" : "?" + url.getQuery());
    } else {
      result = prefix + baseURL;
    }
    
    /*if (log.isDebugEnabled())
      log.debug("Created Cache URL " + result + " from " + baseURL);*/
    
    return result;
  }
}
