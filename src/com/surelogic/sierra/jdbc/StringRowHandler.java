package com.surelogic.sierra.jdbc;

public class StringRowHandler implements RowHandler<String> {

	public String handle(Row r) {
		return r.nextString();
	}

}
