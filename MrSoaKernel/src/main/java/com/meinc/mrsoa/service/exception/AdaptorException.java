package com.meinc.mrsoa.service.exception;

public class AdaptorException extends RuntimeException {

	private static final long serialVersionUID = -901983059434261295L;

	public AdaptorException() {
		super();
	}

	public AdaptorException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdaptorException(String message) {
		super(message);
	}

	public AdaptorException(Throwable cause) {
		super(cause);
	}
}
