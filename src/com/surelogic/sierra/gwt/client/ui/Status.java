package com.surelogic.sierra.gwt.client.ui;

public class Status {
	private final State state;
	private final String message;

	public static enum State {
		WAIT, SUCCESS, FAILURE
	};

	public Status(final State state, final String message) {
		super();
		this.state = state;
		this.message = message;
	}

	public State getState() {
		return state;
	}

	public String getMessage() {
		return message;
	}

}
