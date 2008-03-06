package com.surelogic.sierra.tool.message;

public class ScanVersionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8452786165776202302L;

	public ScanVersionException() {
	}

	public ScanVersionException(String message) {
		super(message);
	}

	public ScanVersionException(Throwable cause) {
		super(cause);
	}

	public ScanVersionException(String message, Throwable cause) {
		super(message, cause);
	}

}
