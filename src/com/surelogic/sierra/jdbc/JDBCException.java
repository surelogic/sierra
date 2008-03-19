package com.surelogic.sierra.jdbc;

public class JDBCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2205368726861435029L;

	public JDBCException() {
	}

	public JDBCException(String message) {
		super(message);
	}

	public JDBCException(Throwable cause) {
		super(cause);
	}

	public JDBCException(String message, Throwable cause) {
		super(message, cause);
	}

}
