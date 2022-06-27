package com.meinc.commons.team.exception;

public class TeamAccountInactiveException 
extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public TeamAccountInactiveException() {
		super();
	}

	public TeamAccountInactiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public TeamAccountInactiveException(String message) {
		super(message);
	}

	public TeamAccountInactiveException(Throwable cause) {
		super(cause);
	}

}
