package com.surelogic.sierra.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class LongView implements View {

	protected Long id;

	public Long getId() {
		return id;
	}

	public int read(ResultSet set, int idx) throws SQLException {
		this.id = set.getLong(idx++);
		return readRest(set, idx);
	}

	protected abstract int readRest(ResultSet set, int idx) throws SQLException;

}
