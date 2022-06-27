package com.meinc.commons;

public class PhoenixMethodAuthorizationException 
extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public PhoenixMethodAuthorizationException()
	{
		super();
	}

	public PhoenixMethodAuthorizationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PhoenixMethodAuthorizationException(String message)
	{
		super(message);
	}

	public PhoenixMethodAuthorizationException(Throwable cause)
	{
		super(cause);
	}

}
