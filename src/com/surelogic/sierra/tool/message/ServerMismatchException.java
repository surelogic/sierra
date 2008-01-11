package com.surelogic.sierra.tool.message;


/**
 * Indicates that the request expected a different server instance than actually
 * occurred
 * 
 * @author nathan
 * 
 */

public class ServerMismatchException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8082777217298968722L;

	public ServerMismatchException() {
		super();
	}

	public ServerMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerMismatchException(String message) {
		super(message);
	}

	public ServerMismatchException(Throwable cause) {
		super(cause);
	}

}
