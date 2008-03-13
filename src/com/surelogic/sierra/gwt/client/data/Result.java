package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Result implements Serializable {
	private static final long serialVersionUID = -3937812036639461449L;
	private boolean success;
	private String message;
	private Serializable result;

	public Result() {
		super();
	}

	public Result(boolean success, String message, Serializable result) {
		super();
		this.success = success;
		this.message = message;
		this.result = result;
	}

	public static Result success(String message) {
		return new Result(true, message, null);
	}

	public static Result success(Serializable result) {
		return new Result(true, "", result);
	}

	public static Result failure(String message) {
		return new Result(false, message, null);
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

	public Object getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	public static Result success(String message, Serializable result) {
		return new Result(true, message, result);
	}

	public static Result fail(String message, Serializable result) {
		return new Result(false, message, result);
	}
}
