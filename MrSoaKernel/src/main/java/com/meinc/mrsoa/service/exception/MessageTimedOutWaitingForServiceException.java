package com.meinc.mrsoa.service.exception;

public class MessageTimedOutWaitingForServiceException extends AdaptorException {
  private static final long serialVersionUID = 2645252898275848949L;

  public MessageTimedOutWaitingForServiceException() {
    super();
  }

  public MessageTimedOutWaitingForServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public MessageTimedOutWaitingForServiceException(String message) {
    super(message);
  }

  public MessageTimedOutWaitingForServiceException(Throwable cause) {
    super(cause);
  }
}
