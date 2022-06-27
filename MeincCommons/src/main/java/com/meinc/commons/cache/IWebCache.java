package com.meinc.commons.cache;

import java.io.FileNotFoundException;

public interface IWebCache {

  /**
   * Builds a new cached-url from an uncached-url. The returned cached-url will
   * contain an embedded instruction (".../cp/...") to serve the specified file
   * from the receiving Servlet's classpath.
   * <p>
   * Serving a file from a Servlet's classpath is accomplished by using the
   * <code>ClasspathStaticFileFilter</code> in the MeincWebCommons project.
   * <p>
   * If filePath is provided (not null), it is used to calculate a new cacheId
   * value and whatever was provided by the cacheId parameter is ignored.
   * 
   * <h4>Examples:</h4>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildClasspathCachedURL(98765, "/s2/img.jpg", null)</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/cp/98765/s2/img.jpg"</td></tr>
   * </table>
   * <p>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildClasspathCachedURL(null, "/s2/img.jpg", "c:/www/shout2/my_img.jpg")</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/cp/1383382909/s2/img.jpg"</td></tr>
   * </table>
   * 
   * @param  cacheId
   *         The cache-id number to use to construct the cached-url
   * @param  baseURL
   *         The uncached-url to transform into a cached-url
   * @param  filePath
   *         Optional path to a physical file on the local machine
   * @return The cached-url
   * @throws FileNotFoundException
   *         If filePath was provided and no such file could be found
   */
  public String buildClasspathCachedURL(Long cacheId, String baseURL,
      String filePath) throws FileNotFoundException;

  /**
   * Builds a new cached-url from an uncached-url. The returned cached-url will
   * contain an embedded instruction (".../sv/...") to serve the specified file
   * using the Servlet's normal file serving mechanism.
   * <p>
   * If filePath is provided (not null), it is used to calculate a new cacheId
   * value and whatever was provided by the cacheId parameter is ignored.
   * 
   * <h4>Examples:</h4>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildServletCachedURL(98765, "/s2/img.jpg", null)</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/sv/98765/s2/img.jpg"</td></tr>
   * </table>
   * <p>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildServletCachedURL(null, "/s2/img.jpg", "c:/www/shout2/my_img.jpg")</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/sv/1383382909/s2/img.jpg"</td></tr>
   * </table>
   * 
   * @param  cacheId
   *         The cache-id number to use to construct the cached-url
   * @param  baseURL
   *         The uncached-url to transform into a cached-url
   * @param  filePath
   *         Optional path to a physical file on the local machine
   * @return The cached-url
   * @throws FileNotFoundException
   *         If filePath was provided and no such file could be found
   */
  public String buildServletCachedURL(Long cacheId, String baseURL,
      String filePath) throws FileNotFoundException;

  /**
   * Builds a new cached-url from an uncached-url. The returned cached-url will
   * contain an embedded instruction (".../ap/...") to serve the specified file
   * from Apache.
   * <p>
   * If filePath is provided (not null), it is used to calculate a new cacheId
   * value and whatever was provided by the cacheId parameter is ignored.
   * 
   * <h4>Examples:</h4>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildApacheCachedURL(98765, "/s2/img.jpg", null)</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/ap/98765/s2/img.jpg"</td></tr>
   * </table>
   * <p>
   * <table>
   * <tr><td><b>Call:</b></td><td>buildApacheCachedURL(null, "/s2/img.jpg", "c:/www/shout2/my_img.jpg")</td></tr>
   * <tr><td><b>Result:</b></td><td>"/cache/ap/1383382909/s2/img.jpg"</td></tr>
   * </table>
   * 
   * @param  cacheId
   *         The cache-id number to use to construct the cached-url
   * @param  baseURL
   *         The uncached-url to transform into a cached-url
   * @param  filePath
   *         Optional path to a physical file on the local machine
   * @return The cached-url
   * @throws FileNotFoundException
   *         If filePath was provided and no such file could be found
   */
  public String buildApacheCachedURL(Long cacheId, String baseURL,
      String filePath) throws FileNotFoundException;

}