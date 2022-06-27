package com.meinc.commons.team.exception;

/**
 * @author shawker
 */
public class TeamApplicationException 
extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public TeamApplicationException()
  {
    super();
  }

  public TeamApplicationException(String message)
  {
    super(message);
  }

  public TeamApplicationException(Throwable arg)
  {
    super(arg);
  }

  public TeamApplicationException(String message, Throwable arg)
  {
    super(message, arg);
  }

}
