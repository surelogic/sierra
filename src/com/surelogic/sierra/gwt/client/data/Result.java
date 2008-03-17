package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.List;

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

	public static Result success(List result) {
		return new Result(true, "", (Serializable) result);
	}

	public static Result success(String message, Serializable result) {
		return new Result(true, message, result);
	}

	public static Result success(String message, List result) {
		return new Result(true, message, (Serializable) result);
	}

	public static Result failure(String message) {
		return new Result(false, message, null);
	}

	public static Result failure(Serializable result) {
		return new Result(false, "", result);
	}

	public static Result failure(List result) {
		return new Result(false, "", (Serializable) result);
	}

	public static Result failure(String message, Serializable result) {
		return new Result(false, message, result);
	}

	public static Result failure(String message, List result) {
		return new Result(false, message, (Serializable) result);
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

	public Serializable getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

}
