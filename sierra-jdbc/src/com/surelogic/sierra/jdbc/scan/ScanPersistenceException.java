package com.surelogic.sierra.jdbc.scan;

public class ScanPersistenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4279432890101035679L;

	public ScanPersistenceException() {
		// Empty constructor
	}

	public ScanPersistenceException(String message) {
		super(message);

	}

	public ScanPersistenceException(Throwable cause) {
		super(cause);

	}

	public ScanPersistenceException(String message, Throwable cause) {
		super(message, cause);

	}

}
