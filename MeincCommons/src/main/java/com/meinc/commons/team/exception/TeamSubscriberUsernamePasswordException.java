package com.meinc.commons.team.exception;

public class TeamSubscriberUsernamePasswordException 
extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public TeamSubscriberUsernamePasswordException() {
		super();
	}

	public TeamSubscriberUsernamePasswordException(String message, Throwable cause) {
		super(message, cause);
	}

	public TeamSubscriberUsernamePasswordException(String message) {
		super(message);
	}

	public TeamSubscriberUsernamePasswordException(Throwable cause) {
		super(cause);
	}

}
