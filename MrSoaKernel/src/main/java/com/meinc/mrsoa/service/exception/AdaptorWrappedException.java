package com.meinc.mrsoa.service.exception;

public class AdaptorWrappedException extends AdaptorException {

	private static final long serialVersionUID = -6811872231536384457L;
	
	public AdaptorWrappedException() {
		super();
	}

	public AdaptorWrappedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdaptorWrappedException(String message) {
		super(message);
	}

	public AdaptorWrappedException(Throwable cause) {
		super(cause);
	}
}
