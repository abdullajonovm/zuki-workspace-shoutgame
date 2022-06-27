package com.meinc.commons.team.exception;

public class TeamAuthenticationException 
extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public TeamAuthenticationException() {
		super();
	}

	public TeamAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TeamAuthenticationException(String message) {
		super(message);
	}

	public TeamAuthenticationException(Throwable cause) {
		super(cause);
	}

}
