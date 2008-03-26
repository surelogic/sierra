package com.surelogic.sierra.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ResultSetRow implements Row {

	private final ResultSet set;
	private int idx;

	ResultSetRow(ResultSet set) {
		this.set = set;
		clear();
	}

	public Date nextDate() {
		try {
			return set.getTimestamp(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	public int nextInt() {
		try {
			return set.getInt(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	public long nextLong() {
		try {
			return set.getLong(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	public String nextString() {
		try {
			return set.getString(idx++);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	public Integer nullableInt() {
		try {
			return JDBCUtils.getNullableInteger(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	public Long nullableLong() {
		try {
			return JDBCUtils.getNullableLong(idx++, set);
		} catch (final SQLException e) {
			throw new ResultSetException(e);
		}
	}

	void clear() {
		idx = 1;
	}
}
