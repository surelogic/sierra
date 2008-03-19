package com.surelogic.sierra.jdbc;

public class EmptyResultHandler implements ResultHandler<Void> {

	public Void handle(Result r) {
		return null;
	}

}
