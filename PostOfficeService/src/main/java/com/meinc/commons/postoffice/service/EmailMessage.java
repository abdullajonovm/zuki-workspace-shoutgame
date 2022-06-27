package com.meinc.commons.postoffice.service;

import java.io.Serializable;

public class EmailMessage implements Serializable {
  private static final long serialVersionUID = 336072239460747029L;

  /** PostOfficeService replaces this with email address of recipient when found. */
  public static final String EMAIL_REPLACE_CODE = "##L@##H";

  /** PostOfficeService replaces this with email name of recipient when found. */
  public static final String NAME_REPLACE_CODE = "##L@##N";

  /** PostOfficeService replaces this with email name of recipient when found. */
  public static final String UNSUBSCRIBE_REPLACE_CODE = "##L@##U";

  private boolean _compress;
  private String _message;

  public EmailMessage(String message) {
    this(message, true);
  }

  public EmailMessage(String message, boolean compress) {
    _message = message;
    _compress = compress;
  }

  public boolean isCompress() {
    return _compress;
  }

  public void setCompress(boolean compress) {
    _compress = compress;
  }

  public String getMessage() {
    return _message;
  }

  public void setMessage(String message) {
    _message = message;
  }
}
