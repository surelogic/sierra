package com.surelogic.sierra.jdbc.settings;

public class InvalidServerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1656196711994983700L;

	public InvalidServerException() {
		super();
	}

	public InvalidServerException(final String message) {
		super(message);
	}

	public InvalidServerException(final Throwable cause) {
		super(cause);
	}

	public InvalidServerException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
