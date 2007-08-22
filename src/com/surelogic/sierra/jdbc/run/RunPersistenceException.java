package com.surelogic.sierra.jdbc.run;

public class RunPersistenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4279432890101035679L;

	public RunPersistenceException() {
		// Empty constructor
	}

	public RunPersistenceException(String message) {
		super(message);

	}

	public RunPersistenceException(Throwable cause) {
		super(cause);

	}

	public RunPersistenceException(String message, Throwable cause) {
		super(message, cause);

	}

}
