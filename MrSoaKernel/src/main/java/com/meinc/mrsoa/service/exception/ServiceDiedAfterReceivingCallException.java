package com.meinc.mrsoa.service.exception;

public class ServiceDiedAfterReceivingCallException extends AdaptorException {
  private static final long serialVersionUID = 7100283711963233211L;

  public ServiceDiedAfterReceivingCallException() {
    super();
  }

  public ServiceDiedAfterReceivingCallException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceDiedAfterReceivingCallException(String message) {
    super(message);
  }

  public ServiceDiedAfterReceivingCallException(Throwable cause) {
    super(cause);
  }
}
