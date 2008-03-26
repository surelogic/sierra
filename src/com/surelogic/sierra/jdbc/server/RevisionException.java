package com.surelogic.sierra.jdbc.server;

public class RevisionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9031133759280997305L;

	public RevisionException() {
	}

	public RevisionException(String message) {
		super(message);
	}

	public RevisionException(Throwable cause) {
		super(cause);
	}

	public RevisionException(String message, Throwable cause) {
		super(message, cause);
	}

}
