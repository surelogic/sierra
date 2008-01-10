package com.surelogic.sierra.tool.message;

import javax.xml.ws.WebFault;

/**
 * Indicates that the request expected a different server instance than actually
 * occurred
 * 
 * @author nathan
 * 
 */
@WebFault(name = "ServerMismatchFault")
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
