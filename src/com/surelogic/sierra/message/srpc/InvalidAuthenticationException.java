package com.surelogic.sierra.message.srpc;

public class InvalidAuthenticationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7626624568360683640L;

	public InvalidAuthenticationException() {
	}

	public InvalidAuthenticationException(String message) {
		super(message);
	}

	public InvalidAuthenticationException(Throwable cause) {
		super(cause);
	}

	public InvalidAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
