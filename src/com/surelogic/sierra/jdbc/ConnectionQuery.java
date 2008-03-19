package com.surelogic.sierra.jdbc;

import java.sql.Connection;
import java.util.List;

public class ConnectionQuery implements Query {

	private final Connection conn;

	public ConnectionQuery(Connection conn) {
		this.conn = conn;
	}

	public Queryable<Void> prepared(String key) {
		return new QueryablePreparedStatement<Void>(conn, key,
				new EmptyResultHandler());
	}

	public <T> Queryable<List<T>> prepared(String key, RowHandler<T> rh) {
		return new QueryablePreparedStatement<List<T>>(conn, key,
				new ResultRowHandler<T>(rh));
	}

	public <T> Queryable<T> prepared(String key, ResultHandler<T> rh) {
		return new QueryablePreparedStatement<T>(conn, key, rh);
	}

	public Queryable<Void> statement(String key) {
		return new QueryableStatement<Void>(conn, key, new EmptyResultHandler());

	}

	public <T> Queryable<T> statement(String key, ResultHandler<T> rh) {
		return new QueryableStatement<T>(conn, key, rh);
	}

	public <T> Queryable<List<T>> statement(String key, RowHandler<T> rh) {
		return new QueryableStatement<List<T>>(conn, key,
				new ResultRowHandler<T>(rh));
	}

}
