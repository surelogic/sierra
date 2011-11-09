package com.surelogic.sierra.message.srpc;

public class ServiceInvocationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8323145297223686778L;

	public ServiceInvocationException() {
	}

	public ServiceInvocationException(String message) {
		super(message);
	}

	public ServiceInvocationException(Throwable cause) {
		super(cause);
	}

	public ServiceInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
