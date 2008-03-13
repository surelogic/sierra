package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Result extends Status {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7892934566840797306L;
	private Serializable result;

	public Result() {
		super();
	}

	public Result(Serializable result) {
		super(true, "");
		this.result = result;
	}

	public Result(boolean isSuccess, String message) {
		super(isSuccess, message);
		this.result = null;
	}

	public Result(boolean isSuccess, String message, Serializable result) {
		super(isSuccess, message);
		this.result = result;
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
