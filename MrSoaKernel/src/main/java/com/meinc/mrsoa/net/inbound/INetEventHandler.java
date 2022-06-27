package com.meinc.mrsoa.net.inbound;

interface INetEventHandler {

  /**
   * Notifies the handler that a network read or write event has occurred. This
   * method may optionally block until the listener is able to process such an
   * event.
   */
  public abstract void netEvent();
  
}