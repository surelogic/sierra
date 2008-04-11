package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.QB;

/**
 * A queryable JDBC statement, backed by a key. Any arguments passed into the
 * call are interpreted as arguments to a formatted string stored in the query
 * bank by the given key.
 * 
 * @author nathan
 * 
 * @param <T>
 */
final class QueryableStatement<T> implements Queryable<T> {

	private final Statement st;
	private final String query;
	private final ResultHandler<T> handler;

	QueryableStatement(Connection conn, String key, ResultHandler<T> handler) {
		try {
			this.st = conn.createStatement();
		} catch (final SQLException e) {
			throw new StatementException();
		}
		this.query = QB.get(key);
		this.handler = handler;
	}

	public T call(Object... args) {
		try {
			st.execute(String.format(query, args));
			return handler.handle(new ResultSetResult(st.getResultSet()));
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
	}

	public void finished() {
		try {
			st.close();
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
	}
}
