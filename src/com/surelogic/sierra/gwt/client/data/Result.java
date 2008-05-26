package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Result<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -3937812036639461449L;
	private boolean success;
	private String message;
	private T result;

	public Result() {
		super();
	}

	public Result(boolean success, String message, T result) {
		super();
		this.success = success;
		this.message = message;
		this.result = result;
	}

	public static <T extends Serializable> Result<T> success(T result) {
		return new Result<T>(true, "", result);
	}

	public static <T extends Serializable> Result<T> success(String message,
			T result) {
		return new Result<T>(true, message, result);
	}

	public static <T extends Serializable> Result<T> failure(T result) {
		return new Result<T>(false, "", result);
	}

	public static <T extends Serializable> Result<T> failure(String message,
			T result) {
		return new Result<T>(false, message, result);
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

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

}
