package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Status implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3150761641714394024L;

	private boolean success;
	private String message;

	public Status() {
	}

	public Status(boolean isSuccess, String message) {
		this.success = isSuccess;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static Status success(String message) {
		return new Status(true, message);
	}

	public static Status failure(String message) {
		return new Status(false, message);
	}
}
