package com.meinc.mrsoa.service.exception;

public class CannotDeserializeExceptionWrapperException extends
    RuntimeException {

  private static final long serialVersionUID = 5874377383696007187L;
  
  private String exceptionClassname;

  public CannotDeserializeExceptionWrapperException() {
  }

  public CannotDeserializeExceptionWrapperException(String arg0) {
    super(arg0);
  }

  public CannotDeserializeExceptionWrapperException(Throwable arg0) {
    super(arg0);
  }

  public CannotDeserializeExceptionWrapperException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public String getExceptionClassname() {
    return exceptionClassname;
  }

  public void setExceptionClassname(String exceptionClassname) {
    this.exceptionClassname = exceptionClassname;
  }
}
