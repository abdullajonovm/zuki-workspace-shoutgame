package com.meinc.commons;

public class PhoenixException 
extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public PhoenixException()
	{
		super();
	}

	public PhoenixException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PhoenixException(String message)
	{
		super(message);
	}

	public PhoenixException(Throwable cause)
	{
		super(cause);
	}

}
