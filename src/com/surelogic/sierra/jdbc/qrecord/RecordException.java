package com.surelogic.sierra.jdbc.qrecord;

public class RecordException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5580262971996005549L;

	public RecordException() {
		super();
	}

	public RecordException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordException(String message) {
		super(message);
	}

	public RecordException(Throwable cause) {
		super(cause);
	}

}
