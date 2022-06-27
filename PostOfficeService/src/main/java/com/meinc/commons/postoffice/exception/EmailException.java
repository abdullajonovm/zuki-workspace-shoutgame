package com.meinc.commons.postoffice.exception;

@SuppressWarnings("serial")
public class EmailException extends Exception {
  public EmailException(Throwable e) {
    super(e);
  }
}