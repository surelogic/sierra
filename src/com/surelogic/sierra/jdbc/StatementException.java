package com.surelogic.sierra.jdbc;

public class StatementException extends JDBCException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -777476481276858626L;

	public StatementException() {
	}

	public StatementException(String message) {
		super(message);
	}

	public StatementException(Throwable cause) {
		super(cause);
	}

	public StatementException(String message, Throwable cause) {
		super(message, cause);
	}

}
