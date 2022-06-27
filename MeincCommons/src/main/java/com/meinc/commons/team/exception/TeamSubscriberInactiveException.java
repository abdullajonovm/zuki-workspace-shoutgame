package com.meinc.commons.team.exception;

public class TeamSubscriberInactiveException 
extends RuntimeException 
{
	private static final long serialVersionUID = 1L;

	public TeamSubscriberInactiveException() {
		super();
	}

	public TeamSubscriberInactiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public TeamSubscriberInactiveException(String message) {
		super(message);
	}

	public TeamSubscriberInactiveException(Throwable cause) {
		super(cause);
	}

}
