package com.meinc.commons.team.exception;

public class TeamAccountSuspendedException 
extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public TeamAccountSuspendedException() {
		super();
	}

	public TeamAccountSuspendedException(String message, Throwable cause) {
		super(message, cause);
	}

	public TeamAccountSuspendedException(String message) {
		super(message);
	}

	public TeamAccountSuspendedException(Throwable cause) {
		super(cause);
	}

}
