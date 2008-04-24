package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.QB;

/**
 * A queryable prepared statement. Arguments passed to call are interpreted as
 * parameters to the statement, and should be of the form specified by
 * {@link JDBCUtils#fill(PreparedStatement, Object[])}
 * 
 * @author nathan
 * 
 * @param <T>
 */
public class QueryablePreparedStatement<T> implements Queryable<T> {

	private final PreparedStatement st;
	private final ResultHandler<T> rh;

	public QueryablePreparedStatement(PreparedStatement st, ResultHandler<T> rh) {
		this.st = st;
		this.rh = rh;
	}

	public QueryablePreparedStatement(Connection conn, String key,
			ResultHandler<T> rh) {
		try {
			st = conn.prepareStatement(QB.get(key));
		} catch (final SQLException e) {
			throw new StatementException(e);
		}
		this.rh = rh;
	}

	public T call(Object... args) {
		try {
			JDBCUtils.fill(st, args);
			st.execute();
			return rh.handle(new ResultSetResult(st.getResultSet()));
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
