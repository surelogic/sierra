package com.surelogic.sierra.message.srpc;

public class InvalidServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2347715549391623483L;

	public InvalidServiceException() {
		super();
	}

	public InvalidServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidServiceException(String message) {
		super(message);
	}

	public InvalidServiceException(Throwable cause) {
		super(cause);
	}

}
