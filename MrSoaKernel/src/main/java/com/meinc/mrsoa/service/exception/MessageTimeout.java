package com.meinc.mrsoa.service.exception;

public class MessageTimeout extends Exception {

	private static final long serialVersionUID = 1L;

	public MessageTimeout() {
		super();
	}

	public MessageTimeout(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageTimeout(String message) {
		super(message);
	}

	public MessageTimeout(Throwable cause) {
		super(cause);
	}
}
