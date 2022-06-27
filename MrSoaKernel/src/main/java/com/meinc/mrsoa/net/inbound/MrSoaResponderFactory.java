package com.meinc.mrsoa.net.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * Factory for {@link MrSoaResponder} objects to be used by
 * {@link MrSoaResponderPool}.
 * 
 * @author Matt
 */
class MrSoaResponderFactory implements PoolableObjectFactory {
  private static final Log log = LogFactory.getLog(MrSoaResponderFactory.class);
  private MrSoaResponderPool responderPool;
  
  /**
   * Each responder object requires a reference to its managing responder pool.
   * 
   * @param responderPool
   */
  public void setResponderPool(MrSoaResponderPool responderPool) {
    this.responderPool = responderPool;
  }

  @Override
  public Object makeObject() throws Exception {
    if (log.isDebugEnabled())
      log.debug("Pool new responder: "+responderPool);
    return new MrSoaResponder(responderPool);
  }

  @Override
  public void destroyObject(Object obj) throws Exception {
    if (log.isDebugEnabled())
      log.debug("Pool kill responder: "+responderPool);
    MrSoaResponder responder = (MrSoaResponder) obj;
    responder.shutdown();
  }

  @Override
  public boolean validateObject(Object obj) {
    MrSoaResponder responder = (MrSoaResponder) obj;
    return responder.isHealthy();
  }
  
  public void passivateObject(Object obj) throws Exception { }
  public void activateObject(Object obj) throws Exception { }
}
