package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.common.jdbc.QB;

public class QueryablePreparedStatement<T> implements Queryable<T> {

	private final PreparedStatement st;
	private final ResultHandler<T> rh;

	public QueryablePreparedStatement(Connection conn, String key,
			ResultHandler<T> rh) {
		try {
			st = conn.prepareStatement(QB.get(key));
		} catch (SQLException e) {
			throw new StatementException(e);
		}
		this.rh = rh;
	}

	public T call(Object... args) {
		try {
			JDBCUtils.fill(st, args);
			st.execute();
			return rh.handle(new ResultSetResult(st.getResultSet()));
		} catch (SQLException e) {
			throw new StatementException(e);
		}
	}

	public T call() {
		try {
			st.execute();
			return rh.handle(new ResultSetResult(st.getResultSet()));
		} catch (SQLException e) {
			throw new StatementException(e);
		}
	}

	public void finished() {
		try {
			st.close();
		} catch (SQLException e) {
			throw new StatementException(e);
		}
	}

}
