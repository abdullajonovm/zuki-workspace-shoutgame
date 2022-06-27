package com.meinc.commons.team.exception;

public class TeamException 
extends RuntimeException
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TeamException()
	{
		super();
	}

	public TeamException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TeamException(String message)
	{
		super(message);
	}

	public TeamException(Throwable cause)
	{
		super(cause);
	}

}
