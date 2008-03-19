package com.surelogic.sierra.jdbc;

public class ResultSetException extends JDBCException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4065867312547240131L;

	public ResultSetException() {
		super();
	}

	public ResultSetException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResultSetException(String message) {
		super(message);
	}

	public ResultSetException(Throwable cause) {
		super(cause);
	}

}
