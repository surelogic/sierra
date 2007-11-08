package com.surelogic.sierra.metrics.analysis;

public class InvalidFileException extends Exception {

	private static final long serialVersionUID = 4106816631538755398L;

	public InvalidFileException() {
		super();
	}

	public InvalidFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFileException(String message) {
		super(message);
	}

	public InvalidFileException(Throwable cause) {
		super(cause);
	}

}
