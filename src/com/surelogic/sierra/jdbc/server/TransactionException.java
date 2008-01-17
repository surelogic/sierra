package com.surelogic.sierra.jdbc.server;

/**
 * This class should be used to signal an exception that occurs while executing
 * a transaction.
 * 
 * @author nathan
 * 
 */
public class TransactionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 373638318353979424L;

	public TransactionException() {
		super();
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(Throwable cause) {
		super(cause);
	}

}
