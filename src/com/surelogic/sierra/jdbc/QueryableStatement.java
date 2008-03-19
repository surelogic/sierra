package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.QB;

final class QueryableStatement<T> implements Queryable<T> {

	private final Statement st;
	private final String query;
	private final ResultHandler<T> handler;

	QueryableStatement(Connection conn, String key, ResultHandler<T> handler) {
		try {
			this.st = conn.createStatement();
		} catch (SQLException e) {
			throw new StatementException();
		}
		this.query = QB.get(key);
		this.handler = handler;
	}

	public T call(Object... args) {
		try {
			st.execute(String.format(query, args));
			return handler.handle(new ResultSetResult(st.getResultSet()));
		} catch (SQLException e) {
			throw new StatementException(e);
		}
	}

	public T call() {
		try {
			st.execute(query);
			return handler.handle(new ResultSetResult(st.getResultSet()));
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
