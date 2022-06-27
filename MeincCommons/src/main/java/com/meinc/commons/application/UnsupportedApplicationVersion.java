package com.meinc.commons.application;

public class UnsupportedApplicationVersion extends Exception {

	private static final long serialVersionUID = 6819894103869818413L;

	public UnsupportedApplicationVersion() {
		super();
	}

	public UnsupportedApplicationVersion(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedApplicationVersion(String message) {
		super(message);
	}

	public UnsupportedApplicationVersion(Throwable cause) {
		super(cause);
	}
}
