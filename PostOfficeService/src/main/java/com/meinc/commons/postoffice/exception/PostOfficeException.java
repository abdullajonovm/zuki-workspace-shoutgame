package com.meinc.commons.postoffice.exception;

public class PostOfficeException extends Exception
{
  private static final long serialVersionUID = -638552490122446338L;

  public PostOfficeException()
  {
    super();
  }

  public PostOfficeException(String message)
  {
    super(message);
  }

  public PostOfficeException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public PostOfficeException(Throwable cause)
  {
    super(cause);

  }
}
