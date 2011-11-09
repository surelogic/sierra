package com.surelogic.sierra.message.srpc;

public class InvalidAuthenticationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7626624568360683640L;

	public InvalidAuthenticationException() {
		// Do nothing
	}

	public InvalidAuthenticationException(final String message) {
		super(message);
	}

	public InvalidAuthenticationException(final Throwable cause) {
		super(cause);
	}

	public InvalidAuthenticationException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
